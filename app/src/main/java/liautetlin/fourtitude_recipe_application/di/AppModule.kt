package liautetlin.fourtitude_recipe_application.di

import android.app.Application
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import liautetlin.fourtitude_recipe_application.dao.RecipeDao
import liautetlin.fourtitude_recipe_application.dao.RecipeTypeDao
import liautetlin.fourtitude_recipe_application.db.RecipeDatabase
import javax.inject.Singleton
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideRecipeDatabase(app: Application): RecipeDatabase {
        return RecipeDatabase.getInstance(app)
    }

    @Provides
    @Singleton
    fun provideRecipeDao(db: RecipeDatabase): RecipeDao {
        return db.recipeDao()
    }

    @Provides
    @Singleton
    fun provideRecipeTypeDao(db: RecipeDatabase): RecipeTypeDao {
        return db.recipeTypeDao()
    }
}
