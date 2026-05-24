package com.grupo.gestioneventos

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.grupo.gestioneventos.databinding.ActivityLoginBinding
import com.grupo.gestioneventos.network.ApiClient
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.rootLogin.applySystemBarPadding(applyTop = true, applyBottom = true)

        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            binding.tilEmail.error = if (email.isEmpty()) "El correo es requerido" else null
            binding.tilPassword.error = if (password.isEmpty()) "La contrasena es requerida" else null

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

        binding.tvRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }
}
