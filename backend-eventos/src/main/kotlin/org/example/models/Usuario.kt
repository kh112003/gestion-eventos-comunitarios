package org.example.org.example.models


import kotlinx.serialization.Serializable

@Serializable
data class UsuarioRequest(
    val nombre: String,
    val email: String,
    val password: String
)

@Serializable
data class LoginRequest(
    val email: String,
    val password: String
)

@Serializable
data class AuthResponse(
    val mensaje: String,
    val token: String? = null
)