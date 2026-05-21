package org.example

import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import org.example.org.example.database.DatabaseConfig
import org.example.org.example.routes.authRoutes

fun main() {
    DatabaseConfig.init()

    embeddedServer(Netty, port = 8080) {
        install(ContentNegotiation) {
            json()
        }
        routing {
            get("/") {
                call.respondText("API Gestión de Eventos funcionando!")
            }
            authRoutes()
        }
    }.start(wait = true)
}