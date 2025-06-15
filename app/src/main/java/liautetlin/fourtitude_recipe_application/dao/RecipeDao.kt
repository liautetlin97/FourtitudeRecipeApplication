package liautetlin.fourtitude_recipe_application.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import liautetlin.fourtitude_recipe_application.model.Recipe
import liautetlin.fourtitude_recipe_application.model.RecipeType

@Dao
interface RecipeDao {
    @Query("SELECT * FROM Recipe WHERE type = :type")
    fun getRecipesByType(type: String): Flow<List<Recipe>>

    @Query("SELECT * FROM Recipe WHERE id = :id")
    suspend fun getById(id: Int): Recipe

    @Query("SELECT * FROM Recipe")
    fun getAllRecipes(): Flow<List<Recipe>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(recipes: List<Recipe>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecipe(recipe: Recipe)

    @Update
    suspend fun updateRecipe(recipe: Recipe)

    @Delete
    suspend fun deleteRecipe(recipe: Recipe)
}

@Dao
interface RecipeTypeDao {
    @Query("SELECT * FROM RecipeType")
    fun getAll(): LiveData<List<RecipeType>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(types: List<RecipeType>)
}
