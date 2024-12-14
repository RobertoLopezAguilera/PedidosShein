package com.example.pedidosshein

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.pedidosshein.data.adapters.AbonoAdapter
import com.example.pedidosshein.data.database.AppDatabase
import com.example.pedidosshein.databinding.ActivityAbonoBinding
import com.example.pedidosshein.databinding.NavigationHeaderBinding
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AbonoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAbonoBinding
    private lateinit var abonoAdapter: AbonoAdapter
    private lateinit var db: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inicializar View Binding
        binding = ActivityAbonoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = AppDatabase.getInstance(this)

        // Configurar RecyclerView
        binding.rvAbonos.layoutManager = LinearLayoutManager(this)

        // Configurar SwipeRefreshLayout
        binding.swipeRefreshLayout.setOnRefreshListener {
            cargarAbonos()
        }

        // Configurar navegación inferior
        configurarBottomNavigation()

        // Configurar NavigationView
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

        cargarAbonos()
    }

    private fun cargarAbonos() {
        // Mostrar indicador de carga
        binding.swipeRefreshLayout.isRefreshing = true

        lifecycleScope.launch(Dispatchers.IO) {
            val abonos = db.abonoDao().getAllAbonos()
            withContext(Dispatchers.Main) {
                // Configurar adaptador si no está inicializado
                if (!::abonoAdapter.isInitialized) {
                    abonoAdapter = AbonoAdapter(abonos) { abono ->
                        mostrarDetallesCliente(abono.clienteId) // Paso 1: Navegar a detalles del cliente
                    }
                    binding.rvAbonos.adapter = abonoAdapter
                } else {
                    // Actualizar abonos si ya existe el adaptador
                    abonoAdapter.setAbonos(abonos)
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
        binding.bottomNavigationView.selectedItemId = R.id.menu_abonos

        binding.bottomNavigationView.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.menu_clientes -> {
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                    true
                }
                R.id.menu_productos -> {
                    startActivity(Intent(this, ProductosActivity::class.java))
                    finish()
                    true
                }
                R.id.menu_abonos -> true
                else -> false
            }
        }
    }

    private fun logout() {
        val sharedPreferences = getSharedPreferences("UserSession", Context.MODE_PRIVATE)
        sharedPreferences.edit().clear().apply()
        FirebaseAuth.getInstance().signOut()
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        finish()
    }
}
