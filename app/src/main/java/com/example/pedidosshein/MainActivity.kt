package com.example.pedidosshein

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.pedidosshein.data.adapters.ClienteAdapter
import com.example.pedidosshein.data.database.AppDatabase
import com.example.pedidosshein.data.entities.Abono
import com.example.pedidosshein.data.entities.Cliente
import com.example.pedidosshein.data.entities.Producto
import com.example.pedidosshein.databinding.ActivityMainBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var clienteAdapter: ClienteAdapter
    private lateinit var db: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inicializar View Binding
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = AppDatabase.getInstance(this)
        insertarDatosDeEjemplo()

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
                R.id.menu_clientes -> {
                    // Ya estás en MainActivity, así que no es necesario hacer nada
                    true
                }
                R.id.menu_productos -> {
                    // Abrir actividad para productos
                    val intent = Intent(this, ProductosActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.menu_abonos -> {
                    // Abrir actividad para abonos
                    val intent = Intent(this, AbonoActivity::class.java)
                    startActivity(intent)
                    true
                }
                else -> false
            }
        }


        // Establecer como seleccionada la sección de Clientes al inicio
        binding.bottomNavigationView.selectedItemId = R.id.menu_clientes
        cargarClientes()
    }

    // Inflar el menú
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)

        // Configurar el SearchView
        val searchItem = menu?.findItem(R.id.action_search)
        val searchView = searchItem?.actionView as? androidx.appcompat.widget.SearchView

        searchView?.setOnQueryTextListener(object : androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filtrarClientes(newText ?: "")
                return true
            }
        })

        return true
    }

    private fun filtrarClientes(query: String) {
        GlobalScope.launch(Dispatchers.IO) {
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

    // Manejar la opción del menú para agregar un cliente
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.item_agregar_cliente -> {
                // Abrir actividad para agregar un nuevo cliente
                val intent = Intent(this, AgregarClienteActivity::class.java)
                startActivity(intent)
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    private fun cargarClientes() {
        GlobalScope.launch(Dispatchers.IO) {
            val clientes = db.clienteDao().getAllClientes()
            withContext(Dispatchers.Main) {
                actualizarLista(clientes)
                binding.swipeRefreshLayout.isRefreshing = false // Detener animación de actualización
            }
        }
    }

    private fun actualizarLista(clientes: List<Cliente>) {
        clienteAdapter.setClientes(clientes)

        // Configurar click listener para cada cliente
        clienteAdapter.setOnItemClickListener { cliente ->
            // Al hacer clic en un cliente, navegar a ClienteDetalleActivity
            val intent = Intent(this, ClienteDetalleActivity::class.java)
            intent.putExtra("CLIENTE_ID", cliente.id)
            startActivity(intent)
        }
    }

    // Método para insertar datos de ejemplo
    private fun insertarDatosDeEjemplo() {
        GlobalScope.launch(Dispatchers.IO) {
            val clientesExistentes = db.clienteDao().getAllClientes()
            if (clientesExistentes.isEmpty()) {
                // Crear 2 clientes
                val cliente1 = Cliente(0, "Roberto López", "123456789")
                val cliente2 = Cliente(0, "Clarisa Rodriguez", "987654321")
                db.clienteDao().insertCliente(cliente1, cliente2)

                // Crear productos
                val producto1 = Producto(0, "Zapatos", 500.0, 1)
                val producto2 = Producto(0, "Camisa", 300.0, 1)
                val producto3 = Producto(0, "Pantalón", 400.0, 1)
                val producto4 = Producto(0, "Pantalón", 400.0, 2)
                db.productoDao().insertProducto(producto1, producto2, producto3, producto4)

                // Crear abonos
                val abono1 = Abono(0, 1, 200.0, "2024-12-01")
                val abono2 = Abono(0, 1, 100.0, "2024-12-02")
                val abono3 = Abono(0, 1, 150.0, "2024-12-01")
                val abono4 = Abono(0, 2, 150.0, "2024-12-02")
                db.abonoDao().insertAbono(abono1, abono2, abono3, abono4)
            }
        }
        cargarClientes()
    }
}