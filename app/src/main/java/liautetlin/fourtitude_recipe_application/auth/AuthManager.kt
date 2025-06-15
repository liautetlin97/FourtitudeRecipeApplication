package liautetlin.fourtitude_recipe_application.auth

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

object AuthManager {
    private const val FILE_NAME = "secure_auth"
    private const val KEY_IS_LOGGED_IN = "is_logged_in"
    private const val KEY_USERNAME = "username"
    private const val KEY_PASSWORD = "password"

    fun getPrefs(context: Context): SharedPreferences {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        return EncryptedSharedPreferences.create(
            context,
            FILE_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    fun login(context: Context, username: String, password: String): Boolean {
        // Simulate successful login (replace with real backend check)
        if (username == "admin" && password == "password") {
            val prefs = getPrefs(context)
            prefs.edit().apply {
                putBoolean(KEY_IS_LOGGED_IN, true)
                putString(KEY_USERNAME, username)
                putString(KEY_PASSWORD, password)
                apply()
            }
            return true
        }
        return false
    }

    fun logout(context: Context) {
        getPrefs(context).edit().clear().apply()
    }

    fun isLoggedIn(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_IS_LOGGED_IN, false)
    }

    fun getUsername(context: Context): String? {
        return getPrefs(context).getString(KEY_USERNAME, null)
    }
}
