package liautetlin.fourtitude_recipe_application.ui

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View.VISIBLE
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
import liautetlin.fourtitude_recipe_application.R
import liautetlin.fourtitude_recipe_application.databinding.ActivityRecipeDetailBinding
import liautetlin.fourtitude_recipe_application.model.Recipe
import liautetlin.fourtitude_recipe_application.model.RecipeType
import liautetlin.fourtitude_recipe_application.viewmodel.RecipeViewModel

@AndroidEntryPoint
class RecipeDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRecipeDetailBinding
    private var currentRecipe: Recipe? = null
    private var recipeTypes: List<RecipeType> = emptyList()
    private var recipeId: Int = -1
    private var imageUrl: String = ""
    private val viewModel: RecipeViewModel by viewModels()
    private var isRecipeLoaded = false
    private var isTypesLoaded = false
    private var isImageUpdated = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRecipeDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        recipeId = intent.getIntExtra("RECIPE_ID", -1)
        if (recipeId == -1) {
            Toast.makeText(this, "Invalid recipe ID", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        setupObservers()
        setupListeners()
        viewModel.loadRecipe(recipeId)
    }

    private fun setupListeners() {
        binding.buttonChooseImage.setOnClickListener {
            requestGalleryPermission()
        }

        binding.buttonUpdate.setOnClickListener {
            updateRecipe()
        }

        binding.buttonDelete.setOnClickListener {
            deleteRecipe()
        }
    }

    private fun setupObservers() {
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.recipeTypes.collect { types ->
                    recipeTypes = types
                    setupSpinner()
                    isTypesLoaded = true
                    trySelectSpinnerItem()
                }
            }
        }
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                if (!isImageUpdated) {
                    viewModel.currentRecipe.collect { recipe ->
                        if (recipe != null) {
                            currentRecipe = recipe
                            binding.editTextRecipeName.setText(currentRecipe?.title)
                            binding.editTextIngredients.setText(currentRecipe?.ingredients)
                            binding.editTextSteps.setText(currentRecipe?.steps)
                            imageUrl = currentRecipe?.imageUri ?: ""
                            val uri = Uri.parse(currentRecipe?.imageUri)
                            if (currentRecipe?.imageUri?.isNotBlank() == true) {
                                binding.imageViewPreview.visibility = VISIBLE
                                Glide.with(this@RecipeDetailActivity)
                                    .load(uri)
                                    .placeholder(R.drawable.ic_launcher_background)
                                    .into(binding.imageViewPreview)
                            }
                            isRecipeLoaded = true
                            trySelectSpinnerItem()
                        }
                    }
                }
            }
        }
    }

    private fun trySelectSpinnerItem() {
        if (isRecipeLoaded && isTypesLoaded) {
            val typeIndex = recipeTypes.indexOfFirst { it.name == currentRecipe?.type }
            if (typeIndex != -1) {
                binding.spinnerRecipeType.setSelection(typeIndex)
            }
        }
    }

    private fun setupSpinner() {
        val typeNames = recipeTypes.map { it.name }
        val adapter = ArrayAdapter(
            this@RecipeDetailActivity,
            android.R.layout.simple_spinner_item,
            typeNames
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerRecipeType.adapter = adapter
    }

    private fun updateRecipe() {
        val name = binding.editTextRecipeName.text.toString().trim()
        val ingredients = binding.editTextIngredients.text.toString().trim()
        val steps = binding.editTextSteps.text.toString().trim()
        val selectedType =
            binding.spinnerRecipeType.selectedItemPosition.takeIf { it >= 0 }
                ?.let { recipeTypes[it] }
        Toast.makeText(this, "" + selectedType, Toast.LENGTH_SHORT).show()

        if (name.isBlank() || ingredients.isBlank() || steps.isBlank() || selectedType == null) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        val updatedRecipe = currentRecipe?.copy(
            title = name,
            ingredients = ingredients,
            steps = steps,
            type = selectedType.name,
            imageUri = imageUrl
        )
        Log.d("create", "created URI: $imageUrl")

        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                if (updatedRecipe != null) {
                    viewModel.updateRecipe(updatedRecipe)
                    isImageUpdated = false
                }
                Toast.makeText(
                    this@RecipeDetailActivity,
                    "Recipe updated",
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            }
        }
    }

    private fun deleteRecipe() {
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                currentRecipe?.let {
                    viewModel.deleteRecipe(it)
                    isImageUpdated = false
                    Toast.makeText(
                        this@RecipeDetailActivity,
                        "Recipe deleted",
                        Toast.LENGTH_SHORT
                    ).show()
                    finish()
                }
            }
        }
    }

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
            Glide.with(this@RecipeDetailActivity).load(uri).into(binding.imageViewPreview)
            imageUrl = uri.toString()
            isImageUpdated = true
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

    private fun requestGalleryPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.READ_MEDIA_IMAGES
                ) == PackageManager.PERMISSION_GRANTED -> {
                    openGallery()
                }

                shouldShowRequestPermissionRationale(Manifest.permission.READ_MEDIA_IMAGES) -> {
                    Toast.makeText(this, "Gallery permission is required", Toast.LENGTH_SHORT)
                        .show()
                    galleryPermissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
                }

                else -> {
                    // Request permission
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

    private fun openGallery() {
        galleryLauncher.launch("image/*")
    }
}