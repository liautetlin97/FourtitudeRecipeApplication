package liautetlin.fourtitude_recipe_application.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import liautetlin.fourtitude_recipe_application.model.Recipe
import liautetlin.fourtitude_recipe_application.model.RecipeType
import liautetlin.fourtitude_recipe_application.repository.RecipeRepository
import javax.inject.Inject

@HiltViewModel
class RecipeViewModel @Inject constructor(
    private val repository: RecipeRepository
) : ViewModel() {
    private val _recipeTypes = MutableStateFlow<List<RecipeType>>(emptyList())
    val recipeTypes: StateFlow<List<RecipeType>> = _recipeTypes
    val currentRecipe = MutableStateFlow<Recipe?>(null)

    init {
        viewModelScope.launch {
            repository.getRecipeTypes().collect {
                _recipeTypes.value = it
            }
        }
    }

    fun loadRecipe(recipeId: Int) {
        viewModelScope.launch {
            currentRecipe.value = repository.getRecipeById(recipeId)
        }
    }

    fun getRecipesByType(type: String): Flow<List<Recipe>> {
        return repository.getRecipesByType(type)
    }

    fun insertRecipe(updated: Recipe) {
        viewModelScope.launch {
            repository.insertRecipe(updated)
        }
    }

    fun updateRecipe(updated: Recipe) {
        viewModelScope.launch {
            repository.updateRecipe(updated)
        }
    }

    fun deleteRecipe(recipe: Recipe) {
        viewModelScope.launch {
            repository.deleteRecipe(recipe)
        }
    }
}