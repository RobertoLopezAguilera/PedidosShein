package com.example.pedidosshein

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.pedidosshein.data.adapters.ProductoAdapter
import com.example.pedidosshein.data.database.AppDatabase
import com.example.pedidosshein.databinding.ActivityProductosBinding
import com.example.pedidosshein.databinding.NavigationHeaderBinding
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
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

        // Configurar SwipeRefreshLayout
        binding.swipeRefreshLayout.setOnRefreshListener {
            cargarProductos()
        }

        // Configurar navegación inferior
        configurarBottomNavigation()

        // Cargar productos
        cargarProductos()

        binding.navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_perfil -> {
                    startActivity(Intent(this, PerfilActivity::class.java))
                    true
                }
                R.id.nav_cerrar_sesion -> {
                    logout()
                    true
                }
                else -> false
            }
        }
    }

    private fun logout() {
        val sharedPreferences = getSharedPreferences("UserSession", Context.MODE_PRIVATE)
        sharedPreferences.edit().clear().apply()
        FirebaseAuth.getInstance().signOut()
        Toast.makeText(this, "Sesión cerrada con éxito", Toast.LENGTH_SHORT).show()

        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        finish()
    }

    private fun setupNavigationHeader(userEmail: String) {
        val headerBinding = NavigationHeaderBinding.bind(binding.navigationView.getHeaderView(0))
        headerBinding.userEmail.text = userEmail
    }

    private fun cargarProductos() {
        // Mostrar indicador de carga
        binding.swipeRefreshLayout.isRefreshing = true

        lifecycleScope.launch(Dispatchers.IO) {
            val productos = db.productoDao().getAllProductos()
            withContext(Dispatchers.Main) {
                // Configurar adaptador si no está inicializado
                if (!::productoAdapter.isInitialized) {
                    productoAdapter = ProductoAdapter(productos) { producto ->
                        mostrarDetallesCliente(producto.clienteId) // Paso 1: Navegar a detalles del cliente
                    }
                    binding.rvProductos.adapter = productoAdapter
                } else {
                    // Actualizar productos si ya existe el adaptador
                    productoAdapter.setProductos(productos)
                }
                // Ocultar indicador de carga
                binding.swipeRefreshLayout.isRefreshing = false
            }
        }
    }

    private fun mostrarDetallesCliente(clienteId: Int) {
        val intent = Intent(this, ClienteDetalleActivity::class.java)
        intent.putExtra("CLIENTE_ID", clienteId) // Paso 2: Enviar ID del cliente
        startActivity(intent) // Paso 3: Navegar a ClienteDetalleActivity
    }

    private fun configurarBottomNavigation() {
        binding.bottomNavigationView.selectedItemId = R.id.menu_productos

        binding.bottomNavigationView.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.menu_clientes -> {
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                    true
                }
                R.id.menu_productos -> true
                R.id.menu_abonos -> {
                    startActivity(Intent(this, AbonoActivity::class.java))
                    finish()
                    true
                }
                else -> false
            }
        }
    }
}
