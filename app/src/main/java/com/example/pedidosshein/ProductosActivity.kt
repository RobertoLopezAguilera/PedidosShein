package com.example.pedidosshein

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
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
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pedidosshein.data.database.AppDatabase
import com.example.pedidosshein.data.entities.Producto
import com.example.pedidosshein.ui.theme.PedidosSheinTheme
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.LoadAdError
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.NumberFormat

class ProductosActivity : ComponentActivity() {
    private var interstitial: InterstitialAd? = null
    private val recargarProducto = mutableStateOf(false)

    private val editarProductoLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            recargarProducto.value = true
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MobileAds.initialize(this)

        setContent {
            PedidosSheinTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    ProductosScreen(
                        onProductoClick = { clienteId -> mostrarDetallesCliente(clienteId) },
                        recargarTrigger = recargarProducto.value,
                        onRecargaConsumida = { recargarProducto.value = false },
                        onNavigateTo = { route ->
                            when (route) {
                                "clientes" -> startActivity(Intent(this, MainActivity::class.java))
                                "abonos" -> startActivity(Intent(this, AbonoActivity::class.java))
                                "perfil" -> startActivity(Intent(this, PerfilActivity::class.java))
                                "agregar_cliente" -> startActivity(Intent(this, AgregarClienteActivity::class.java))
                            }
                        },
                        onLogout = { logout() }
                    )
                }
            }
        }
    }

    private fun mostrarDetallesCliente(clienteId: Int) {
        val intent = Intent(this, ClienteDetalleActivity::class.java)
        intent.putExtra("CLIENTE_ID", clienteId)
        startActivity(intent)
    }

    private fun logout() {
        getSharedPreferences("UserSession", Context.MODE_PRIVATE).edit().clear().apply()
        FirebaseAuth.getInstance().signOut()
        Toast.makeText(this, "Sesión cerrada con éxito", Toast.LENGTH_SHORT).show()
        startActivity(Intent(this, LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        })
        finish()
    }
}

class ProductosViewModel(context: Context) : ViewModel() {
    private val dao = AppDatabase.getInstance(context).productoDao()
    var productos by mutableStateOf(listOf<Producto>())
        private set

    fun cargarProductos(query: String = "") {
        viewModelScope.launch {
            productos = withContext(Dispatchers.IO) {
                if (query.isBlank()) dao.getAllProductos() else dao.buscarProductos(query)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductosScreen(
    onProductoClick: (Int) -> Unit,
    recargarTrigger: Boolean,
    onRecargaConsumida: () -> Unit,
    onNavigateTo: (String) -> Unit,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val viewModel = remember { ProductosViewModel(context) }
    var search by remember { mutableStateOf("") }

    // Paleta de colores consistente con la pantalla principal
    val primaryColor = colorResource(id = R.color.purple_500)
    val primaryContainer = colorResource(id = R.color.purple_200)
    val background = colorResource(id = R.color.background)
    val surface = colorResource(id = R.color.surface)
    val onSurface = colorResource(id = R.color.on_surface)
    val secondaryColor = colorResource(id = R.color.teal_200)

    // Cargar productos
    LaunchedEffect(search, recargarTrigger) {
        viewModel.cargarProductos(search)
        if (recargarTrigger) onRecargaConsumida()
    }

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
                            "Productos Shein",
                            color = Color.White,
                            style = MaterialTheme.typography.titleLarge
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        actionIconContentColor = Color.White
                    ),
                    navigationIcon = {
                        IconButton(onClick = { (context as? ComponentActivity)?.finish() }) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Volver",
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
                    label = { Text("Buscar producto") },
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

            // Lista de productos
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                items(viewModel.productos) { producto ->
                    val nombreCliente = viewModel.productos[producto.clienteId] ?: "Cliente desconocido"
                    ProductoItem(
                        producto = producto,
                        onClick = { onProductoClick(producto.clienteId) },
                        primaryColor = primaryColor,
                        surfaceColor = surface,
                        onSurfaceColor = onSurface,
                        secondaryColor = secondaryColor
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
                    onClick = { onNavigateTo("clientes") },
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
                    selected = true, // Resaltamos que estamos en productos
                    onClick = { /* Ya estamos aquí */ },
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
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
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
fun ProductoItem(
    producto: Producto,
    onClick: () -> Unit,
    primaryColor: Color,
    surfaceColor: Color,
    onSurfaceColor: Color,
    secondaryColor: Color
) {
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
            // Icono del producto
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        color = secondaryColor.copy(alpha = 0.2f),
                        shape = CircleShape
                    )
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.pedidos),
                    contentDescription = "Producto",
                    modifier = Modifier.size(24.dp),
                    tint = secondaryColor
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Información principal
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = producto.nombre ?: "Producto sin nombre",
                    style = MaterialTheme.typography.titleMedium,
                    color = onSurfaceColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Precio y detalles
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = NumberFormat.getCurrencyInstance().format(producto.precio),
                    style = MaterialTheme.typography.titleMedium,
                    color = primaryColor,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}