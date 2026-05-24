package com.grupo.gestioneventos

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.grupo.gestioneventos.databinding.ActivityHistorialBinding
import com.grupo.gestioneventos.network.ApiClient
import kotlinx.coroutines.launch

class HistorialActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHistorialBinding
    private val eventos = mutableListOf<Map<String, Any>>()
    private lateinit var adapter: EventoAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHistorialBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.rootHistorial.applySystemBarPadding(applyTop = true, applyBottom = true)

        adapter = EventoAdapter(eventos) { evento ->
            val intent = Intent(this, DetalleEventoActivity::class.java)
            intent.putExtra("evento_id", evento.intValue("id"))
            intent.putExtra("evento_titulo", evento.textValue("titulo"))
            intent.putExtra("evento_descripcion", evento.textValue("descripcion"))
            intent.putExtra("evento_fecha", evento.textValue("fecha"))
            intent.putExtra("evento_ubicacion", evento.textValue("ubicacion"))
            startActivity(intent)
        }

        binding.rvHistorial.layoutManager = LinearLayoutManager(this)
        binding.rvHistorial.adapter = adapter
        binding.btnVolver.setOnClickListener { finish() }

        cargarHistorial()
    }

    private fun cargarHistorial() {
        lifecycleScope.launch {
            try {
                val response = ApiClient.apiService.getHistorial(Sesion.usuarioId(this@HistorialActivity))
                if (response.isSuccessful) {
                    eventos.clear()
                    response.body()?.let { eventos.addAll(it) }
                    adapter.notifyDataSetChanged()
                    binding.tvEstado.text = if (eventos.isEmpty()) {
                        "Aun no tienes eventos confirmados."
                    } else {
                        "Eventos a los que confirmaste asistencia"
                    }
                } else {
                    Toast.makeText(this@HistorialActivity, "No se pudo cargar el historial", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@HistorialActivity, "Error de conexion", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
