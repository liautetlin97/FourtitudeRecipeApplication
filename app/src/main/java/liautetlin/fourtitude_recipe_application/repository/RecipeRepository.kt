package liautetlin.fourtitude_recipe_application.repository

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import liautetlin.fourtitude_recipe_application.Constants
import liautetlin.fourtitude_recipe_application.Utils
import liautetlin.fourtitude_recipe_application.dao.RecipeDao
import liautetlin.fourtitude_recipe_application.model.Recipe
import liautetlin.fourtitude_recipe_application.model.RecipeType
import javax.inject.Inject

class RecipeRepository @Inject constructor(
    private val recipeDao: RecipeDao,
    @ApplicationContext private val context: Context
) {
    fun getRecipeTypes(): Flow<List<RecipeType>> = flow {
        val json = Utils.loadJSONFromAsset(context, Constants.FILE_NAME)
        val types = Utils.parseRecipeTypes(json)
        emit(types)
    }.flowOn(Dispatchers.IO)

    fun getRecipesByType(type: String): Flow<List<Recipe>> {
        return recipeDao.getRecipesByType(type)
    }

    suspend fun getRecipeById(id: Int): Recipe {
        return recipeDao.getById(id)
    }

    suspend fun insertRecipe(recipe: Recipe) {
        recipeDao.insertRecipe(recipe)
    }

    suspend fun updateRecipe(recipe: Recipe) {
        recipeDao.updateRecipe(recipe)
    }

    suspend fun deleteRecipe(recipe: Recipe) {
        recipeDao.deleteRecipe(recipe)
    }
}