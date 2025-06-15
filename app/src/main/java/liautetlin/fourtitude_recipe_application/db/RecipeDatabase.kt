package liautetlin.fourtitude_recipe_application.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import liautetlin.fourtitude_recipe_application.Constants.FILE_NAME
import liautetlin.fourtitude_recipe_application.dao.RecipeDao
import liautetlin.fourtitude_recipe_application.dao.RecipeTypeDao
import liautetlin.fourtitude_recipe_application.model.Recipe
import liautetlin.fourtitude_recipe_application.model.RecipeType

@Database(entities = [Recipe::class, RecipeType::class], version = 1)
abstract class RecipeDatabase : RoomDatabase() {
    abstract fun recipeDao(): RecipeDao
    abstract fun recipeTypeDao(): RecipeTypeDao

    companion object {
        @Volatile
        private var INSTANCE: RecipeDatabase? = null
        fun getInstance(context: Context): RecipeDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    RecipeDatabase::class.java, "recipe1.db"
                )
                    .fallbackToDestructiveMigration(true)
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            CoroutineScope(Dispatchers.IO).launch {
                                val tempInstance = INSTANCE
                                if (tempInstance != null) {
                                    val recipeDao = tempInstance.recipeDao()
                                    val recipeTypeDao = tempInstance.recipeTypeDao()

                                    val json = context.assets.open(FILE_NAME).bufferedReader().use { it.readText() }
                                    val recipeTypes = Gson().fromJson<List<RecipeType>>(json, object : TypeToken<List<RecipeType>>() {}.type)
                                    recipeTypeDao.insertAll(recipeTypes)

                                    val sampleRecipes = recipeList()
                                    recipeDao.insertAll(sampleRecipes)
                                }
                            }
                        }
                    })
                    .build()
                    .also { INSTANCE = it }

                instance
            }
        }

        private fun recipeList() = listOf(
            Recipe(
                title = "Spaghetti Carbonara",
                type = "Western",
                imageUri = "https://upload.wikimedia.org/wikipedia/commons/thumb/3/30/Spaghetti_carbonara.jpg/640px-Spaghetti_carbonara.jpg",
                ingredients = "Spaghetti, Eggs, Parmesan Cheese, Bacon, Black Pepper",
                steps = "Cook pasta. Mix eggs and cheese. Cook bacon. Combine all with pasta over heat."
            ),
            Recipe(
                title = "Grilled Chicken Crop",
                type = "Western",
                imageUri = "https://upload.wikimedia.org/wikipedia/commons/thumb/a/a1/Chicken_chop_grill.jpg/640px-Chicken_chop_grill.jpg",
                ingredients = "Chicken, Spices, Olive oil",
                steps = "Marinate and grill"
            ),
            Recipe(
                title = "Nasi Lemak",
                type = "Local",
                imageUri = "https://upload.wikimedia.org/wikipedia/commons/thumb/4/41/Nasi_Lemak_Gunung_Berapi.jpg/640px-Nasi_Lemak_Gunung_Berapi.jpg",
                ingredients = "Rice, Coconut Milk, Anchovies, Egg, Sambal, Cucumber",
                steps = "Cook rice in coconut milk. Fry anchovies. Serve with egg, sambal, cucumber."
            ),
            Recipe(
                title = "Char Kway Teow",
                type = "Local",
                imageUri = "https://upload.wikimedia.org/wikipedia/commons/thumb/d/d8/Kwetiau_Kuah_Ayam_-_rice_noodles_and_chicken.jpg/640px-Kwetiau_Kuah_Ayam_-_rice_noodles_and_chicken.jpg",
                ingredients = "Flat rice noodles, Shrimp, Chinese sausage, Eggs, Soy sauce, Garlic",
                steps = "Stir-fry garlic, sausage, shrimp. Add noodles, soy sauce, egg, and stir-fry well."
            ),
            Recipe(
                title = "Sushi Rolls",
                type = "Japanese",
                imageUri = "https://upload.wikimedia.org/wikipedia/commons/thumb/b/b2/Kingston_and_Midland_sushi_rolls_at_Ikki_Sushi.jpg/640px-Kingston_and_Midland_sushi_rolls_at_Ikki_Sushi.jpg",
                ingredients = "Sushi rice, Nori, Cucumber, Avocado, Crab stick",
                steps = "Place rice on nori, add fillings, roll tightly, and slice."
            ),
            Recipe(
                title = "Chicken Teriyaki",
                type = "Japanese",
                imageUri = "https://upload.wikimedia.org/wikipedia/commons/thumb/6/68/Teriyaki_chicken_bento_by_Lil%27_Dee_in_Melbourne.jpg/640px-Teriyaki_chicken_bento_by_Lil%27_Dee_in_Melbourne.jpg",
                ingredients = "Chicken thighs, Soy sauce, Mirin, Sugar, Ginger, Garlic",
                steps = "Marinate chicken. Cook in pan until caramelized. Serve with rice."
            ),
            Recipe(
                title = "Chocolate Brownies",
                type = "Dessert",
                imageUri = "https://upload.wikimedia.org/wikipedia/commons/thumb/4/49/Black_bean_Brownies_%2834470711140%29.jpg/1280px-Black_bean_Brownies_%2834470711140%29.jpg",
                ingredients = "Flour, Cocoa powder, Sugar, Eggs, Butter",
                steps = "Mix ingredients. Bake at 175°C for 25 minutes."
            ),
            Recipe(
                title = "Mango Pudding",
                type = "Dessert",
                imageUri = "https://upload.wikimedia.org/wikipedia/commons/thumb/0/0e/Mango_pudding.JPG/1280px-Mango_pudding.JPG",
                ingredients = "Mango, Gelatin, Sugar, Cream, Milk",
                steps = "Blend mango with sugar. Heat with gelatin. Add milk and cream. Chill to set."
            )
        )
    }
}
