package com.grupo.gestioneventos

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.grupo.gestioneventos.databinding.ActivityLoginBinding
import com.grupo.gestioneventos.network.ApiClient
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var googleClient: GoogleSignInClient

    private val googleLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.result
            val idToken = account.idToken
            if (idToken.isNullOrBlank()) {
                Toast.makeText(this, "No se pudo obtener el token de Google", Toast.LENGTH_SHORT).show()
                return@registerForActivityResult
            }
            loginConGoogle(idToken, account.displayName.orEmpty(), account.email.orEmpty())
        } catch (e: Exception) {
            Toast.makeText(this, "Inicio con Google cancelado", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        googleClient = GoogleSignIn.getClient(
            this,
            GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()
        )

        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launch {
                try {
                    val response = ApiClient.apiService.login(
                        mapOf("email" to email, "password" to password)
                    )
                    if (response.isSuccessful) {
                        Sesion.guardar(this@LoginActivity, 1, email.substringBefore("@"), email)
                        Toast.makeText(this@LoginActivity, "Login exitoso", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this@LoginActivity, EventosActivity::class.java))
                        finish()
                    } else {
                        Toast.makeText(this@LoginActivity, "Credenciales incorrectas", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(this@LoginActivity, "Error de conexion", Toast.LENGTH_SHORT).show()
                }
            }
        }

        binding.btnGoogleLogin.setOnClickListener {
            googleLauncher.launch(googleClient.signInIntent)
        }

        binding.tvRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun loginConGoogle(idToken: String, nombre: String, email: String) {
        lifecycleScope.launch {
            try {
                val response = ApiClient.apiService.loginGoogle(mapOf("idToken" to idToken))
                if (response.isSuccessful) {
                    Sesion.guardar(this@LoginActivity, 1, nombre.ifBlank { "Usuario Google" }, email)
                    Toast.makeText(this@LoginActivity, "Login con Google exitoso", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this@LoginActivity, EventosActivity::class.java))
                    finish()
                } else {
                    Toast.makeText(this@LoginActivity, "No se pudo validar Google en la API", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@LoginActivity, "Error de conexion con Google", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
