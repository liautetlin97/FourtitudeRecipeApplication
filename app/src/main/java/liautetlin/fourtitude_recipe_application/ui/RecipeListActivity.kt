package liautetlin.fourtitude_recipe_application.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import liautetlin.fourtitude_recipe_application.adapter.RecipeAdapter
import liautetlin.fourtitude_recipe_application.auth.AuthManager
import liautetlin.fourtitude_recipe_application.databinding.ActivityRecipeListBinding
import liautetlin.fourtitude_recipe_application.model.RecipeType
import liautetlin.fourtitude_recipe_application.viewmodel.RecipeViewModel

@AndroidEntryPoint
class RecipeListActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRecipeListBinding
    private var adapter: RecipeAdapter? = null
    private var recipeTypes: List<RecipeType> = emptyList()
    private val viewModel: RecipeViewModel by viewModels()
    private var selectedRecipeType: String? = null
    private var selectedPosition = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityRecipeListBinding.inflate(layoutInflater)
        setContentView(binding.root)
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.recipeTypes.collect { types ->
                    recipeTypes = types
                    val typeNames = recipeTypes.map { it.name }
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
                                selectedPosition = position
                                val selectedType = typeNames[position]
                                loadRecipesByType(selectedType)
                            }

                            override fun onNothingSelected(parent: AdapterView<*>) {}
                        }
                }
            }
        }
        if (recipeTypes.isNotEmpty()) {
            binding.spinnerRecipeType.setSelection(0)
        }

        binding.fabAddRecipe.setOnClickListener {
            startActivity(Intent(this, CreateRecipeActivity::class.java))
        }

        binding.logout.setOnClickListener {
            AuthManager.logout(this)
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        adapter = RecipeAdapter { recipe ->
            val intent = Intent(this, RecipeDetailActivity::class.java)
            intent.putExtra("RECIPE_ID", recipe.id)
            startActivity(intent)
        }
        binding.recipeRecyclerView.adapter = adapter
        binding.recipeRecyclerView.layoutManager = LinearLayoutManager(this)
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

    override fun onResume() {
        super.onResume()
        binding.spinnerRecipeType.setSelection(selectedPosition)
        selectedRecipeType?.let { type ->
            lifecycleScope.launch {
                viewModel.getRecipesByType(type).collect { recipes ->
                    adapter?.submitList(recipes)
                }
            }
        }
    }
}