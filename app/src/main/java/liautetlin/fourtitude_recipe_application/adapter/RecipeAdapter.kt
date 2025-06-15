package liautetlin.fourtitude_recipe_application.adapter

import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import liautetlin.fourtitude_recipe_application.R
import liautetlin.fourtitude_recipe_application.Utils.getMediaUriFromDocumentUri
import liautetlin.fourtitude_recipe_application.model.Recipe

class RecipeAdapter(
    private val onItemClick: (Recipe) -> Unit
) : ListAdapter<Recipe, RecipeAdapter.RecipeViewHolder>(DiffCallback()) {

    inner class RecipeViewHolder(private val view: View) : RecyclerView.ViewHolder(view) {
        private val nameText: TextView = view.findViewById(R.id.tvRecipeTitle)
        private val imageView: ImageView = view.findViewById(R.id.imageRecipe)

        fun bind(recipe: Recipe) {
            nameText.text = recipe.title
            val uri = Uri.parse(recipe.imageUri)
            val imageURL = getMediaUriFromDocumentUri(view.context, uri)
            if (recipe.imageUri.isNotBlank()) {
                Glide.with(view.context)
                    .load(imageURL)
                    .placeholder(R.drawable.ic_launcher_background)
                    .into(imageView)
                Log.d("Adapter", "Loading URI: $imageURL")
            } else {
                imageView.setImageResource(R.drawable.ic_launcher_background)
                Log.d("Adapter", "error")
            }

            view.setOnClickListener {
                onItemClick(recipe)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecipeViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_recipe, parent, false)
        return RecipeViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecipeViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class DiffCallback : DiffUtil.ItemCallback<Recipe>() {
        override fun areItemsTheSame(oldItem: Recipe, newItem: Recipe) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Recipe, newItem: Recipe) = oldItem == newItem
    }
}
