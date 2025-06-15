package liautetlin.fourtitude_recipe_application.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Recipe(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val imageUri: String,
    val ingredients: String,
    val steps: String,
    val type: String
)