package com.example.pedidosshein

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.pedidosshein.data.adapters.ProductoAdapter
import com.example.pedidosshein.data.database.AppDatabase
import com.example.pedidosshein.data.entities.Producto
import com.example.pedidosshein.databinding.ActivityProductosBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ProductosActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProductosBinding
    private lateinit var productoAdapter: ProductoAdapter
    private lateinit var db: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inicializar View Binding
        binding = ActivityProductosBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = AppDatabase.getInstance(this)

        // Configurar RecyclerView
        binding.rvProductos.layoutManager = LinearLayoutManager(this)
        productoAdapter = ProductoAdapter(emptyList()) { producto ->
            onProductoClick(producto)
        }
        binding.rvProductos.adapter = productoAdapter

        // Configurar SwipeRefreshLayout
        binding.swipeRefreshLayout.setOnRefreshListener {
            cargarProductos() // Cargar productos al hacer "swipe" para refrescar
        }

        // Configurar navegación inferior
        configurarBottomNavigation()

        // Cargar productos
        cargarProductos()
    }

    private fun cargarProductos() {
        // Mostrar el indicador de carga mientras se obtienen los productos
        binding.swipeRefreshLayout.isRefreshing = true

        // Cargar productos de la base de datos en segundo plano
        GlobalScope.launch(Dispatchers.IO) {
            val productos = db.productoDao().getAllProductos() // Método para obtener productos
            withContext(Dispatchers.Main) {
                // Actualizar el adapter con la lista de productos
                productoAdapter.setProductos(productos)
                // Desactivar el indicador de carga
                binding.swipeRefreshLayout.isRefreshing = false
            }
        }
    }

    private fun onProductoClick(producto: Producto) {
        // Acción cuando se hace clic en un producto
        val intent = Intent(this, ProductoDetalleActivity::class.java)
        intent.putExtra("productoId", producto.id)
        startActivity(intent)
    }

    private fun configurarBottomNavigation() {
        binding.bottomNavigationView.selectedItemId = R.id.menu_productos // Seleccionar "Productos"

        binding.bottomNavigationView.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.menu_clientes -> {
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    finish() // Opcional: cerrar la actividad actual para evitar acumulación
                    true
                }
                R.id.menu_productos -> {
                    // Ya estamos en la actividad de productos
                    true
                }
                R.id.menu_abonos -> {
                    val intent = Intent(this, AbonoActivity::class.java)
                    startActivity(intent)
                    finish() // Opcional
                    true
                }
                else -> false
            }
        }
    }
}
