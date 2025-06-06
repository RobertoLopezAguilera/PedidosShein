package com.example.pedidosshein

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.example.pedidosshein.data.entities.Cliente
import com.example.pedidosshein.ui.theme.PedidosSheinTheme
import com.google.android.gms.ads.MobileAds
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.NumberFormat

class MainActivity : ComponentActivity() {
    private var recargarDatos by mutableStateOf(false)

    override fun onResume() {
        super.onResume()
        recargarDatos = true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sharedPreferences = getSharedPreferences("UserSession", Context.MODE_PRIVATE)
        val userEmail = sharedPreferences.getString("USER_EMAIL", null)
        val currentUser = FirebaseAuth.getInstance().currentUser

        if (currentUser == null || userEmail.isNullOrEmpty()) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        MobileAds.initialize(this)

        setContent {
            PedidosSheinTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    ClienteScreen(
                        userEmail = userEmail,
                        recargarTrigger = recargarDatos,
                        onRecargaConsumida = { recargarDatos = false },
                        onLogout = { logout() },
                        onNavigateTo = { route ->
                            when (route) {
                                "productos" -> startActivity(Intent(this, ProductosActivity::class.java))
                                "abonos" -> startActivity(Intent(this, AbonoActivity::class.java))
                                "perfil" -> startActivity(Intent(this, PerfilActivity::class.java))
                                "agregar_cliente" -> startActivity(Intent(this, AgregarClienteActivity::class.java))
                            }
                        },
                        onClienteClick = { cliente ->
                            val intent = Intent(this, ClienteDetalleActivity::class.java)
                            intent.putExtra("CLIENTE_ID", cliente.id)
                            startActivity(intent)
                        }
                    )
                }
            }
        }
    }

    private fun logout() {
        getSharedPreferences("UserSession", Context.MODE_PRIVATE).edit().clear().apply()
        FirebaseAuth.getInstance().signOut()
        Toast.makeText(this, "Sesión cerrada con éxito", Toast.LENGTH_SHORT).show()
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClienteScreen(
    userEmail: String,
    recargarTrigger: Boolean,
    onRecargaConsumida: () -> Unit,
    onLogout: () -> Unit,
    onNavigateTo: (String) -> Unit,
    onClienteClick: (Cliente) -> Unit
) {
    val context = LocalContext.current
    val db = remember { AppDatabase.getInstance(context) }
    var clientes by remember { mutableStateOf(listOf<Cliente>()) }
    var clientesConDeuda by remember { mutableStateOf<Map<Int, Double>>(emptyMap()) }
    var search by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    // Paleta de colores personalizada
    val primaryColor = colorResource(id = R.color.purple_500)
    val primaryContainer = colorResource(id = R.color.purple_200)
    val background = colorResource(id = R.color.background)
    val surface = colorResource(id = R.color.surface)
    val onSurface = colorResource(id = R.color.on_surface)
    val errorColor = colorResource(id = R.color.red_400)
    val successColor = colorResource(id = R.color.green_400)
    val infoColor = colorResource(id = R.color.blue_400)

    // Cargar clientes y calcular deudas
    LaunchedEffect(search, recargarTrigger) {
        scope.launch(Dispatchers.IO) {
            val clientesFiltrados = if (search.isBlank()) {
                db.clienteDao().getAllClientes()
            } else {
                db.clienteDao().buscarClientes(search.lowercase())
            }

            val todosProductos = db.productoDao().getAllProductos()
            val todosAbonos = db.abonoDao().getAllAbonos()

            val deudasMap = mutableMapOf<Int, Double>()
            clientesFiltrados.forEach { cliente ->
                val productosCliente = todosProductos.filter { it.clienteId == cliente.id }
                val abonosCliente = todosAbonos.filter { it.clienteId == cliente.id }
                val totalProductos = productosCliente.sumOf { it.precio }
                val totalAbonos = abonosCliente.sumOf { it.monto }
                deudasMap[cliente.id] = totalProductos - totalAbonos
            }

            withContext(Dispatchers.Main) {
                clientes = clientesFiltrados
                clientesConDeuda = deudasMap
                if (recargarTrigger) onRecargaConsumida()
            }
        }
    }

    // Diseño mejorado con Surface y Columna
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = background
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // AppBar con gradiente
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
                            "Pedidos Shein",
                            color = Color.White,
                            style = MaterialTheme.typography.titleLarge
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        actionIconContentColor = Color.White
                    ),
                    actions = {
                        IconButton(onClick = { onNavigateTo("agregar_cliente") }) {
                            Icon(
                                painter = painterResource(id = R.drawable.icon_agregar_cliente),
                                contentDescription = "Agregar Cliente",
                                modifier = Modifier.size(24.dp),
                                tint = Color.White
                            )
                        }
                        IconButton(onClick = { onLogout() }) {
                            Icon(
                                painter = painterResource(id = R.drawable.icon_logout),
                                contentDescription = "Cerrar Sesión",
                                modifier = Modifier.size(24.dp),
                                tint = Color.White
                            )
                        }
                    }
                )
            }

            // Barra de búsqueda con sombra
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .shadow(4.dp, shape = RoundedCornerShape(8.dp)),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = surface)
            ) {
                TextField(
                    value = search,
                    onValueChange = { search = it },
                    label = { Text("Buscar cliente") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Buscar",
                            tint = primaryColor
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = surface,
                        unfocusedContainerColor = surface,
                        disabledContainerColor = surface,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    shape = RoundedCornerShape(8.dp),
                    singleLine = true
                )
            }

            // Lista de clientes
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                items(clientes) { cliente ->
                    val deuda = clientesConDeuda[cliente.id] ?: 0.0
                    ClienteItem(
                        cliente = cliente,
                        deuda = deuda,
                        onClick = { onClienteClick(cliente) },
                        primaryColor = primaryColor,
                        surfaceColor = surface,
                        onSurfaceColor = onSurface,
                        errorColor = errorColor,
                        successColor = successColor,
                        infoColor = infoColor
                    )
                }
            }

            // Barra de navegación inferior
            NavigationBar(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(8.dp),
                containerColor = surface
            ) {
                NavigationBarItem(
                    selected = false,
                    onClick = {},
                    icon = {
                        Icon(
                            painter = painterResource(id = R.drawable.clientes),
                            contentDescription = "Clientes",
                            modifier = Modifier.size(24.dp),
                            tint = primaryColor
                        )
                    },
                    label = {
                        Text(
                            text = "Clientes",
                            color = primaryColor,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { onNavigateTo("productos") },
                    icon = {
                        Icon(
                            painter = painterResource(id = R.drawable.pedidos),
                            contentDescription = "Productos",
                            modifier = Modifier.size(24.dp),
                            tint = primaryColor
                        )
                    },
                    label = {
                        Text(
                            text = "Productos",
                            color = primaryColor,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { onNavigateTo("abonos") },
                    icon = {
                        Icon(
                            painter = painterResource(id = R.drawable.abonos),
                            contentDescription = "Abonos",
                            modifier = Modifier.size(24.dp),
                            tint = primaryColor
                        )
                    },
                    label = {
                        Text(
                            text = "Abonos",
                            color = primaryColor,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { onNavigateTo("perfil") },
                    icon = {
                        Icon(
                            painter = painterResource(id = R.drawable.iconamoonprofilecirclefill),
                            contentDescription = "Perfil",
                            modifier = Modifier.size(24.dp),
                            tint = primaryColor
                        )
                    },
                    label = {
                        Text(
                            text = "Perfil",
                            color = primaryColor,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                )
            }
        }
    }
}

@Composable
fun ClienteItem(
    cliente: Cliente,
    deuda: Double,
    onClick: () -> Unit,
    primaryColor: Color,
    surfaceColor: Color,
    onSurfaceColor: Color,
    errorColor: Color,
    successColor: Color,
    infoColor: Color
) {
    // Determinar colores según estado de deuda
    val (debtColor, statusText, iconRes) = when {
        deuda > 0 -> Triple(
            errorColor,
            "Debe: ${NumberFormat.getCurrencyInstance().format(deuda)}",
            R.drawable.icon_agregar_abono
        )
        deuda < 0 -> Triple(
            infoColor,
            "A favor: ${NumberFormat.getCurrencyInstance().format(-deuda)}",
            R.drawable.icon_agregar_abono
        )
        else -> Triple(
            successColor,
            "Al día",
            R.drawable.icon_agregar_abono
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = surfaceColor)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar del cliente
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        color = primaryColor.copy(alpha = 0.2f),
                        shape = CircleShape
                    )
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.iconamoonprofilecirclefill),
                    contentDescription = "Cliente",
                    modifier = Modifier.size(24.dp),
                    tint = primaryColor
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Información principal
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = cliente.nombre ?: "Sin nombre",
                    style = MaterialTheme.typography.titleLarge,
                    color = onSurfaceColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = cliente.telefono ?: "Sin teléfono",
                    style = MaterialTheme.typography.bodyMedium,
                    color = onSurfaceColor.copy(alpha = 0.6f)
                )
            }

            // Monto y estado
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(id = iconRes),
                        contentDescription = "Estado de deuda",
                        modifier = Modifier.size(16.dp),
                        tint = debtColor
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = statusText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = debtColor
                    )
                }
            }
        }
    }
}