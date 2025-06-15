package liautetlin.fourtitude_recipe_application.ui

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.View.VISIBLE
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.bumptech.glide.Glide
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import liautetlin.fourtitude_recipe_application.adapter.RecipeAdapter
import liautetlin.fourtitude_recipe_application.databinding.ActivityCreateRecipeBinding
import liautetlin.fourtitude_recipe_application.model.Recipe
import liautetlin.fourtitude_recipe_application.model.RecipeType
import liautetlin.fourtitude_recipe_application.viewmodel.RecipeViewModel

@AndroidEntryPoint
class CreateRecipeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCreateRecipeBinding
    private var recipeTypes: List<RecipeType> = emptyList()
    private var imageUrl: String = ""
    private var adapter: RecipeAdapter? = null
    private val viewModel: RecipeViewModel by viewModels()

    // Permission launcher
    private val galleryPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            requestGalleryPermission()
        } else {
            Toast.makeText(this, "Gallery access is needed to pick an image", Toast.LENGTH_SHORT)
                .show()
        }
    }

    // Image picker launcher
    private val galleryLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            binding.imageViewPreview.visibility = VISIBLE
            Glide.with(this).load(uri).into(binding.imageViewPreview)
            imageUrl = uri.toString()
            Log.d("create", "Loading URI: $imageUrl")

        }
    }


    private fun requestGalleryPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES)
                        == PackageManager.PERMISSION_GRANTED -> {

                    openGallery()
                }

                shouldShowRequestPermissionRationale(Manifest.permission.READ_MEDIA_IMAGES) -> {
                    Toast.makeText(this, "Gallery permission is required", Toast.LENGTH_SHORT)
                        .show()
                    galleryPermissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
                }

                else -> {
                    galleryPermissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
                }
            }
        } else {
            // Android 12 and below
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                openGallery()
            } else {
                permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }
    }

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            openGallery()
        } else {
            Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openGallery() {
        galleryLauncher.launch("image/*")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateRecipeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        imageUrl = ""

        loadRecipeTypes()

        binding.buttonChooseImage.setOnClickListener {
            requestGalleryPermission()
        }
        binding.buttonSaveRecipe.setOnClickListener {
            saveRecipe()
        }
    }

    private fun loadRecipesByType(typeId: String) {
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.getRecipesByType(typeId).collect { recipes ->
                    adapter?.submitList(recipes)
                }
            }
        }
    }

    private fun loadRecipeTypes() {
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.recipeTypes.collect { types ->
                    recipeTypes = types
                    val typeNames = recipeTypes.map { it.name }
                    Log.d("RoomDB", "Loaded types: $types")
                    val adapter = ArrayAdapter(
                        applicationContext,
                        android.R.layout.simple_spinner_item,
                        typeNames
                    )
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    binding.spinnerRecipeType.adapter = adapter

                    binding.spinnerRecipeType.onItemSelectedListener =
                        object : AdapterView.OnItemSelectedListener {
                            override fun onItemSelected(
                                parent: AdapterView<*>,
                                view: View?,
                                position: Int,
                                id: Long
                            ) {
                                val selectedType = recipeTypes[position].name
                                loadRecipesByType(selectedType)
                            }

                            override fun onNothingSelected(parent: AdapterView<*>) {}
                        }
                }
            }

            if (recipeTypes.isNotEmpty()) {
                binding.spinnerRecipeType.setSelection(0)
            }
        }
    }

    private fun saveRecipe() {
        val name = binding.editTextRecipeName.text.toString().trim()
        val ingredients = binding.editTextIngredients.text.toString().trim()
        val steps = binding.editTextSteps.text.toString().trim()
        val selectedType =
            binding.spinnerRecipeType.selectedItemPosition.takeIf { it >= 0 }
                ?.let { recipeTypes[it] }

        if (name.isBlank() || ingredients.isBlank() || steps.isBlank() || selectedType == null) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        val recipe =
            Recipe(
                title = name,
                type = selectedType.name,
                imageUri = imageUrl,
                ingredients = ingredients,
                steps = steps
            )

        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.insertRecipe(recipe)
                Toast.makeText(this@CreateRecipeActivity, "Recipe Saved!", Toast.LENGTH_SHORT)
                    .show()
                finish()
            }
        }
    }
}


