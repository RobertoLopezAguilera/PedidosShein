package com.example.pedidosshein

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.example.pedidosshein.data.database.AppDatabase
import com.example.pedidosshein.data.entities.Producto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import android.content.Context
import android.net.Uri
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class AgregarProductoActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val clienteId = intent.getIntExtra("CLIENTE_ID", -1)

        setContent {
            AgregarProductoScreen(clienteId = clienteId, onProductoAgregado = {
                finish()
            })
        }
    }
}

@Composable
fun AgregarProductoScreen(clienteId: Int, onProductoAgregado: () -> Unit) {
    val context = LocalContext.current
    val db = remember { AppDatabase.getInstance(context) }
    val scope = rememberCoroutineScope()

    var nombre by remember { mutableStateOf("") }
    var precio by remember { mutableStateOf("") }
    var fotoUri by remember { mutableStateOf<Uri?>(null) }

    // Obtener la fecha actual por defecto
    val calendar = remember { Calendar.getInstance() }
    var fechaPedido by remember {
        mutableStateOf(
            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)
        )
    }

    var showDatePicker by remember { mutableStateOf(false) }

    // Launcher para abrir galería
    val pickImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        fotoUri = uri
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Agregar Producto") })
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            OutlinedTextField(
                value = nombre,
                onValueChange = { nombre = it },
                label = { Text("Nombre del producto") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = precio,
                onValueChange = { precio = it },
                label = { Text("Precio") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            // Campo para fecha del pedido
            OutlinedTextField(
                value = fechaPedido,
                onValueChange = { }, // No permitir edición manual
                label = { Text("Fecha del pedido") },
                modifier = Modifier.fillMaxWidth(),
                readOnly = true,
                trailingIcon = {
                    IconButton(onClick = { showDatePicker = true }) {
                        Icon(Icons.Default.CalendarToday, contentDescription = "Seleccionar fecha")
                    }
                }
            )

            // Mostrar imagen seleccionada si existe
            fotoUri?.let {
                Image(
                    painter = rememberAsyncImagePainter(it),
                    contentDescription = "Imagen seleccionada",
                    modifier = Modifier
                        .size(400.dp)
                        .padding(8.dp)
                )
            }

            Button(onClick = {
                pickImageLauncher.launch("image/*")
            }) {
                Text("Seleccionar Imagen")
            }

            Button(
                onClick = {
                    val precioDouble = precio.toDoubleOrNull()
                    if (nombre.isNotEmpty() && precioDouble != null) {
                        // Copiar imagen localmente si se seleccionó
                        val rutaLocal = fotoUri?.let { copiarImagenAlmacenamientoInterno(context, it) }

                        val producto = Producto(
                            clienteId = clienteId,
                            nombre = nombre,
                            precio = precioDouble,
                            fotoUri = rutaLocal,
                            fechaPedido = fechaPedido // Nuevo campo agregado
                        )

                        scope.launch(Dispatchers.IO) {
                            db.productoDao().insertProducto(producto)
                            launch(Dispatchers.Main) {
                                Toast.makeText(context, "Producto agregado", Toast.LENGTH_SHORT).show()
                                onProductoAgregado()
                            }
                        }
                    } else {
                        Toast.makeText(context, "Ingrese un nombre y precio válido", Toast.LENGTH_SHORT).show()
                    }
                }
            ) {
                Text("Agregar Producto")
            }
        }
    }

    // DatePicker Dialog
    if (showDatePicker) {
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
}

fun copiarImagenAlmacenamientoInterno(context: Context, uri: Uri): String? {
    return try {
        val inputStream = context.contentResolver.openInputStream(uri)
        val nombreArchivo = "producto_${System.currentTimeMillis()}.jpg"
        val archivo = File(context.filesDir, nombreArchivo)

        inputStream?.use { input ->
            archivo.outputStream().use { output ->
                input.copyTo(output)
            }
        }

        archivo.absolutePath // Ruta segura y accesible
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}