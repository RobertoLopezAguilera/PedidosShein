package com.example.pedidosshein

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.pedidosshein.data.adapters.ClienteAdapter
import com.example.pedidosshein.data.database.AppDatabase
import com.example.pedidosshein.data.entities.Cliente
import com.example.pedidosshein.data.entities.toAbono
import com.example.pedidosshein.data.entities.toCliente
import com.example.pedidosshein.data.entities.toMap
import com.example.pedidosshein.data.entities.toProducto
import com.example.pedidosshein.databinding.ActivityMainBinding
import com.example.pedidosshein.databinding.NavigationHeaderBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var clienteAdapter: ClienteAdapter
    private lateinit var db: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme)
        super.onCreate(savedInstanceState)

        // Inicializar View Binding
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = AppDatabase.getInstance(this)

        val sharedPreferences = getSharedPreferences("UserSession", Context.MODE_PRIVATE)
        val userEmail = sharedPreferences.getString("USER_EMAIL", null)
        val currentUser = FirebaseAuth.getInstance().currentUser

        val userId = currentUser?.uid

        if (currentUser == null || userEmail.isNullOrEmpty()) {
            // Redirigir al LoginActivity si no hay sesión
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
            return
        }

        // Mostrar el correo en el NavigationHeader
        setupNavigationHeader(userEmail)

        // Configurar RecyclerView
        binding.rvClientes.layoutManager = LinearLayoutManager(this)
        clienteAdapter = ClienteAdapter(emptyList())
        binding.rvClientes.adapter = clienteAdapter

        // Configurar SwipeRefreshLayout
        binding.swipeRefreshLayout.setOnRefreshListener {
            cargarClientes()
        }

        // Configurar navegación inferior
        binding.bottomNavigationView.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.menu_clientes -> true
                R.id.menu_productos -> {
                    startActivity(Intent(this, ProductosActivity::class.java))
                    true
                }
                R.id.menu_abonos -> {
                    startActivity(Intent(this, AbonoActivity::class.java))
                    true
                }
                else -> false
            }
        }

        binding.bottomNavigationView.selectedItemId = R.id.menu_clientes

        // Configurar NavigationView
        binding.navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_perfil -> {0
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
        cargarClientes()
    }

    private fun setupNavigationHeader(userEmail: String) {
        val headerBinding = NavigationHeaderBinding.bind(binding.navigationView.getHeaderView(0))
        headerBinding.userEmail.text = userEmail
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)

        val searchItem = menu?.findItem(R.id.action_search)
        val searchView = searchItem?.actionView as? androidx.appcompat.widget.SearchView

        searchView?.setOnQueryTextListener(object : androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = false

            override fun onQueryTextChange(newText: String?): Boolean {
                filtrarClientes(newText ?: "")
                return true
            }
        })

        return true
    }
    //Respaldo de datos


    private fun filtrarClientes(query: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            val clientesFiltrados = if (query.isEmpty()) {
                db.clienteDao().getAllClientes()
            } else {
                db.clienteDao().buscarClientes(query.lowercase())
            }

            withContext(Dispatchers.Main) {
                actualizarLista(clientesFiltrados)
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.item_agregar_cliente -> {
                startActivity(Intent(this, AgregarClienteActivity::class.java))
                return true
            }
        }
        return super.onOptionsItemSelected(item)
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

    private fun cargarClientes() {
        lifecycleScope.launch(Dispatchers.IO) {
            val clientes = db.clienteDao().getAllClientes()
            withContext(Dispatchers.Main) {
                actualizarLista(clientes)
                binding.swipeRefreshLayout.isRefreshing = false
            }
        }
    }

    private fun actualizarLista(clientes: List<Cliente>) {
        clienteAdapter.setClientes(clientes)
        clienteAdapter.setOnItemClickListener { cliente ->
            val intent = Intent(this, ClienteDetalleActivity::class.java)
            intent.putExtra("CLIENTE_ID", cliente.id)
            startActivity(intent)
        }
    }
}
