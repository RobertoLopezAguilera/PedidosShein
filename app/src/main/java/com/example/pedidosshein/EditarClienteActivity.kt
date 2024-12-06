package com.example.pedidosshein

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.pedidosshein.data.database.AppDatabase
import com.example.pedidosshein.data.entities.Cliente
import com.example.pedidosshein.databinding.ActivityEditarClienteBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class EditarClienteActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditarClienteBinding
    private lateinit var db: AppDatabase
    private var clienteId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityEditarClienteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = AppDatabase.getInstance(this)

        clienteId = intent.getIntExtra("CLIENTE_ID", -1)

        if (clienteId != -1) {
            cargarCliente(clienteId)
        }

        binding.btnGuardar.setOnClickListener {
            // Guardar los cambios del cliente en la base de datos
            guardarCliente(clienteId)
        }
    }

    private fun cargarCliente(clienteId: Int) {
        GlobalScope.launch(Dispatchers.IO) {
            val cliente = db.clienteDao().getClienteById(clienteId)
            withContext(Dispatchers.Main) {
                // Cargar los datos del cliente en los campos
                binding.etNombre.setText(cliente?.nombre ?: "")
                binding.etTelefono.setText(cliente?.telefono ?: "")
            }
        }
    }

    private fun guardarCliente(clienteId: Int) {
        val nombre = binding.etNombre.text.toString()
        val telefono = binding.etTelefono.text.toString()

        val cliente = Cliente(id = clienteId, nombre = nombre, telefono = telefono)
        GlobalScope.launch(Dispatchers.IO) {
            db.clienteDao().updateAbono(cliente)
            withContext(Dispatchers.Main) {
                finish() // Regresar a la actividad anterior
            }
        }
    }
}
