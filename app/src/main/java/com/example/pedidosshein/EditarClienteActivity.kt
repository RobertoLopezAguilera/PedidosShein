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
import androidx.appcompat.app.AlertDialog

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
            guardarCliente(clienteId)
        }

        binding.btnEliminar.setOnClickListener {
            mostrarDialogoConfirmacion()
        }
    }

    private fun cargarCliente(clienteId: Int) {
        GlobalScope.launch(Dispatchers.IO) {
            val cliente = db.clienteDao().getClienteById(clienteId)
            withContext(Dispatchers.Main) {
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
            db.clienteDao().updateCliente(cliente)
            withContext(Dispatchers.Main) {
                Toast.makeText(this@EditarClienteActivity, "Cliente actualizado", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private fun mostrarDialogoConfirmacion() {
        AlertDialog.Builder(this)
            .setTitle("Eliminar Cliente")
            .setMessage("¿Estás seguro de que deseas eliminar este cliente? Se eliminarán todos los datos asociados.")
            .setPositiveButton("Sí") { _, _ -> eliminarCliente() }
            .setNegativeButton("No", null)
            .show()
    }

    private fun eliminarCliente() {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                db.productoDao().deleteProductoByClienteId(clienteId)
                db.abonoDao().deleteAbonoByClienteId(clienteId)
                db.clienteDao().deleteClienteById(clienteId)

                withContext(Dispatchers.Main) {
                    Toast.makeText(this@EditarClienteActivity, "Cliente eliminado", Toast.LENGTH_SHORT).show()
                    finish() // Cerrar la actividad y regresar a la anterior
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@EditarClienteActivity, "Error al eliminar cliente: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
