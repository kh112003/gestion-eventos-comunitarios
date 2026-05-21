package org.example.org.example.routes


import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.example.org.example.models.AuthResponse
import org.example.org.example.models.LoginRequest
import org.example.org.example.models.UsuarioRequest
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.mindrot.jbcrypt.BCrypt

fun Route.authRoutes() {

    post("/auth/register") {
        val request = call.receive<UsuarioRequest>()

        val usuarioExiste = transaction {
            exec("SELECT email FROM usuarios WHERE email = '${request.email}'") { rs ->
                rs.next()
            }
        }

        if (usuarioExiste == true) {
            call.respond(HttpStatusCode.Conflict,
                AuthResponse("El email ya está registrado"))
            return@post
        }

        val passwordHash = BCrypt.hashpw(request.password, BCrypt.gensalt())

        transaction {
            exec("INSERT INTO usuarios (nombre, email, password) VALUES ('${request.nombre}', '${request.email}', '$passwordHash')")
        }

        call.respond(HttpStatusCode.Created,
            AuthResponse("Usuario registrado exitosamente"))
    }

    post("/auth/login") {
        val request = call.receive<LoginRequest>()

        val passwordHash = transaction {
            var hash: String? = null
            exec("SELECT password FROM usuarios WHERE email = '${request.email}'") { rs ->
                if (rs.next()) hash = rs.getString("password")
            }
            hash
        }

        if (passwordHash == null || !BCrypt.checkpw(request.password, passwordHash)) {
            call.respond(HttpStatusCode.Unauthorized,
                AuthResponse("Credenciales incorrectas"))
            return@post
        }

        val token = com.auth0.jwt.JWT.create()
            .withClaim("email", request.email)
            .withExpiresAt(java.util.Date(System.currentTimeMillis() + 86400000))
            .sign(com.auth0.jwt.algorithms.Algorithm.HMAC256("secret_key_eventos_2024"))

        call.respond(HttpStatusCode.OK,
            AuthResponse("Login exitoso", token))
    }

    post("/auth/google") {
        val body = call.receive<Map<String, String>>()
        val idToken = body["idToken"]
            ?: return@post call.respond(HttpStatusCode.BadRequest,
                AuthResponse("Token requerido"))

        try {
            val decodedToken = com.google.firebase.auth.FirebaseAuth
                .getInstance()
                .verifyIdToken(idToken)

            val email = decodedToken.email
            val nombre = decodedToken.name ?: "Usuario Google"

            transaction {
                exec("INSERT OR IGNORE INTO usuarios (nombre, email, proveedor) VALUES ('$nombre', '$email', 'google')")
            }

            val token = com.auth0.jwt.JWT.create()
                .withClaim("email", email)
                .withExpiresAt(java.util.Date(System.currentTimeMillis() + 86400000))
                .sign(com.auth0.jwt.algorithms.Algorithm.HMAC256("secret_key_eventos_2024"))

            call.respond(HttpStatusCode.OK, AuthResponse("Login con Google exitoso", token))

        } catch (e: Exception) {
            call.respond(HttpStatusCode.Unauthorized, AuthResponse("Token inválido"))
        }
    }
}