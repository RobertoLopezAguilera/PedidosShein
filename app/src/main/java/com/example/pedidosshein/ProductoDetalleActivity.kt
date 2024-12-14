package com.example.pedidosshein

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.pedidosshein.data.database.AppDatabase
import com.example.pedidosshein.databinding.ActivityProductoDetalleBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ProductoDetalleActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProductoDetalleBinding
    private lateinit var db: AppDatabase
    private var productoId: Int = -1

    // Registrar el resultado de la edición del producto
    private val editarProductoLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                recargarProducto()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inicializar View Binding
        binding = ActivityProductoDetalleBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = AppDatabase.getInstance(this)

        // Obtener los datos del Intent
        productoId = intent.getIntExtra("PRODUCTO_ID", -1)
        if (productoId == -1) {
            Toast.makeText(this, "Error: Producto no encontrado", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        recargarProducto()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_producto_detalle, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_editar -> {
                editarProducto()
                return true
            }
            R.id.action_eliminar -> {
                eliminarProducto()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun recargarProducto() {
        GlobalScope.launch(Dispatchers.IO) {
            val producto = db.productoDao().getProductoById(productoId)
            withContext(Dispatchers.Main) {
                if (producto != null) {
                    binding.tvProductoNombre.text = producto.nombre
                    binding.tvProductoPrecio.text = "Precio: \$${"%.2f".format(producto.precio)}"
                } else {
                    Toast.makeText(this@ProductoDetalleActivity, "Producto no encontrado", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        }
    }

    private fun editarProducto() {
        GlobalScope.launch(Dispatchers.IO) {
            val producto = db.productoDao().getProductoById(productoId)
            withContext(Dispatchers.Main) {
                if (producto != null) {
                    val intent = Intent(this@ProductoDetalleActivity, EditarProductoActivity::class.java)
                    intent.putExtra("PRODUCTO_ID", productoId)
                    editarProductoLauncher.launch(intent) // Usar launcher para iniciar la actividad
                } else {
                    Toast.makeText(this@ProductoDetalleActivity, "Producto no encontrado", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun eliminarProducto() {
        AlertDialog.Builder(this)
            .setTitle("Eliminar Producto")
            .setMessage("¿Estás seguro de que deseas eliminar este producto?")
            .setPositiveButton("Sí") { _, _ ->
                GlobalScope.launch(Dispatchers.IO) {
                    try {
                        val productosEliminados = db.productoDao().deleteProductoById(productoId)
                        withContext(Dispatchers.Main) {
                            if (productosEliminados > 0) {
                                Toast.makeText(this@ProductoDetalleActivity, "Producto eliminado", Toast.LENGTH_SHORT).show()
                                finish()
                            } else {
                                Toast.makeText(this@ProductoDetalleActivity, "Error: Producto no encontrado", Toast.LENGTH_SHORT).show()
                            }
                        }
                    } catch (e: Exception) {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(this@ProductoDetalleActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
            .setNegativeButton("No", null)
            .show()
    }
}
