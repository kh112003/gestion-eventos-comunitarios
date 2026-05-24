package com.grupo.gestioneventos

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.grupo.gestioneventos.databinding.ActivityEventosBinding
import com.grupo.gestioneventos.network.ApiClient
import kotlinx.coroutines.launch

class EventosActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEventosBinding
    private val eventos = mutableListOf<Map<String, Any>>()
    private lateinit var adapter: EventoAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEventosBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        adapter = EventoAdapter(eventos) { evento ->
            val intent = Intent(this, DetalleEventoActivity::class.java)
            intent.putExtra("evento_id", evento.intValue("id"))
            intent.putExtra("evento_titulo", evento.textValue("titulo"))
            intent.putExtra("evento_descripcion", evento.textValue("descripcion"))
            intent.putExtra("evento_fecha", evento.textValue("fecha"))
            intent.putExtra("evento_ubicacion", evento.textValue("ubicacion"))
            startActivity(intent)
        }

        binding.rvEventos.layoutManager = LinearLayoutManager(this)
        binding.rvEventos.adapter = adapter

        binding.fabAddEvento.setOnClickListener {
            startActivity(Intent(this, EventoFormActivity::class.java))
        }

        binding.btnHistorial.setOnClickListener {
            startActivity(Intent(this, HistorialActivity::class.java))
        }

        binding.btnCerrarSesion.setOnClickListener {
            Sesion.cerrar(this)
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        cargarEventos()
    }

    override fun onResume() {
        super.onResume()
        cargarEventos()
    }

    private fun cargarEventos() {
        lifecycleScope.launch {
            try {
                val response = ApiClient.apiService.getEventos()
                if (response.isSuccessful) {
                    eventos.clear()
                    response.body()?.let { eventos.addAll(it) }
                    adapter.notifyDataSetChanged()
                }
            } catch (e: Exception) {
                Toast.makeText(this@EventosActivity, "Error al cargar eventos", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
