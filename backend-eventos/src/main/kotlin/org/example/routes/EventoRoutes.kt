package org.example.routes


import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.transactions.transaction

@Serializable
data class Evento(
    val id: Int,
    val titulo: String,
    val descripcion: String,
    val fecha: String,
    val ubicacion: String,
    val organizador_id: Int
)

@Serializable
data class EventoRequest(
    val titulo: String,
    val descripcion: String,
    val fecha: String,
    val ubicacion: String,
    val organizador_id: Int
)

@Serializable
data class EventoResponse(
    val mensaje: String,
    val id: Int? = null
)

fun Route.eventoRoutes() {

    post("/eventos") {
        val request = call.receive<EventoRequest>()
        var nuevoId = 0

        transaction {
            exec("INSERT INTO eventos (titulo, descripcion, fecha, ubicacion, organizador_id) VALUES ('${request.titulo}', '${request.descripcion}', '${request.fecha}', '${request.ubicacion}', ${request.organizador_id})")
            exec("SELECT last_insert_rowid() as id") { rs ->
                if (rs.next()) nuevoId = rs.getInt("id")
            }
        }

        call.respond(HttpStatusCode.Created, EventoResponse("Evento creado exitosamente", nuevoId))
    }

    get("/eventos") {
        val eventos = mutableListOf<Evento>()

        transaction {
            exec("SELECT * FROM eventos ORDER BY fecha ASC") { rs ->
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

    get("/eventos/{id}") {
        val id = call.parameters["id"]?.toIntOrNull()
            ?: return@get call.respond(HttpStatusCode.BadRequest, "ID inválido")

        var evento: Evento? = null

        transaction {
            exec("SELECT * FROM eventos WHERE id = $id") { rs ->
                if (rs.next()) {
                    evento = Evento(
                        id = rs.getInt("id"),
                        titulo = rs.getString("titulo"),
                        descripcion = rs.getString("descripcion"),
                        fecha = rs.getString("fecha"),
                        ubicacion = rs.getString("ubicacion"),
                        organizador_id = rs.getInt("organizador_id")
                    )
                }
            }
        }

        if (evento == null) {
            call.respond(HttpStatusCode.NotFound, "Evento no encontrado")
        } else {
            call.respond(HttpStatusCode.OK, evento!!)
        }
    }

    put("/eventos/{id}") {
        val id = call.parameters["id"]?.toIntOrNull()
            ?: return@put call.respond(HttpStatusCode.BadRequest, "ID inválido")
        val request = call.receive<EventoRequest>()

        transaction {
            exec("UPDATE eventos SET titulo='${request.titulo}', descripcion='${request.descripcion}', fecha='${request.fecha}', ubicacion='${request.ubicacion}' WHERE id=$id")
        }

        call.respond(HttpStatusCode.OK, EventoResponse("Evento actualizado exitosamente"))
    }

    delete("/eventos/{id}") {
        val id = call.parameters["id"]?.toIntOrNull()
            ?: return@delete call.respond(HttpStatusCode.BadRequest, "ID inválido")

        transaction {
            exec("DELETE FROM eventos WHERE id = $id")
        }

        call.respond(HttpStatusCode.OK, EventoResponse("Evento eliminado exitosamente"))
    }
}