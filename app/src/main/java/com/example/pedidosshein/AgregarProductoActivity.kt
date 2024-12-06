package com.example.pedidosshein

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.pedidosshein.data.database.AppDatabase
import com.example.pedidosshein.data.entities.Producto
import com.example.pedidosshein.databinding.ActivityAgregarProductoBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AgregarProductoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAgregarProductoBinding
    private lateinit var db: AppDatabase
    private var clienteId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAgregarProductoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = AppDatabase.getInstance(this)

        // Obtener el ID del cliente desde el Intent
        clienteId = intent.getIntExtra("CLIENTE_ID", -1)

        // Manejar la acción de agregar producto
        binding.btnAgregarProducto.setOnClickListener {
            val nombre = binding.etProductoNombre.text.toString()
            val precio = binding.etProductoPrecio.text.toString().toDoubleOrNull()

            if (nombre.isNotEmpty() && precio != null) {
                // Crear el nuevo producto y guardarlo en la base de datos
                val producto = Producto(clienteId = clienteId, nombre = nombre, precio = precio)

                GlobalScope.launch(Dispatchers.IO) {
                    db.productoDao().insertProducto(producto)
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@AgregarProductoActivity, "Producto agregado", Toast.LENGTH_SHORT).show()
                        finish() // Cerrar la actividad después de agregar el producto
                    }
                }
            } else {
                Toast.makeText(this, "Por favor, ingrese un nombre y un precio válido", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
