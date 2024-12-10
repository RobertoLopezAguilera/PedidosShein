package com.example.pedidosshein

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.pedidosshein.data.adapters.AbonoAdapter
import com.example.pedidosshein.data.adapters.ProductoAdapter
import com.example.pedidosshein.data.database.AppDatabase
import com.example.pedidosshein.databinding.ActivityClienteDetalleBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import android.widget.Toast
import androidx.appcompat.app.AlertDialog

class ClienteDetalleActivity : AppCompatActivity() {

    private lateinit var binding: ActivityClienteDetalleBinding
    private lateinit var db: AppDatabase
    private lateinit var productoAdapter: ProductoAdapter
    private lateinit var abonoAdapter: AbonoAdapter
    private var clienteId: Int = -1

    // Registrar el resultado de la edición del producto
    private val editarProductoLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                cargarDatos(clienteId) // Recargar los datos del cliente
            }
        }

    // Registrar el resultado de la edición del abono
    private val editarAbonoLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                cargarDatos(clienteId) // Recargar los datos del cliente
            }
        }

    // Crear el menú de opciones
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_cliente_detalle, menu)
        return true
    }

    // Manejar la selección de un ítem del menú
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_editar_cliente -> {
                editarCliente(clienteId)
                true
            }
            R.id.menu_agregar_producto -> {
                agregarProducto(clienteId)
                true
            }
            R.id.menu_agregar_abono -> {
                agregarAbono(clienteId)
                true
            }
            R.id.menu_borrar_cliente -> {
                borrarCliente(clienteId)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun borrarCliente(clienteId: Int) {
        GlobalScope.launch(Dispatchers.IO) {
            val cliente = db.clienteDao().getClienteById(clienteId)
            withContext(Dispatchers.Main) {
                if (cliente != null) {
                    // Mostrar un cuadro de diálogo de confirmación
                    AlertDialog.Builder(this@ClienteDetalleActivity)
                        .setTitle("Eliminar Cliente")
                        .setMessage("¿Estás seguro de que deseas eliminar todos los productos y abonos asociados a este cliente?")
                        .setPositiveButton("Sí") { _, _ ->
                            // Proceder con la eliminación de productos y abonos sin borrar al cliente
                            GlobalScope.launch(Dispatchers.IO) {
                                try {
                                    db.productoDao().deleteProductoByClienteId(clienteId) // Eliminar productos según el ID del cliente
                                    db.abonoDao().deleteAbonoByClienteId(clienteId) // Eliminar abonos según el ID del cliente

                                    withContext(Dispatchers.Main) {
                                        Toast.makeText(
                                            this@ClienteDetalleActivity,
                                            "Productos y abonos eliminados correctamente",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        cargarDatos(clienteId) // Recargar los datos del cliente para reflejar los cambios
                                    }
                                } catch (e: Exception) {
                                    withContext(Dispatchers.Main) {
                                        Toast.makeText(
                                            this@ClienteDetalleActivity,
                                            "Error al eliminar productos y abonos: ${e.message}",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                            }
                        }
                        .setNegativeButton("No", null)
                        .show()
                } else {
                    Toast.makeText(this@ClienteDetalleActivity, "Error: Cliente no encontrado", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun agregarAbono(clienteId: Int) {
        val intent = Intent(this, AgregarAbonoActivity::class.java)
        intent.putExtra("CLIENTE_ID", clienteId)
        startActivity(intent) // Iniciar la actividad para agregar el abono
    }

    private fun agregarProducto(clienteId: Int) {
        val intent = Intent(this, AgregarProductoActivity::class.java)
        intent.putExtra("CLIENTE_ID", clienteId)
        startActivity(intent) // Iniciar la actividad para agregar el producto
    }


    private fun editarCliente(clienteId: Int) {
        val intent = Intent(this, EditarClienteActivity::class.java)
        intent.putExtra("CLIENTE_ID", clienteId)
        startActivity(intent)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inicializar View Binding
        binding = ActivityClienteDetalleBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = AppDatabase.getInstance(this)

        // Configurar RecyclerView para los productos
        binding.rvProductos.layoutManager = LinearLayoutManager(this)

        // Configurar RecyclerView para los abonos
        binding.rvAbonos.layoutManager = LinearLayoutManager(this)

        // Configurar SwipeRefreshLayout para actualizar los datos
        binding.swipeRefreshLayout.setOnRefreshListener {
            cargarDatos(clienteId) // Recargar los datos cuando se deslice hacia abajo
        }

        // Obtener el ID del cliente del Intent
        clienteId = intent.getIntExtra("CLIENTE_ID", -1)

        // Asegurarse de que el ID es válido
        if (clienteId != -1) {
            cargarCliente(clienteId)
            cargarDatos(clienteId)
        }
    }

    private fun cargarCliente(clienteId: Int) {
        GlobalScope.launch(Dispatchers.IO) {
            val cliente = db.clienteDao().getClienteById(clienteId)
            withContext(Dispatchers.Main) {
                // Establecer los datos del cliente
                binding.tvClienteNombre.text = cliente?.nombre ?: "Desconocido"
                binding.tvClienteTelefono.text = cliente?.telefono ?: "Desconocido"
            }
        }
    }

    private fun cargarDatos(clienteId: Int) {
        // Iniciar el refresco visual del SwipeRefreshLayout
        binding.swipeRefreshLayout.isRefreshing = true

        GlobalScope.launch(Dispatchers.IO) {
            val productos = db.productoDao().getProductosByClienteId(clienteId)
            val totalProductos = productos.sumOf { it.precio }

            val abonos = db.abonoDao().getAbonosByClienteId(clienteId)
            val totalAbonos = abonos.fold(0.0) { acc, abono -> acc + abono.monto }

            val restante = totalProductos - totalAbonos

            withContext(Dispatchers.Main) {
                // Mostrar los productos
                binding.tvProductosNombre.text = "Productos - Total: \$${"%.2f".format(totalProductos)}"
                productoAdapter = ProductoAdapter(productos) { producto ->
                    editarProducto(producto.id) // Editar producto al hacer clic
                }
                binding.rvProductos.adapter = productoAdapter

                // Mostrar los abonos
                binding.tvAbonosNombre.text = "Abonos - Total: \$${"%.2f".format(totalAbonos)}"
                abonoAdapter = AbonoAdapter(abonos) { abono ->
                    editarAbono(abono.id) // Editar abono al hacer clic
                }
                binding.rvAbonos.adapter = abonoAdapter

                // Mostrar la deuda restante
                binding.tvDeuda.text = "Deuda restante: \$${"%.2f".format(restante)}"

                // Detener el refresco visual una vez que los datos estén actualizados
                binding.swipeRefreshLayout.isRefreshing = false
            }
        }
    }

    private fun editarProducto(productoId: Int) {
        val intent = Intent(this, ProductoDetalleActivity::class.java)
        intent.putExtra("PRODUCTO_ID", productoId)
        editarProductoLauncher.launch(intent) // Iniciar ProductoDetalleActivity para edición
    }

    private fun editarAbono(abonoId: Int) {
        val intent = Intent(this, AbonoDetalleActivity::class.java)
        intent.putExtra("ABONO_ID", abonoId)
        editarAbonoLauncher.launch(intent) // Iniciar EditarAbonoActivity para edición
    }
}
