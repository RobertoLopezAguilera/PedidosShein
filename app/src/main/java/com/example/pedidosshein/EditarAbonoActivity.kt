package com.example.pedidosshein

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.pedidosshein.data.database.AppDatabase
import com.example.pedidosshein.data.entities.Abono
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class EditarAbonoActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val abonoId = intent.getIntExtra("ABONO_ID", -1)
        setContent {
            EditarAbonoScreen(abonoId = abonoId) {
                setResult(RESULT_OK)
                finish()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditarAbonoScreen(abonoId: Int, onAbonoActualizado: () -> Unit) {
    val context = LocalContext.current
    val db = remember { AppDatabase.getInstance(context) }
    val scope = rememberCoroutineScope()

    // Paleta de colores
    val primaryColor = colorResource(id = R.color.purple_500)
    val background = colorResource(id = R.color.background)
    val surface = colorResource(id = R.color.surface)
    val errorColor = colorResource(id = R.color.red_400)

    var monto by remember { mutableStateOf("") }
    var fecha by remember { mutableStateOf("") }
    var fechaProductoPedido by remember { mutableStateOf("") }
    var clienteId by remember { mutableStateOf(0) }
    var cargando by remember { mutableStateOf(true) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showDatePickerProducto by remember { mutableStateOf(false) }

    // Estados para las fechas únicas de productos del cliente
    var fechasUnicas by remember { mutableStateOf<List<String>>(emptyList()) }
    var expanded by remember { mutableStateOf(false) }

    // Cargar abono y fechas únicas al iniciar
    LaunchedEffect(abonoId) {
        scope.launch(Dispatchers.IO) {
            val abono = db.abonoDao().getAbonoById(abonoId)
            if (abono != null) {
                monto = abono.monto.toString()
                fecha = abono.fecha.toString()
                fechaProductoPedido = abono.fechaProductoPedido ?: ""
                clienteId = abono.clienteId

                // Cargar fechas únicas de productos del cliente
                val productos = db.productoDao().getProductosByClienteId(clienteId)
                val fechas = productos
                    .mapNotNull { it.fechaPedido }
                    .filter { it.isNotEmpty() }
                    .distinct()
                    .sorted()

                withContext(Dispatchers.Main) {
                    fechasUnicas = fechas
                    // Si no hay fechaProductoPedido, usar la primera fecha disponible o fecha actual
                    if (fechaProductoPedido.isEmpty() && fechas.isNotEmpty()) {
                        fechaProductoPedido = fechas.first()
                    } else if (fechaProductoPedido.isEmpty()) {
                        fechaProductoPedido = getCurrentDate()
                    }
                }
            } else {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Error: Abono no encontrado", Toast.LENGTH_SHORT).show()
                    onAbonoActualizado()
                }
            }
            cargando = false
        }
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
                    TopAppBar(
                        title = {
                            Text(
                                "Editar Abono",
                                color = Color.White,
                                style = MaterialTheme.typography.titleLarge
                            )
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = primaryColor,
                            actionIconContentColor = Color.White
                        )
                    )
                }
            ) { paddingValues ->
                Column(
                    modifier = Modifier
                        .padding(paddingValues)
                        .padding(24.dp)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        colors = CardDefaults.cardColors(containerColor = surface)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            OutlinedTextField(
                                value = monto,
                                onValueChange = { monto = it },
                                label = { Text("Monto") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth(),
                                colors = TextFieldDefaults.outlinedTextFieldColors(
                                    focusedBorderColor = primaryColor,
                                    unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f)
                                ),
                                prefix = {
                                    Text(
                                        text = "$",
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = primaryColor
                                    )
                                }
                            )

                            // Selector de fecha del abono
                            OutlinedTextField(
                                value = fecha,
                                onValueChange = { fecha = it },
                                label = { Text("Fecha del abono (yyyy-MM-dd)") },
                                modifier = Modifier.fillMaxWidth(),
                                colors = TextFieldDefaults.outlinedTextFieldColors(
                                    focusedBorderColor = primaryColor,
                                    unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f)
                                ),
                                trailingIcon = {
                                    IconButton(
                                        onClick = { showDatePicker = true },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CalendarToday,
                                            contentDescription = "Seleccionar fecha del abono",
                                            tint = primaryColor
                                        )
                                    }
                                }
                            )

                            // Selector de fecha del pedido (producto)
                            if (fechasUnicas.isNotEmpty()) {
                                Text(
                                    "Fecha del pedido asociado:",
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.padding(bottom = 4.dp)
                                )

                                Box(modifier = Modifier.fillMaxWidth()) {
                                    OutlinedTextField(
                                        value = fechaProductoPedido,
                                        onValueChange = { },
                                        label = { Text("Fecha del pedido") },
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = TextFieldDefaults.outlinedTextFieldColors(
                                            focusedBorderColor = primaryColor,
                                            unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f)
                                        ),
                                        readOnly = true,
                                        trailingIcon = {
                                            Icon(
                                                Icons.Default.ArrowDropDown,
                                                contentDescription = "Seleccionar fecha del pedido",
                                                tint = primaryColor
                                            )
                                        }
                                    )

                                    // Overlay clickable para abrir el dropdown
                                    Box(
                                        modifier = Modifier
                                            .matchParentSize()
                                            .clickable{ expanded = true }
                                    )
                                }

                                DropdownMenu(
                                    expanded = expanded,
                                    onDismissRequest = { expanded = false }
                                ) {
                                    fechasUnicas.forEach { fecha ->
                                        DropdownMenuItem(
                                            onClick = {
                                                fechaProductoPedido = fecha
                                                expanded = false
                                            },
                                            text = {
                                                Text(text = fecha)
                                            }
                                        )
                                    }
                                }
                            } else {
                                // Si no hay fechas de productos, usar selector de fecha normal
                                OutlinedTextField(
                                    value = fechaProductoPedido,
                                    onValueChange = { },
                                    label = { Text("Fecha del pedido asociado") },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = TextFieldDefaults.outlinedTextFieldColors(
                                        focusedBorderColor = primaryColor,
                                        unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f)
                                    ),
                                    readOnly = true,
                                    trailingIcon = {
                                        IconButton(
                                            onClick = { showDatePickerProducto = true },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.CalendarToday,
                                                contentDescription = "Seleccionar fecha del pedido",
                                                tint = primaryColor
                                            )
                                        }
                                    }
                                )
                            }
                        }
                    }

                    Button(
                        onClick = {
                            val nuevoMonto = monto.toDoubleOrNull()
                            when {
                                nuevoMonto == null || nuevoMonto <= 0.0 -> {
                                    Toast.makeText(
                                        context,
                                        "El monto debe ser un número válido mayor a 0",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                                fecha.isEmpty() -> {
                                    Toast.makeText(
                                        context,
                                        "La fecha del abono no puede estar vacía",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                                fechaProductoPedido.isEmpty() -> {
                                    Toast.makeText(
                                        context,
                                        "La fecha del pedido no puede estar vacía",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                                else -> {
                                    scope.launch(Dispatchers.IO) {
                                        try {
                                            val abono = Abono(
                                                id = abonoId,
                                                clienteId = clienteId,
                                                monto = nuevoMonto,
                                                fecha = fecha,
                                                fechaProductoPedido = fechaProductoPedido // Nuevo campo
                                            )
                                            db.abonoDao().updateAbono(abono)
                                            withContext(Dispatchers.Main) {
                                                Toast.makeText(
                                                    context,
                                                    "Abono actualizado correctamente",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                                onAbonoActualizado()
                                            }
                                        } catch (e: Exception) {
                                            withContext(Dispatchers.Main) {
                                                Toast.makeText(
                                                    context,
                                                    "Error al actualizar: ${e.message}",
                                                    Toast.LENGTH_LONG
                                                ).show()
                                            }
                                        }
                                    }
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = primaryColor,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Guardar Cambios", style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
        }
    }

    // DatePicker Dialog para fecha del abono
    if (showDatePicker) {
        val calendar = Calendar.getInstance()
        try {
            if (fecha.isNotEmpty()) {
                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val date = sdf.parse(fecha)
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
                fecha = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    .format(selectedDate.time)
                showDatePicker = false
            },
            year,
            month,
            day
        ).show()
    }

    // DatePicker Dialog para fecha del pedido (cuando no hay fechas de productos)
    if (showDatePickerProducto) {
        val calendar = Calendar.getInstance()
        try {
            if (fechaProductoPedido.isNotEmpty()) {
                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val date = sdf.parse(fechaProductoPedido)
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
                fechaProductoPedido = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    .format(selectedDate.time)
                showDatePickerProducto = false
            },
            year,
            month,
            day
        ).show()
    }
}