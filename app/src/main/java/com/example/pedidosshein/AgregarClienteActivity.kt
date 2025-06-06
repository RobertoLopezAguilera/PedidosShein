package com.example.pedidosshein

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.pedidosshein.data.database.AppDatabase
import com.example.pedidosshein.data.entities.Cliente
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AgregarClienteActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AgregarClienteScreen {
                setResult(RESULT_OK)
                finish()
            }
        }
    }
}

@Composable
fun AgregarClienteScreen(onClienteAgregado: () -> Unit) {
    val context = LocalContext.current
    val db = remember { AppDatabase.getInstance(context) }
    val scope = rememberCoroutineScope()

    var nombre by remember { mutableStateOf("") }
    var telefono by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Agregar Cliente") })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            OutlinedTextField(
                value = nombre,
                onValueChange = { nombre = it },
                label = { Text("Nombre") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = telefono,
                onValueChange = { telefono = it },
                label = { Text("Teléfono") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier.fillMaxWidth()
            )

            Button(onClick = {
                if (nombre.isNotEmpty() && telefono.isNotEmpty()) {
                    val cliente = Cliente(0, nombre, telefono)
                    scope.launch(Dispatchers.IO) {
                        db.clienteDao().insertCliente(cliente)
                        launch(Dispatchers.Main) {
                            Toast.makeText(context, "Cliente agregado", Toast.LENGTH_SHORT).show()
                            onClienteAgregado()
                        }
                    }
                } else {
                    Toast.makeText(context, "Por favor, ingrese todos los datos", Toast.LENGTH_SHORT).show()
                }
            }) {
                Text("Agregar Cliente")
            }
        }
    }
}
