package liautetlin.fourtitude_recipe_application

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.provider.DocumentsContract
import android.provider.MediaStore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import liautetlin.fourtitude_recipe_application.model.RecipeType

object Utils {
    fun loadJSONFromAsset(context: Context, filename: String): String {
        return context.assets.open(filename).bufferedReader().use { it.readText() }
    }

    fun parseRecipeTypes(json: String): List<RecipeType> {
        val gson = Gson()
        val type = object : TypeToken<List<RecipeType>>() {}.type
        return gson.fromJson(json, type)
    }

    fun getMediaUriFromDocumentUri(context: Context, uri: Uri): Uri? {
        return if (uri.toString().contains("document")) {
            if (DocumentsContract.isDocumentUri(context, uri)) {
                val docId = DocumentsContract.getDocumentId(uri)
                val split = docId.split(":")
                if (split.size == 2) {
                    val type = split[0]
                    val id = split[1].toLongOrNull() ?: return null

                    return when (type) {
                        "image" -> ContentUris.withAppendedId(
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id
                        )
                        else -> null
                    }
                }
            }
            return null
        } else {
            uri
        }
    }
}
