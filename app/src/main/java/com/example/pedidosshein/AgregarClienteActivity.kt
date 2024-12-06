package com.example.pedidosshein

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.pedidosshein.data.database.AppDatabase
import com.example.pedidosshein.data.entities.Cliente
import com.example.pedidosshein.databinding.ActivityAgregarClienteBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AgregarClienteActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAgregarClienteBinding
    private lateinit var db: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAgregarClienteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = AppDatabase.getInstance(this)

        // Agregar el cliente
        binding.btnAgregarCliente.setOnClickListener {
            val nombre = binding.etNombreCliente.text.toString()
            val telefono = binding.etTelefonoCliente.text.toString()

            if (nombre.isNotEmpty() && telefono.isNotEmpty()) {
                val cliente = Cliente(0, nombre, telefono)
                GlobalScope.launch(Dispatchers.IO) {
                    db.clienteDao().insertCliente(cliente)
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@AgregarClienteActivity, "Cliente agregado", Toast.LENGTH_SHORT).show()
                        finish() // Cerrar la actividad después de agregar el cliente
                    }
                }
            } else {
                Toast.makeText(this, "Por favor, ingrese todos los datos", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
