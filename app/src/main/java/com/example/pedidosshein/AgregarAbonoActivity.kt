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
import com.example.pedidosshein.data.entities.Abono
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

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
    var fecha by remember { mutableStateOf(getCurrentDate()) }

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

            OutlinedTextField(
                value = fecha,
                onValueChange = { fecha = it },
                label = { Text("Fecha (yyyy-MM-dd)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            Button(onClick = {
                val montoDouble = monto.toDoubleOrNull()
                if (montoDouble != null && fecha.isNotEmpty()) {
                    val abono = Abono(clienteId = clienteId, monto = montoDouble, fecha = fecha)
                    scope.launch(Dispatchers.IO) {
                        db.abonoDao().insertAbono(abono)
                        launch(Dispatchers.Main) {
                            Toast.makeText(context, "Abono agregado", Toast.LENGTH_SHORT).show()
                            onAbonoAgregado()
                        }
                    }
                } else {
                    Toast.makeText(context, "Por favor, ingrese un monto válido", Toast.LENGTH_SHORT).show()
                }
            }) {
                Text("Agregar Abono")
            }
        }
    }
}

fun getCurrentDate(): String {
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    return sdf.format(Date())
}
