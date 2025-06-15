package liautetlin.fourtitude_recipe_application.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class RecipeType(
    @PrimaryKey val id: Int,
    val name: String
)
