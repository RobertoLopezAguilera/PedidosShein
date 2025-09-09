package com.example.pedidosshein

import android.app.DatePickerDialog
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.example.pedidosshein.data.database.AppDatabase
import com.example.pedidosshein.data.entities.Producto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class EditarProductoActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val productoId = intent.getIntExtra("PRODUCTO_ID", -1)
        setContent {
            EditarProductoScreen(productoId = productoId) {
                setResult(RESULT_OK)
                finish()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditarProductoScreen(productoId: Int, onProductoActualizado: () -> Unit) {
    val context = LocalContext.current
    val db = remember { AppDatabase.getInstance(context) }
    val scope = rememberCoroutineScope()

    // Paleta de colores
    val primaryColor = colorResource(id = R.color.purple_500)
    val primaryContainer = colorResource(id = R.color.purple_200)
    val background = colorResource(id = R.color.background)
    val surface = colorResource(id = R.color.surface)
    val onSurface = colorResource(id = R.color.on_surface)
    val errorColor = colorResource(id = R.color.red_400)
    val successColor = colorResource(id = R.color.green_400)

    // Estados del formulario
    var nombre by remember { mutableStateOf("") }
    var precio by remember { mutableStateOf("") }
    var fechaPedido by remember { mutableStateOf("") }
    var fotoUri by remember { mutableStateOf<String?>(null) }
    var clienteId by remember { mutableStateOf(0) }
    var cargando by remember { mutableStateOf(true) }
    var showErrorDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var showDatePicker by remember { mutableStateOf(false) }

    // Launcher para seleccionar imagen
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            scope.launch(Dispatchers.IO) {
                val rutaLocal = copiarImagenAlmacenamientoInterno(context, it)
                withContext(Dispatchers.Main) {
                    fotoUri = rutaLocal ?: run {
                        errorMessage = "Error al cargar la imagen"
                        showErrorDialog = true
                        null
                    }
                }
            }
        }
    }

    // Cargar datos del producto
    LaunchedEffect(productoId) {
        scope.launch(Dispatchers.IO) {
            try {
                val producto = db.productoDao().getProductoById(productoId)
                producto?.let {
                    nombre = it.nombre.toString()
                    precio = it.precio.toString()
                    fechaPedido = it.fechaPedido ?: getCurrentDate()
                    fotoUri = it.fotoUri
                    clienteId = it.clienteId
                } ?: run {
                    errorMessage = "Producto no encontrado"
                    showErrorDialog = true
                }
            } catch (e: Exception) {
                errorMessage = "Error al cargar los datos"
                showErrorDialog = true
            } finally {
                cargando = false
            }
        }
    }

    // Diálogo de error
    if (showErrorDialog) {
        AlertDialog(
            onDismissRequest = { showErrorDialog = false },
            title = { Text("Error", color = errorColor) },
            text = { Text(errorMessage) },
            confirmButton = {
                TextButton(
                    onClick = { showErrorDialog = false },
                    colors = ButtonDefaults.textButtonColors(contentColor = onSurface)
                ) {
                    Text("Aceptar")
                }
            },
            containerColor = surface
        )
    }

    // DatePicker Dialog
    if (showDatePicker) {
        val calendar = Calendar.getInstance()
        // Intentar parsear la fecha actual si existe
        try {
            if (fechaPedido.isNotEmpty()) {
                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val date = sdf.parse(fechaPedido)
                date?.let {
                    calendar.time = it
                }
            }
        } catch (e: Exception) {
            // Si hay error al parsear, usar fecha actual
        }

        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        DatePickerDialog(
            context,
            { _, selectedYear, selectedMonth, selectedDay ->
                val selectedDate = Calendar.getInstance().apply {
                    set(selectedYear, selectedMonth, selectedDay)
                }
                fechaPedido = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    .format(selectedDate.time)
                showDatePicker = false
            },
            year,
            month,
            day
        ).show()
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = background
    ) {
        if (cargando) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = primaryColor)
            }
        } else {
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
                                    "Editar Producto",
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
                }
            ) { paddingValues ->
                Column(
                    modifier = Modifier
                        .padding(paddingValues)
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Sección de imagen
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = surface)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            fotoUri?.let { uri ->
                                val imageModel = if (uri.startsWith("content://")) Uri.parse(uri) else File(uri)
                                Image(
                                    painter = rememberAsyncImagePainter(
                                        model = imageModel,
                                        error = painterResource(R.drawable.ic_error),
                                        placeholder = painterResource(R.drawable.ic_image_placeholder)
                                    ),
                                    contentDescription = "Foto del producto",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(max = 400.dp)
                                        .clip(RoundedCornerShape(8.dp)),
                                    contentScale = ContentScale.Fit
                                )
                            } ?: run {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(200.dp)
                                        .background(
                                            color = primaryColor.copy(alpha = 0.1f),
                                            shape = RoundedCornerShape(8.dp)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            painter = painterResource(id = R.drawable.ic_image_placeholder),
                                            contentDescription = "Sin imagen",
                                            modifier = Modifier.size(48.dp),
                                            tint = primaryColor.copy(alpha = 0.5f)
                                        )
                                        Text(
                                            text = "Sin imagen seleccionada",
                                            color = onSurface.copy(alpha = 0.7f)
                                        )
                                    }
                                }
                            }

                            FilledTonalButton(
                                onClick = { imagePickerLauncher.launch("image/*") },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.filledTonalButtonColors(
                                    containerColor = primaryColor.copy(alpha = 0.2f),
                                    contentColor = primaryColor
                                ),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_image_placeholder),
                                    contentDescription = "Cambiar imagen",
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Cambiar Imagen")
                            }
                        }
                    }

                    // Formulario de edición
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = surface)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                text = "Información del Producto",
                                style = MaterialTheme.typography.titleMedium,
                                color = primaryColor,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )

                            OutlinedTextField(
                                value = nombre,
                                onValueChange = { nombre = it },
                                label = { Text("Nombre del producto") },
                                modifier = Modifier.fillMaxWidth(),
                                colors = TextFieldDefaults.outlinedTextFieldColors(
                                    focusedBorderColor = primaryColor,
                                    unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f),
                                    focusedLabelColor = primaryColor
                                ),
                                singleLine = true,
                                shape = RoundedCornerShape(8.dp)
                            )

                            OutlinedTextField(
                                value = precio,
                                onValueChange = { if (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d*\$"))) precio = it },
                                label = { Text("Precio") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth(),
                                colors = TextFieldDefaults.outlinedTextFieldColors(
                                    focusedBorderColor = primaryColor,
                                    unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f),
                                    focusedLabelColor = primaryColor
                                ),
                                prefix = {
                                    Text(
                                        text = "$",
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = primaryColor
                                    )
                                },
                                singleLine = true,
                                shape = RoundedCornerShape(8.dp)
                            )

                            // Nuevo campo: Fecha del pedido
                            OutlinedTextField(
                                value = fechaPedido,
                                onValueChange = { }, // No permitir edición manual
                                label = { Text("Fecha del pedido") },
                                modifier = Modifier.fillMaxWidth(),
                                colors = TextFieldDefaults.outlinedTextFieldColors(
                                    focusedBorderColor = primaryColor,
                                    unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f),
                                    focusedLabelColor = primaryColor
                                ),
                                readOnly = true,
                                trailingIcon = {
                                    IconButton(
                                        onClick = { showDatePicker = true },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CalendarToday,
                                            contentDescription = "Seleccionar fecha",
                                            tint = primaryColor
                                        )
                                    }
                                },
                                shape = RoundedCornerShape(8.dp)
                            )
                        }
                    }

                    // Botón de guardar
                    Button(
                        onClick = {
                            val precioDouble = precio.toDoubleOrNull()
                            when {
                                nombre.isEmpty() -> {
                                    errorMessage = "El nombre no puede estar vacío"
                                    showErrorDialog = true
                                }
                                precioDouble == null || precioDouble <= 0.0 -> {
                                    errorMessage = "Ingrese un precio válido"
                                    showErrorDialog = true
                                }
                                fechaPedido.isEmpty() -> {
                                    errorMessage = "Seleccione una fecha válida"
                                    showErrorDialog = true
                                }
                                else -> {
                                    scope.launch(Dispatchers.IO) {
                                        try {
                                            val productoActualizado = Producto(
                                                id = productoId,
                                                clienteId = clienteId,
                                                nombre = nombre,
                                                precio = precioDouble,
                                                fotoUri = fotoUri,
                                                fechaPedido = fechaPedido // Nuevo campo
                                            )

                                            db.productoDao().updateProducto(productoActualizado)

                                            withContext(Dispatchers.Main) {
                                                Toast.makeText(
                                                    context,
                                                    "Producto actualizado con éxito",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                                onProductoActualizado()
                                            }
                                        } catch (e: Exception) {
                                            withContext(Dispatchers.Main) {
                                                errorMessage = "Error al actualizar: ${e.message}"
                                                showErrorDialog = true
                                            }
                                        }
                                    }
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .padding(horizontal = 16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = successColor,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(8.dp),
                        elevation = ButtonDefaults.buttonElevation(
                            defaultElevation = 4.dp,
                            pressedElevation = 8.dp
                        )
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_save),
                            contentDescription = "Guardar",
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Guardar Cambios",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
