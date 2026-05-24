package com.grupo.gestioneventos

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.textfield.TextInputLayout
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import com.grupo.gestioneventos.databinding.ActivityEventoFormBinding
import com.grupo.gestioneventos.network.ApiClient
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class EventoFormActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEventoFormBinding
    private var eventoId: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEventoFormBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.rootFormulario.applySystemBarsAndImePadding(applyTop = true, applyBottom = true)
        binding.rootFormulario.keepFocusedViewVisible(binding.etUbicacion)

        eventoId = intent.getIntExtra("evento_id", 0)
        val editando = eventoId > 0

        binding.tvTituloFormulario.text = if (editando) "Actualizar evento" else "Crear evento"
        binding.btnGuardarEvento.text = if (editando) "Guardar cambios" else "Crear evento"

        val fechaEvento = intent.getStringExtra("evento_fecha").orEmpty()
        val fechaPartes = fechaEvento.split(" ", limit = 2)

        binding.etTitulo.setText(intent.getStringExtra("evento_titulo").orEmpty())
        binding.etDescripcion.setText(intent.getStringExtra("evento_descripcion").orEmpty())
        binding.etFecha.setText(fechaPartes.firstOrNull().orEmpty())
        binding.etHora.setText(intent.getStringExtra("evento_hora").orEmpty().ifBlank { fechaPartes.getOrNull(1).orEmpty() })
        binding.etUbicacion.setText(intent.getStringExtra("evento_ubicacion").orEmpty())

        binding.etFecha.setOnClickListener { mostrarCalendario() }
        binding.tilFecha.setEndIconOnClickListener { mostrarCalendario() }
        binding.etHora.setOnClickListener { mostrarReloj() }
        binding.tilHora.setEndIconOnClickListener { mostrarReloj() }
        binding.btnGuardarEvento.setOnClickListener { guardarEvento(editando) }
        binding.btnCancelar.setOnClickListener { finish() }
    }

    private fun guardarEvento(editando: Boolean) {
        val titulo = binding.etTitulo.text.toString().trim()
        val descripcion = binding.etDescripcion.text.toString().trim()
        val fecha = binding.etFecha.text.toString().trim()
        val hora = binding.etHora.text.toString().trim()
        val ubicacion = binding.etUbicacion.text.toString().trim()

        if (!validarCampos(titulo, descripcion, fecha, ubicacion)) {
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

    private fun validarCampos(
        titulo: String,
        descripcion: String,
        fecha: String,
        ubicacion: String
    ): Boolean {
        marcarRequerido(binding.tilTitulo, titulo, "El titulo es requerido")
        marcarRequerido(binding.tilDescripcion, descripcion, "La descripcion es requerida")
        marcarRequerido(binding.tilFecha, fecha, "La fecha es requerida")
        marcarRequerido(binding.tilUbicacion, ubicacion, "La ubicacion es requerida")
        return titulo.isNotEmpty() && descripcion.isNotEmpty() && fecha.isNotEmpty() && ubicacion.isNotEmpty()
    }

    private fun marcarRequerido(layout: TextInputLayout, value: String, mensaje: String) {
        layout.error = if (value.isBlank()) mensaje else null
    }

    private fun mostrarCalendario() {
        val picker = MaterialDatePicker.Builder.datePicker()
            .setTitleText("Selecciona la fecha")
            .setSelection(fechaSeleccionadaMillis() ?: MaterialDatePicker.todayInUtcMilliseconds())
            .build()

        picker.addOnPositiveButtonClickListener { millis ->
            binding.etFecha.setText(formatoFecha.format(Date(millis)))
            binding.tilFecha.error = null
        }
        picker.show(supportFragmentManager, "fecha_evento")
    }

    private fun mostrarReloj() {
        val partes = binding.etHora.text.toString().split(":")
        val ahora = Calendar.getInstance()
        val horaInicial = partes.getOrNull(0)?.toIntOrNull() ?: ahora.get(Calendar.HOUR_OF_DAY)
        val minutoInicial = partes.getOrNull(1)?.toIntOrNull() ?: ahora.get(Calendar.MINUTE)

        val picker = MaterialTimePicker.Builder()
            .setTitleText("Selecciona la hora")
            .setTimeFormat(TimeFormat.CLOCK_24H)
            .setHour(horaInicial)
            .setMinute(minutoInicial)
            .build()

        picker.addOnPositiveButtonClickListener {
            binding.etHora.setText(String.format(Locale.US, "%02d:%02d", picker.hour, picker.minute))
        }
        picker.show(supportFragmentManager, "hora_evento")
    }

    private fun fechaSeleccionadaMillis(): Long? =
        binding.etFecha.text?.toString()?.takeIf { it.isNotBlank() }?.let { fecha ->
            runCatching { formatoFecha.parse(fecha)?.time }.getOrNull()
        }

    companion object {
        private const val TAG = "EventoFormActivity"
        private val formatoFecha = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
    }
}
