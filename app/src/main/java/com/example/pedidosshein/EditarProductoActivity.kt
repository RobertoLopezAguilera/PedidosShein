package com.example.pedidosshein

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.pedidosshein.data.database.AppDatabase
import com.example.pedidosshein.data.entities.Producto
import com.example.pedidosshein.databinding.ActivityEditarProductoBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class EditarProductoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditarProductoBinding
    private lateinit var db: AppDatabase
    private var productoId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inicializar View Binding
        binding = ActivityEditarProductoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = AppDatabase.getInstance(this)

        // Obtener el ID del producto del Intent
        productoId = intent.getIntExtra("PRODUCTO_ID", -1)

        if (productoId != -1) {
            cargarProducto()
        } else {
            Toast.makeText(this, "Error: Producto no encontrado", Toast.LENGTH_SHORT).show()
            finish()
        }

        // Guardar cambios
        binding.btnGuardarProducto.setOnClickListener {
            guardarCambios()
        }
    }

    private fun cargarProducto() {
        GlobalScope.launch(Dispatchers.IO) {
            val producto = db.productoDao().getProductoById(productoId)
            producto?.let {
                withContext(Dispatchers.Main) {
                    binding.etNombreProducto.setText(it.nombre)
                    binding.etPrecioProducto.setText(it.precio.toString())
                }
            }
        }
    }

    private fun guardarCambios() {
        val nuevoNombre = binding.etNombreProducto.text.toString().trim()
        val nuevoPrecioTexto = binding.etPrecioProducto.text.toString().trim()
        val nuevoPrecio = nuevoPrecioTexto.toDoubleOrNull()

        if (nuevoNombre.isEmpty()) {
            Toast.makeText(this, "El nombre no puede estar vacío", Toast.LENGTH_SHORT).show()
            return
        }

        if (nuevoPrecio == null || nuevoPrecio <= 0.0) {
            Toast.makeText(this, "El precio debe ser un número válido mayor a 0", Toast.LENGTH_SHORT).show()
            return
        }

        GlobalScope.launch(Dispatchers.IO) {
            try {
                val producto = Producto(
                    id = productoId,
                    clienteId = db.productoDao().getProductoById(productoId)?.clienteId ?: 0,
                    nombre = nuevoNombre,
                    precio = nuevoPrecio
                )
                db.productoDao().updateProducto(producto)

                withContext(Dispatchers.Main) {
                    Toast.makeText(this@EditarProductoActivity, "Producto actualizado correctamente", Toast.LENGTH_SHORT).show()

                    // Indicar que el producto fue actualizado
                    setResult(RESULT_OK)
                    finish()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@EditarProductoActivity, "Error al actualizar el producto: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

}
