package liautetlin.fourtitude_recipe_application.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import dagger.hilt.android.AndroidEntryPoint
import liautetlin.fourtitude_recipe_application.R
import liautetlin.fourtitude_recipe_application.auth.AuthManager
import liautetlin.fourtitude_recipe_application.databinding.ActivityLoginBinding
import liautetlin.fourtitude_recipe_application.databinding.ActivityRecipeListBinding

@AndroidEntryPoint
class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (AuthManager.isLoggedIn(this)) {
            goToMain()
            return
        }

       binding.buttonLogin.setOnClickListener {
            val username = binding.editTextUsername.text.toString()
            val password = binding.editTextPassword.text.toString()

            if (AuthManager.login(this, username, password)) {
                Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show()
                goToMain()
            } else {
                Toast.makeText(this, "Invalid credentials", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun goToMain() {
        startActivity(Intent(this, RecipeListActivity::class.java))
        finish()
    }
}
