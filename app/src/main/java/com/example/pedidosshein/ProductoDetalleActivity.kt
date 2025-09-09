package com.example.pedidosshein

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.example.pedidosshein.data.database.AppDatabase
import com.example.pedidosshein.data.entities.Producto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.NumberFormat

class ProductoDetalleActivity : ComponentActivity() {

    private var productoId: Int = -1
    private val recargarProducto = mutableStateOf(false)

    private val editarProductoLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            recargarProducto.value = true
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        productoId = intent.getIntExtra("PRODUCTO_ID", -1)

        setContent {
            if (productoId == -1) {
                Toast.makeText(this, "Producto no encontrado", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                ProductoDetalleScreen(
                    productoId = productoId,
                    recargarTrigger = recargarProducto.value,
                    onRecargaConsumida = { recargarProducto.value = false },
                    onEditar = { id ->
                        val intent = Intent(this, EditarProductoActivity::class.java)
                        intent.putExtra("PRODUCTO_ID", id)
                        editarProductoLauncher.launch(intent)
                    },
                    onEliminar = { finish() }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductoDetalleScreen(
    productoId: Int,
    recargarTrigger: Boolean,
    onRecargaConsumida: () -> Unit,
    onEditar: (Int) -> Unit,
    onEliminar: () -> Unit
) {
    // Paleta de colores
    val primaryColor = colorResource(id = R.color.purple_500)
    val primaryContainer = colorResource(id = R.color.purple_200)
    val background = colorResource(id = R.color.background)
    val surface = colorResource(id = R.color.surface)
    val onSurface = colorResource(id = R.color.on_surface)
    val errorColor = colorResource(id = R.color.red_400)

    val context = LocalContext.current
    val db = remember { AppDatabase.getInstance(context) }
    val scope = rememberCoroutineScope()

    var producto by remember { mutableStateOf<Producto?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }

    // Cargar datos
    LaunchedEffect(productoId, recargarTrigger) {
        isLoading = true
        scope.launch(Dispatchers.IO) {
            producto = db.productoDao().getProductoById(productoId)
            if (recargarTrigger) {
                onRecargaConsumida()
            }
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
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
                            "Detalle del Producto",
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
                    },
                    actions = {
                        IconButton(onClick = { onEditar(productoId) }) {
                            Icon(
                                painter = painterResource(id = R.drawable.icon_editar),
                                contentDescription = "Editar",
                                tint = Color.White
                            )
                        }
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(
                                painter = painterResource(id = R.drawable.borrar_cliente),
                                contentDescription = "Eliminar",
                                tint = Color.White
                            )
                        }
                    }
                )
            }
        }
    ) { paddingValues ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            producto?.let { prod ->
                Column(
                    modifier = Modifier
                        .padding(paddingValues)
                        .padding(16.dp)
                        .fillMaxSize(),
                    verticalArrangement = Arrangement.Top,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Imagen del producto
                    if (!prod.fotoUri.isNullOrBlank()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp),
                                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                                colors = CardDefaults.cardColors(containerColor = surface)
                            ) {
                                Image(
                                    painter = rememberAsyncImagePainter(model = File(prod.fotoUri)),
                                    contentDescription = "Foto del producto",
                                    contentScale = ContentScale.Fit, // Cambiado a Fit
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(max = 400.dp) // Altura máxima flexible
                                )
                            }
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                                .aspectRatio(1f)
                                .background(
                                    color = primaryColor.copy(alpha = 0.1f),
                                    shape = RoundedCornerShape(12.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            IconButton(onClick = { onEditar(productoId) }) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_image_placeholder),
                                    contentDescription = "Sin imagen",
                                    modifier = Modifier.size(64.dp),
                                    tint = primaryColor.copy(alpha = 0.5f)
                                )
                            }

                        }
                    }

                    // Información del producto
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        colors = CardDefaults.cardColors(containerColor = surface)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = prod.nombre.toString(),
                                style = MaterialTheme.typography.headlineSmall,
                                color = onSurface,
                                fontWeight = FontWeight.Bold
                            )

                            Divider(color = primaryColor.copy(alpha = 0.2f))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Precio:",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = onSurface.copy(alpha = 0.7f)
                                )
                                Text(
                                    text = NumberFormat.getCurrencyInstance().format(prod.precio),
                                    style = MaterialTheme.typography.titleLarge,
                                    color = primaryColor,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Fecha:",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = onSurface.copy(alpha = 0.7f)
                                )
                                prod.fechaPedido?.let {
                                    Text(
                                        text = it,
                                        style = MaterialTheme.typography.titleLarge,
                                        color = primaryColor,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            } ?: run {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_error),
                            contentDescription = "Error",
                            modifier = Modifier.size(48.dp),
                            tint = errorColor
                        )
                        Text(
                            text = "Producto no encontrado",
                            style = MaterialTheme.typography.titleMedium,
                            color = onSurface
                        )
                    }
                }
            }
        }
    }

    // Diálogo de confirmación para eliminar
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = {
                Text(
                    "Eliminar producto",
                    style = MaterialTheme.typography.titleLarge,
                    color = errorColor
                )
            },
            text = {
                Text(
                    "¿Estás seguro de que deseas eliminar este producto permanentemente?",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteDialog = false
                        scope.launch(Dispatchers.IO) {
                            val deleted = db.productoDao().deleteProductoById(productoId)
                            withContext(Dispatchers.Main) {
                                if (deleted > 0) {
                                    Toast.makeText(
                                        context,
                                        "Producto eliminado",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    onEliminar()
                                } else {
                                    Toast.makeText(
                                        context,
                                        "Error al eliminar el producto",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = errorColor)
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteDialog = false },
                    colors = ButtonDefaults.textButtonColors(contentColor = onSurface)
                ) {
                    Text("Cancelar")
                }
            },
            containerColor = surface
        )
    }
}