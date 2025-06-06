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
import com.example.pedidosshein.data.entities.Cliente
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class EditarClienteActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val clienteId = intent.getIntExtra("CLIENTE_ID", -1)
        setContent {
            EditarClienteScreen(clienteId = clienteId) {
                setResult(RESULT_OK)
                finish()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditarClienteScreen(clienteId: Int, onClienteActualizado: () -> Unit) {
    val context = LocalContext.current
    val db = remember { AppDatabase.getInstance(context) }
    val scope = rememberCoroutineScope()

    // Paleta de colores
    val primaryColor = colorResource(id = R.color.purple_500)
    val background = colorResource(id = R.color.background)
    val surface = colorResource(id = R.color.surface)
    val errorColor = colorResource(id = R.color.red_400)

    var nombre by remember { mutableStateOf("") }
    var telefono by remember { mutableStateOf("") }

    // Cargar cliente
    LaunchedEffect(clienteId) {
        scope.launch(Dispatchers.IO) {
            val cliente = db.clienteDao().getClienteById(clienteId)
            cliente?.let {
                nombre = it.nombre.toString()
                telefono = it.telefono.toString()
            }
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = background
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            "Editar Cliente",
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
                            value = nombre,
                            onValueChange = { nombre = it },
                            label = { Text("Nombre del cliente") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = TextFieldDefaults.outlinedTextFieldColors(
                                focusedBorderColor = primaryColor,
                                unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f)
                            )
                        )

                        OutlinedTextField(
                            value = telefono,
                            onValueChange = { telefono = it },
                            label = { Text("Teléfono") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth(),
                            colors = TextFieldDefaults.outlinedTextFieldColors(
                                focusedBorderColor = primaryColor,
                                unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f)
                            )
                        )
                    }
                }

                Button(
                    onClick = {
                        if (nombre.isBlank()) {
                            Toast.makeText(context, "El nombre no puede estar vacío", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        val cliente = Cliente(id = clienteId, nombre = nombre, telefono = telefono)
                        scope.launch(Dispatchers.IO) {
                            db.clienteDao().updateCliente(cliente)
                            launch(Dispatchers.Main) {
                                Toast.makeText(context, "Cliente actualizado", Toast.LENGTH_SHORT).show()
                                onClienteActualizado()
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

                Button(
                    onClick = {
                        mostrarDialogoEliminacion(context) {
                            scope.launch(Dispatchers.IO) {
                                try {
                                    db.productoDao().deleteProductoByClienteId(clienteId)
                                    db.abonoDao().deleteAbonoByClienteId(clienteId)
                                    db.clienteDao().deleteClienteById(clienteId)
                                    launch(Dispatchers.Main) {
                                        Toast.makeText(context, "Cliente eliminado", Toast.LENGTH_SHORT).show()
                                        onClienteActualizado()
                                    }
                                } catch (e: Exception) {
                                    launch(Dispatchers.Main) {
                                        Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = errorColor,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Eliminar Cliente", style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }
}

fun mostrarDialogoEliminacion(context: android.content.Context, onConfirmar: () -> Unit) {
    androidx.appcompat.app.AlertDialog.Builder(context)
        .setTitle("Eliminar Cliente")
        .setMessage("¿Estás seguro de que deseas eliminar este cliente? Se eliminarán todos los datos asociados.")
        .setPositiveButton("Sí") { _, _ -> onConfirmar() }
        .setNegativeButton("No", null)
        .show()
}