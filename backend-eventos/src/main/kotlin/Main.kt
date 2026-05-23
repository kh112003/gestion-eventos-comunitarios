package org.example

import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import org.example.database.DatabaseConfig
import org.example.routes.authRoutes
import org.example.routes.eventoRoutes
import org.example.utils.FirebaseConfig
import org.example.routes.asistenciaRoutes

fun main() {
    DatabaseConfig.init()
    FirebaseConfig.init()

    embeddedServer(Netty, port = 8080) {
        install(ContentNegotiation) {
            json()
        }
        routing {
            get("/") {
                call.respondText("API Gestión de Eventos funcionando!")
            }
            authRoutes()
            eventoRoutes()
            asistenciaRoutes()
        }
    }.start(wait = true)
}