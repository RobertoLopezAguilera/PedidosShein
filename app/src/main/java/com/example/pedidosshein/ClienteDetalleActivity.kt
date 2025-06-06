package com.example.pedidosshein

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.registerForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.pedidosshein.data.database.AppDatabase
import com.example.pedidosshein.data.entities.Abono
import com.example.pedidosshein.data.entities.Producto
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.NumberFormat
import kotlin.properties.Delegates

class ClienteDetalleActivity : ComponentActivity() {
    private var recargarDatos by mutableStateOf(false)
    private var clienteId by Delegates.notNull<Int>()

    private val editarProductoLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { if (it.resultCode == RESULT_OK) recargarDatos = true }

    private val editarAbonoLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { if (it.resultCode == RESULT_OK) recargarDatos = true }

    override fun onResume() {
        super.onResume()
        recargarDatos = true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        clienteId = intent.getIntExtra("CLIENTE_ID", -1)
        if (clienteId == -1) {
            Toast.makeText(this, "ID de cliente inválido", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        setContent {
            ClienteDetalleContent()
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun ClienteDetalleContent() {
        val context = LocalContext.current
        val db = remember { AppDatabase.getInstance(context) }
        val scope = rememberCoroutineScope()

        // Estados
        var clienteNombre by remember { mutableStateOf("Cargando...") }
        var clienteTelefono by remember { mutableStateOf("Cargando...") }
        var productos by remember { mutableStateOf(emptyList<Producto>()) }
        var abonos by remember { mutableStateOf(emptyList<Abono>()) }
        var refreshing by remember { mutableStateOf(false) }
        var expandirProductos by remember { mutableStateOf(false) }
        var expandirAbonos by remember { mutableStateOf(false) }

        // Paleta de colores
        val primaryColor = colorResource(id = R.color.purple_500)
        val primaryContainer = colorResource(id = R.color.purple_200)
        val background = colorResource(id = R.color.background)
        val surface = colorResource(id = R.color.surface)
        val onSurface = colorResource(id = R.color.on_surface)
        val errorColor = colorResource(id = R.color.red_400)
        val successColor = colorResource(id = R.color.green_400)
        val infoColor = colorResource(id = R.color.blue_400)

        // Cálculos financieros
        val totalProductos = productos.sumOf { it.precio }
        val totalAbonos = abonos.sumOf { it.monto }
        val deuda = totalProductos - totalAbonos
        val deudaColor = when {
            deuda > 0 -> errorColor
            deuda < 0 -> infoColor
            else -> successColor
        }

        // Función para cargar datos
        fun cargarDatos() {
            refreshing = true
            scope.launch(Dispatchers.IO) {
                val cliente = db.clienteDao().getClienteById(clienteId)
                val productosDB = db.productoDao().getProductosByClienteId(clienteId)
                val abonosDB = db.abonoDao().getAbonosByClienteId(clienteId)

                withContext(Dispatchers.Main) {
                    clienteNombre = cliente?.nombre ?: "Desconocido"
                    clienteTelefono = cliente?.telefono ?: "Desconocido"
                    productos = productosDB
                    abonos = abonosDB
                    refreshing = false
                }
            }
        }

        // Efectos para cargar datos
        LaunchedEffect(Unit) { cargarDatos() }
        LaunchedEffect(recargarDatos) {
            if (recargarDatos) {
                cargarDatos()
                recargarDatos = false
            }
        }

        // Función para formatear moneda
        fun formatCurrency(amount: Double): String {
            return NumberFormat.getCurrencyInstance().format(amount)
        }

        // Diseño de la pantalla
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = background
        ) {
            Scaffold(
                topBar = {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(primaryColor, primaryContainer)
                                )
                            )
                    ) {
                        TopAppBar(
                            title = {
                                Text(
                                    "Detalle de Cliente",
                                    color = Color.White,
                                    style = typography.titleLarge
                                )
                            },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = Color.Transparent,
                                actionIconContentColor = Color.White
                            ),
                            actions = {
                                IconButton(
                                    onClick = {
                                        startActivity(
                                            Intent(context, EditarClienteActivity::class.java).apply {
                                                putExtra("CLIENTE_ID", clienteId)
                                            }
                                        )
                                    }
                                ) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.icon_editar_cliente),
                                        contentDescription = "Editar cliente"
                                    )
                                }
                                IconButton(
                                    onClick = {
                                        startActivity(
                                            Intent(context, AgregarProductoActivity::class.java).apply {
                                                putExtra("CLIENTE_ID", clienteId)
                                            }
                                        )
                                    }
                                ) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.icon_pedidos),
                                        contentDescription = "Agregar producto"
                                    )
                                }
                                IconButton(
                                    onClick = {
                                        startActivity(
                                            Intent(context, AgregarAbonoActivity::class.java).apply {
                                                putExtra("CLIENTE_ID", clienteId)
                                            }
                                        )
                                    }
                                ) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.icon_agregar_abono),
                                        contentDescription = "Agregar abono"
                                    )
                                }
                                IconButton(
                                    onClick = {
                                        AlertDialog.Builder(context)
                                            .setTitle("Eliminar registros")
                                            .setMessage("¿Eliminar productos y abonos del cliente?")
                                            .setPositiveButton("Sí") { _, _ ->
                                                scope.launch(Dispatchers.IO) {
                                                    db.productoDao().deleteProductoByClienteId(clienteId)
                                                    db.abonoDao().deleteAbonoByClienteId(clienteId)
                                                    withContext(Dispatchers.Main) {
                                                        Toast.makeText(
                                                            context,
                                                            "Registros eliminados",
                                                            Toast.LENGTH_SHORT
                                                        ).show()
                                                        cargarDatos()
                                                    }
                                                }
                                            }
                                            .setNegativeButton("No", null)
                                            .show()
                                    }
                                ) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.borrar_cliente),
                                        contentDescription = "Eliminar registros"
                                    )
                                }
                            }
                        )
                    }
                },
                floatingActionButton = {
                    FloatingActionButton(
                        onClick = {
                            scope.launch(Dispatchers.IO) {
                                val cliente = db.clienteDao().getClienteById(clienteId)
                                val productos = db.productoDao().getProductosByClienteId(clienteId)
                                val abonos = db.abonoDao().getAbonosByClienteId(clienteId)
                                val totalProductos = productos.sumOf { it.precio }
                                val totalAbonos = abonos.sumOf { it.monto }
                                val deuda = totalProductos - totalAbonos

                                val mensaje = buildString {
                                    appendLine("Cliente: ${cliente?.nombre}")
                                    appendLine("Teléfono: ${cliente?.telefono}")
                                    appendLine()
                                    appendLine("Productos (Total: ${formatCurrency(totalProductos)}):")
                                    productos.forEach { producto ->
                                        appendLine("- ${producto.nombre}: ${formatCurrency(producto.precio)}")
                                    }
                                    appendLine()
                                    appendLine("Abonos (Total: ${formatCurrency(totalAbonos)}):")
                                    abonos.forEach { abono ->
                                        appendLine("- ${formatCurrency(abono.monto)} el ${abono.fecha}")
                                    }
                                    appendLine()
                                    appendLine("Deuda restante: ${formatCurrency(deuda)}")
                                }

                                withContext(Dispatchers.Main) {
                                    val intent = Intent(Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(Intent.EXTRA_TEXT, mensaje)
                                    }
                                    context.startActivity(Intent.createChooser(intent, "Compartir detalles"))
                                }
                            }
                        },
                        containerColor = primaryColor,
                        contentColor = Color.White
                    ) {
                        Icon(Icons.Default.Share, contentDescription = "Compartir")
                    }
                }
            ) { padding ->
                SwipeRefresh(
                    state = rememberSwipeRefreshState(isRefreshing = refreshing),
                    onRefresh = { cargarDatos() },
                    modifier = Modifier
                        .padding(padding)
                        .fillMaxSize()
                ) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Información del cliente
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                                colors = CardDefaults.cardColors(containerColor = surface)
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = clienteNombre,
                                            style = typography.titleLarge,
                                            color = onSurface
                                        )
                                        Text(
                                            text = formatCurrency(deuda),
                                            style = typography.titleLarge,
                                            color = deudaColor,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    Text(
                                        text = clienteTelefono,
                                        style = typography.bodyMedium,
                                        color = onSurface.copy(alpha = 0.7f)
                                    )

                                    // Estado de la deuda
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(top = 4.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .background(
                                                    color = deudaColor,
                                                    shape = CircleShape
                                                )
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = when {
                                                deuda > 0 -> "Deuda pendiente"
                                                deuda < 0 -> "Saldo a favor"
                                                else -> "Cuenta al día"
                                            },
                                            style = typography.bodyMedium,
                                            color = deudaColor
                                        )
                                    }
                                }
                            }
                        }

                        // Resumen financiero
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                // Total productos
                                Card(
                                    modifier = Modifier.weight(1f),
                                    colors = CardDefaults.cardColors(containerColor = surface)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(12.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = "Productos",
                                            style = typography.labelMedium,
                                            color = onSurface.copy(alpha = 0.6f)
                                        )
                                        Text(
                                            text = formatCurrency(totalProductos),
                                            style = typography.titleMedium,
                                            color = primaryColor
                                        )
                                    }
                                }

                                // Total abonos
                                Card(
                                    modifier = Modifier.weight(1f),
                                    colors = CardDefaults.cardColors(containerColor = surface)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(12.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = "Abonos",
                                            style = typography.labelMedium,
                                            color = onSurface.copy(alpha = 0.6f)
                                        )
                                        Text(
                                            text = formatCurrency(totalAbonos),
                                            style = typography.titleMedium,
                                            color = successColor
                                        )
                                    }
                                }
                            }
                        }

                        // Sección de productos
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = surface),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Column {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { expandirProductos = !expandirProductos }
                                            .padding(16.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "Productos (${productos.size})",
                                            style = typography.titleMedium,
                                            color = onSurface
                                        )
                                        Icon(
                                            imageVector = if (expandirProductos) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                            contentDescription = if (expandirProductos) "Contraer" else "Expandir",
                                            tint = primaryColor
                                        )
                                    }

                                    AnimatedVisibility(visible = expandirProductos) {
                                        Column {
                                            productos.forEach { producto ->
                                                Card(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                                        .clickable {
                                                            editarProductoLauncher.launch(
                                                                Intent(
                                                                    context,
                                                                    ProductoDetalleActivity::class.java
                                                                ).apply {
                                                                    putExtra("PRODUCTO_ID", producto.id)
                                                                }
                                                            )
                                                        },
                                                    colors = CardDefaults.cardColors(
                                                        containerColor = surface.copy(alpha = 0.8f)
                                                    )
                                                ) {
                                                    Row(
                                                        modifier = Modifier.padding(12.dp),
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.SpaceBetween
                                                    ) {
                                                        Column(modifier = Modifier.weight(1f)) {
                                                            Text(
                                                                text = producto.nombre ?: "Sin nombre",
                                                                style = typography.bodyLarge,
                                                                color = onSurface,
                                                                maxLines = 1,
                                                                overflow = TextOverflow.Ellipsis
                                                            )
                                                        }

                                                        Text(
                                                            text = formatCurrency(producto.precio),
                                                            style = typography.bodyLarge,
                                                            color = primaryColor,
                                                            fontWeight = FontWeight.Bold
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Sección de abonos
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = surface),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Column {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { expandirAbonos = !expandirAbonos }
                                            .padding(16.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "Abonos (${abonos.size})",
                                            style = typography.titleMedium,
                                            color = onSurface
                                        )
                                        Icon(
                                            imageVector = if (expandirAbonos) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                            contentDescription = if (expandirAbonos) "Contraer" else "Expandir",
                                            tint = primaryColor
                                        )
                                    }

                                    AnimatedVisibility(visible = expandirAbonos) {
                                        Column {
                                            abonos.forEach { abono ->
                                                Card(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                                        .clickable {
                                                            editarAbonoLauncher.launch(
                                                                Intent(
                                                                    context,
                                                                    AbonoDetalleActivity::class.java
                                                                ).apply {
                                                                    putExtra("ABONO_ID", abono.id)
                                                                }
                                                            )
                                                        },
                                                    colors = CardDefaults.cardColors(
                                                        containerColor = surface.copy(alpha = 0.8f)
                                                    )
                                                ) {
                                                    Row(
                                                        modifier = Modifier.padding(12.dp),
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.SpaceBetween
                                                    ) {
                                                        Column(modifier = Modifier.weight(1f)) {
                                                            Text(
                                                                text = "Abono del ${abono.fecha}",
                                                                style = typography.bodyLarge,
                                                                color = onSurface
                                                            )
                                                        }

                                                        Text(
                                                            text = formatCurrency(abono.monto),
                                                            style = typography.bodyLarge,
                                                            color = successColor,
                                                            fontWeight = FontWeight.Bold
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        item {
                            Spacer(modifier = Modifier.height(80.dp))
                        }
                    }
                }
            }
        }
    }
}