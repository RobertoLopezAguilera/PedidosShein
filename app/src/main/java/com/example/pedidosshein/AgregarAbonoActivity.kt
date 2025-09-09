package com.example.pedidosshein

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.pedidosshein.data.database.AppDatabase
import com.example.pedidosshein.data.entities.Abono
import com.example.pedidosshein.data.entities.Producto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*
import com.example.pedidosshein.ui.theme.*

class AgregarAbonoActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val clienteId = intent.getIntExtra("CLIENTE_ID", -1)

        setContent {
            AgregarAbonoScreen(clienteId = clienteId) {
                setResult(RESULT_OK)
                finish()
            }
        }
    }
}

@Composable
fun AgregarAbonoScreen(clienteId: Int, onAbonoAgregado: () -> Unit) {
    val context = LocalContext.current
    val db = remember { AppDatabase.getInstance(context) }
    val scope = rememberCoroutineScope()

    var monto by remember { mutableStateOf("") }
    var fechaAbono by remember { mutableStateOf(getCurrentDate()) }
    var showDatePicker by remember { mutableStateOf(false) }

    // Estado para las fechas únicas de productos
    var fechasUnicas by remember { mutableStateOf<List<String>>(emptyList()) }
    var fechaSeleccionada by remember { mutableStateOf<String?>(null) }

    // Estado para los productos de la fecha seleccionada
    var productosPorFecha by remember { mutableStateOf<List<Producto>>(emptyList()) }

    // Función para cargar productos por fecha
    fun cargarProductosPorFecha(fecha: String) {
        scope.launch(Dispatchers.IO) {
            val productos = db.productoDao().getProductosByClienteIdAndFecha(clienteId, fecha)
            withContext(Dispatchers.Main) {
                productosPorFecha = productos
            }
        }
    }

    // Cargar fechas únicas al iniciar
    LaunchedEffect(clienteId) {
        scope.launch(Dispatchers.IO) {
            val productos = db.productoDao().getProductosByClienteId(clienteId)
            // Obtener fechas únicas (excluyendo nulls y vacíos)
            val fechas = productos
                .mapNotNull { it.fechaPedido }
                .filter { it.isNotEmpty() }
                .distinct()
                .sorted()

            withContext(Dispatchers.Main) {
                fechasUnicas = fechas
                // Seleccionar la última fecha por defecto, o fecha actual si no hay
                fechaSeleccionada = fechas.lastOrNull() ?: getCurrentDate()
                fechaSeleccionada?.let { cargarProductosPorFecha(it) }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Agregar Abono") })
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
                value = monto,
                onValueChange = { monto = it },
                label = { Text("Monto") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            // Fecha del abono
            OutlinedTextField(
                value = fechaAbono,
                onValueChange = { fechaAbono = it },
                label = { Text("Fecha del abono (yyyy-MM-dd)") },
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    IconButton(onClick = { showDatePicker = true }) {
                        Icon(Icons.Default.CalendarToday, contentDescription = "Seleccionar fecha")
                    }
                }
            )

            // Selector de fecha del pedido
            if (fechasUnicas.isNotEmpty()) {
                Text("Seleccionar fecha del pedido:", style = MaterialTheme.typography.bodyMedium)

                // Dropdown para seleccionar fecha
                var expanded by remember { mutableStateOf(false) }
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = fechaSeleccionada ?: "",
                        onValueChange = { },
                        label = { Text("Fecha del pedido") },
                        modifier = Modifier.fillMaxWidth(),
                        readOnly = true,
                        trailingIcon = {
                            Icon(Icons.Default.ArrowDropDown, contentDescription = "Seleccionar fecha")
                        }
                    )
                    Spacer(
                        modifier = Modifier
                            .matchParentSize()
                            .alpha(0f)
                            .clickable { expanded = true }
                    )
                }

                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    fechasUnicas.forEach { fecha ->
                        DropdownMenuItem(
                            onClick = {
                                fechaSeleccionada = fecha
                                cargarProductosPorFecha(fecha)
                                expanded = false
                            }
                        ) {
                            Text(text = fecha)
                        }
                    }
                }
            } else {
                // Si no hay fechas, mostrar fecha actual
                OutlinedTextField(
                    value = fechaSeleccionada ?: getCurrentDate(),
                    onValueChange = { },
                    label = { Text("Fecha del pedido") },
                    modifier = Modifier.fillMaxWidth(),
                    readOnly = true,
                    trailingIcon = {
                        IconButton(onClick = { showDatePicker = true }) {
                            Icon(Icons.Default.CalendarToday, contentDescription = "Seleccionar fecha")
                        }
                    }
                )
            }

            // Mostrar productos de la fecha seleccionada
            if (productosPorFecha.isNotEmpty()) {
                Text(
                    "Productos en esta fecha:",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 200.dp) // Limitar altura máxima
                ) {
                    items(productosPorFecha) { producto ->
                        ProductoItem(producto = producto)
                    }
                }
            }

            Button(
                onClick = {
                    val montoDouble = monto.toDoubleOrNull()
                    if (montoDouble != null && fechaAbono.isNotEmpty()) {
                        val abono = Abono(
                            clienteId = clienteId,
                            monto = montoDouble,
                            fecha = fechaAbono,
                            fechaProductoPedido = fechaSeleccionada // Nuevo campo
                        )
                        scope.launch(Dispatchers.IO) {
                            db.abonoDao().insertAbono(abono)
                            launch(Dispatchers.Main) {
                                Toast.makeText(context, "Abono agregado para el pedido del $fechaSeleccionada", Toast.LENGTH_SHORT).show()
                                onAbonoAgregado()
                            }
                        }
                    } else {
                        Toast.makeText(context, "Por favor, ingrese un monto válido", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Agregar Abono")
            }
        }
    }

    // DatePicker Dialog para fecha del abono
    if (showDatePicker) {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        DatePickerDialog(
            context,
            { _, selectedYear, selectedMonth, selectedDay ->
                val selectedDate = Calendar.getInstance().apply {
                    set(selectedYear, selectedMonth, selectedDay)
                }
                fechaAbono = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    .format(selectedDate.time)
                showDatePicker = false
            },
            year,
            month,
            day
        ).show()
    }
}

// Composable para mostrar cada producto
@Composable
fun ProductoItem(producto: Producto) {
    val primaryColor = colorResource(id = R.color.purple_500)
    val onSurface = colorResource(id = R.color.on_surface)
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
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
                text = formatCurrency(producto.precio), // Ahora mostrará con $
                style = typography.bodyLarge,
                color = primaryColor,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

// Funciones de utilidad
fun getCurrentDate(): String {
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    return sdf.format(Date())
}

// Cambiado de "Q" a "$"
fun formatCurrency(amount: Double): String {
    return String.format(Locale.getDefault(), "$%.2f", amount)
}