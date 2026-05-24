package com.grupo.gestioneventos

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.grupo.gestioneventos.databinding.ActivityEventoFormBinding
import com.grupo.gestioneventos.network.ApiClient
import kotlinx.coroutines.launch

class EventoFormActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEventoFormBinding
    private var eventoId: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEventoFormBinding.inflate(layoutInflater)
        setContentView(binding.root)

        eventoId = intent.getIntExtra("evento_id", 0)
        val editando = eventoId > 0

        binding.tvTituloFormulario.text = if (editando) "Actualizar evento" else "Crear evento"
        binding.btnGuardarEvento.text = if (editando) "Guardar cambios" else "Crear evento"

        binding.etTitulo.setText(intent.getStringExtra("evento_titulo").orEmpty())
        binding.etDescripcion.setText(intent.getStringExtra("evento_descripcion").orEmpty())
        binding.etFecha.setText(intent.getStringExtra("evento_fecha").orEmpty())
        binding.etHora.setText(intent.getStringExtra("evento_hora").orEmpty())
        binding.etUbicacion.setText(intent.getStringExtra("evento_ubicacion").orEmpty())

        binding.btnGuardarEvento.setOnClickListener { guardarEvento(editando) }
        binding.btnCancelar.setOnClickListener { finish() }
    }

    private fun guardarEvento(editando: Boolean) {
        val titulo = binding.etTitulo.text.toString().trim()
        val descripcion = binding.etDescripcion.text.toString().trim()
        val fecha = binding.etFecha.text.toString().trim()
        val hora = binding.etHora.text.toString().trim()
        val ubicacion = binding.etUbicacion.text.toString().trim()

        if (titulo.isEmpty() || descripcion.isEmpty() || fecha.isEmpty() || ubicacion.isEmpty()) {
            Toast.makeText(this, "Completa titulo, descripcion, fecha y ubicacion", Toast.LENGTH_SHORT).show()
            return
        }

        val fechaCompleta = if (hora.isBlank()) fecha else "$fecha $hora"
        val body = mapOf(
            "titulo" to titulo,
            "descripcion" to descripcion,
            "fecha" to fechaCompleta,
            "ubicacion" to ubicacion,
            "organizador_id" to Sesion.usuarioId(this)
        )

        lifecycleScope.launch {
            try {
                val response = if (editando) {
                    ApiClient.apiService.updateEvento(eventoId, body)
                } else {
                    ApiClient.apiService.createEvento(body)
                }

                if (response.isSuccessful) {
                    Toast.makeText(
                        this@EventoFormActivity,
                        if (editando) "Evento actualizado" else "Evento creado",
                        Toast.LENGTH_SHORT
                    ).show()
                    finish()
            } else {
                val error = response.errorBody()?.string().orEmpty()
                Toast.makeText(
                    this@EventoFormActivity,
                    "No se pudo guardar (${response.code()})",
                    Toast.LENGTH_LONG
                ).show()
                Log.e(TAG, "Error al guardar evento: ${response.code()} $error")
            }
        } catch (e: Exception) {
                Log.e(TAG, "Excepcion al guardar evento", e)
                Toast.makeText(
                    this@EventoFormActivity,
                    "Error: ${e.localizedMessage ?: "conexion"}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    companion object {
        private const val TAG = "EventoFormActivity"
    }
}
