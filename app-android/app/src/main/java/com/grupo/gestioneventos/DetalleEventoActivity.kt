package com.grupo.gestioneventos

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.lifecycleScope
import com.grupo.gestioneventos.databinding.ActivityDetalleEventoBinding
import com.grupo.gestioneventos.network.ApiClient
import kotlinx.coroutines.launch

class DetalleEventoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetalleEventoBinding
    private var eventoId = 0
    private var titulo = ""
    private var descripcion = ""
    private var fecha = ""
    private var ubicacion = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetalleEventoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        eventoId = intent.getIntExtra("evento_id", 0)
        titulo = intent.getStringExtra("evento_titulo").orEmpty()
        descripcion = intent.getStringExtra("evento_descripcion").orEmpty()
        fecha = intent.getStringExtra("evento_fecha").orEmpty()
        ubicacion = intent.getStringExtra("evento_ubicacion").orEmpty()

        binding.tvTitulo.text = titulo
        binding.tvDescripcion.text = descripcion
        binding.tvFecha.text = "Fecha: $fecha"
        binding.tvUbicacion.text = "Ubicacion: $ubicacion"

        crearCanalNotificaciones()
        mostrarComentarios()

        binding.btnAsistir.setOnClickListener { confirmarAsistencia() }
        binding.btnCompartir.setOnClickListener { compartirEvento() }
        binding.btnEditar.setOnClickListener { abrirEdicion() }
        binding.btnEliminar.setOnClickListener { confirmarEliminacion() }
        binding.btnGuardarComentario.setOnClickListener { guardarComentario() }
        binding.btnVolver.setOnClickListener { finish() }
    }

    private fun confirmarAsistencia() {
        lifecycleScope.launch {
            try {
                val response = ApiClient.apiService.confirmarAsistencia(
                    mapOf("evento_id" to eventoId, "usuario_id" to Sesion.usuarioId(this@DetalleEventoActivity))
                )
                if (response.isSuccessful) {
                    Toast.makeText(this@DetalleEventoActivity, "Asistencia confirmada", Toast.LENGTH_SHORT).show()
                    mostrarNotificacionRecordatorio()
                } else {
                    Toast.makeText(this@DetalleEventoActivity, "Ya confirmaste asistencia", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@DetalleEventoActivity, "Error de conexion", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun compartirEvento() {
        val texto = """
            Te comparto este evento comunitario:
            $titulo
            Fecha: $fecha
            Ubicacion: $ubicacion
            $descripcion
        """.trimIndent()

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, titulo)
            putExtra(Intent.EXTRA_TEXT, texto)
        }
        startActivity(Intent.createChooser(intent, "Compartir evento"))
    }

    private fun abrirEdicion() {
        val intent = Intent(this, EventoFormActivity::class.java)
        intent.putExtra("evento_id", eventoId)
        intent.putExtra("evento_titulo", titulo)
        intent.putExtra("evento_descripcion", descripcion)
        intent.putExtra("evento_fecha", fecha)
        intent.putExtra("evento_ubicacion", ubicacion)
        startActivity(intent)
    }

    private fun confirmarEliminacion() {
        AlertDialog.Builder(this)
            .setTitle("Eliminar evento")
            .setMessage("Esta accion eliminara el evento para todos los usuarios.")
            .setPositiveButton("Eliminar") { _, _ -> eliminarEvento() }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun eliminarEvento() {
        lifecycleScope.launch {
            try {
                val response = ApiClient.apiService.deleteEvento(eventoId)
                if (response.isSuccessful) {
                    Toast.makeText(this@DetalleEventoActivity, "Evento eliminado", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this@DetalleEventoActivity, "No se pudo eliminar", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@DetalleEventoActivity, "Error de conexion", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun guardarComentario() {
        val comentario = binding.etComentario.text.toString().trim()
        val rating = binding.ratingEvento.rating.toInt()

        if (comentario.isEmpty() || rating == 0) {
            Toast.makeText(this, "Agrega comentario y calificacion", Toast.LENGTH_SHORT).show()
            return
        }

        val prefs = getSharedPreferences("comentarios_eventos", Context.MODE_PRIVATE)
        val key = "evento_$eventoId"
        val previo = prefs.getString(key, "").orEmpty()
        val nuevo = "Calificacion: $rating/5\n$comentario"
        prefs.edit().putString(key, listOf(previo, nuevo).filter { it.isNotBlank() }.joinToString("\n\n")).apply()

        binding.etComentario.text?.clear()
        binding.ratingEvento.rating = 0f
        mostrarComentarios()
        Toast.makeText(this, "Comentario guardado", Toast.LENGTH_SHORT).show()
    }

    private fun mostrarComentarios() {
        val comentarios = getSharedPreferences("comentarios_eventos", Context.MODE_PRIVATE)
            .getString("evento_$eventoId", "")
            .orEmpty()
        binding.tvComentarios.text = comentarios.ifBlank { "Sin comentarios todavia." }
    }

    private fun crearCanalNotificaciones() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Recordatorios de eventos",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }

    private fun mostrarNotificacionRecordatorio() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 200)
            return
        }

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Asistencia confirmada")
            .setContentText("Te recordaremos cambios del evento: $titulo")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        NotificationManagerCompat.from(this).notify(eventoId, notification)
    }

    companion object {
        private const val CHANNEL_ID = "eventos_recordatorios"
    }
}