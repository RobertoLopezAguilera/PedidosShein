package com.example.pedidosshein

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
    var clienteId by remember { mutableStateOf(0) }
    var cargando by remember { mutableStateOf(true) }

    // Cargar abono al iniciar
    LaunchedEffect(abonoId) {
        scope.launch(Dispatchers.IO) {
            val abono = db.abonoDao().getAbonoById(abonoId)
            if (abono != null) {
                monto = abono.monto.toString()
                fecha = abono.fecha.toString()
                clienteId = abono.clienteId
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

                            OutlinedTextField(
                                value = fecha,
                                onValueChange = { fecha = it },
                                label = { Text("Fecha (yyyy-MM-dd)") },
                                modifier = Modifier.fillMaxWidth(),
                                colors = TextFieldDefaults.outlinedTextFieldColors(
                                    focusedBorderColor = primaryColor,
                                    unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f)
                                ),
                                placeholder = {
                                    Text(text = "Ej: 2023-05-15")
                                }
                            )
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
                                        "La fecha no puede estar vacía",
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
                                                fecha = fecha
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
}