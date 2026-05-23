package org.example.routes


import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.transactions.transaction

@Serializable
data class AsistenciaRequest(
    val evento_id: Int,
    val usuario_id: Int
)

@Serializable
data class Asistencia(
    val id: Int,
    val evento_id: Int,
    val usuario_id: Int,
    val fecha_confirmacion: String
)

@Serializable
data class AsistenciaResponse(
    val mensaje: String
)

fun Route.asistenciaRoutes() {

    post("/asistencias") {
        val request = call.receive<AsistenciaRequest>()

        transaction {
            exec("INSERT OR IGNORE INTO asistencias (evento_id, usuario_id) VALUES (${request.evento_id}, ${request.usuario_id})")
        }

        call.respond(HttpStatusCode.Created, AsistenciaResponse("Asistencia confirmada exitosamente"))
    }

    get("/asistencias/evento/{evento_id}") {
        val eventoId = call.parameters["evento_id"]?.toIntOrNull()
            ?: return@get call.respond(HttpStatusCode.BadRequest, "ID inválido")

        val asistencias = mutableListOf<Asistencia>()

        transaction {
            exec("SELECT * FROM asistencias WHERE evento_id = $eventoId") { rs ->
                while (rs.next()) {
                    asistencias.add(Asistencia(
                        id = rs.getInt("id"),
                        evento_id = rs.getInt("evento_id"),
                        usuario_id = rs.getInt("usuario_id"),
                        fecha_confirmacion = rs.getString("fecha_confirmacion") ?: ""
                    ))
                }
            }
        }

        call.respond(HttpStatusCode.OK, asistencias)
    }

    get("/historial/usuario/{usuario_id}") {
        val usuarioId = call.parameters["usuario_id"]?.toIntOrNull()
            ?: return@get call.respond(HttpStatusCode.BadRequest, "ID inválido")

        val eventos = mutableListOf<Evento>()

        transaction {
            exec("""
                SELECT e.* FROM eventos e
                INNER JOIN asistencias a ON e.id = a.evento_id
                WHERE a.usuario_id = $usuarioId
                ORDER BY e.fecha DESC
            """) { rs ->
                while (rs.next()) {
                    eventos.add(Evento(
                        id = rs.getInt("id"),
                        titulo = rs.getString("titulo"),
                        descripcion = rs.getString("descripcion"),
                        fecha = rs.getString("fecha"),
                        ubicacion = rs.getString("ubicacion"),
                        organizador_id = rs.getInt("organizador_id")
                    ))
                }
            }
        }

        call.respond(HttpStatusCode.OK, eventos)
    }

    delete("/asistencias") {
        val request = call.receive<AsistenciaRequest>()

        transaction {
            exec("DELETE FROM asistencias WHERE evento_id = ${request.evento_id} AND usuario_id = ${request.usuario_id}")
        }

        call.respond(HttpStatusCode.OK, AsistenciaResponse("Asistencia cancelada exitosamente"))
    }
}