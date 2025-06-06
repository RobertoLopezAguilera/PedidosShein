package com.example.pedidosshein

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material3.Icon
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.pedidosshein.data.database.AppDatabase
import com.example.pedidosshein.data.entities.Abono
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AbonoDetalleActivity : ComponentActivity() {

    private var abonoId: Int = -1
    private val recargarAbono = mutableStateOf(false)

    private val editarAbonoLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            recargarAbono.value = true
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        abonoId = intent.getIntExtra("ABONO_ID", -1)

        setContent {
            if (abonoId == -1) {
                Toast.makeText(this, "Abono no encontrado", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                AbonoDetalleScreen(
                    abonoId = abonoId,
                    recargarTrigger = recargarAbono.value,
                    onRecargaConsumida = { recargarAbono.value = false },
                    onEditar = { id ->
                        val intent = Intent(this, EditarAbonoActivity::class.java)
                        intent.putExtra("ABONO_ID", id)
                        editarAbonoLauncher.launch(intent)
                    },
                    onEliminar = { finish() }
                )
            }
        }
    }
}

@Composable
fun AbonoDetalleScreen(
    abonoId: Int,
    recargarTrigger: Boolean,
    onRecargaConsumida: () -> Unit,
    onEditar: (Int) -> Unit,
    onEliminar: () -> Unit
) {
    val context = LocalContext.current
    val db = remember { AppDatabase.getInstance(context) }
    val scope = rememberCoroutineScope()

    var abono by remember { mutableStateOf<Abono?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    // Carga inicial
    LaunchedEffect(abonoId) {
        scope.launch(Dispatchers.IO) {
            abono = db.abonoDao().getAbonoById(abonoId)
        }
    }

    // Recarga si fue activado
    LaunchedEffect(recargarTrigger) {
        if (recargarTrigger) {
            scope.launch(Dispatchers.IO) {
                abono = db.abonoDao().getAbonoById(abonoId)
                onRecargaConsumida()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalle del Abono") },
                actions = {
                    IconButton(onClick = { onEditar(abonoId) }) {
                        Icon(
                            painter = painterResource(id = R.drawable.icon_editar),
                            contentDescription = "Editar abono",
                            tint = Color.Unspecified
                        )
                    }
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(
                            painter = painterResource(id = R.drawable.borrar_cliente),
                            contentDescription = "Eliminar abono",
                            tint = Color.Unspecified
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        abono?.let { abn ->
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .padding(16.dp)
                    .fillMaxSize(),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Monto: \$${"%.2f".format(abn.monto)}",
                    style = MaterialTheme.typography.h5,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Text(
                    text = "Fecha: ${abn.fecha}",
                    style = MaterialTheme.typography.body1
                )
            }
        } ?: Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Eliminar abono") },
            text = { Text("¿Estás seguro de que deseas eliminar este abono?") },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    scope.launch(Dispatchers.IO) {
                        val eliminado = db.abonoDao().deleteAbonoById(abonoId)
                        launch(Dispatchers.Main) {
                            if (eliminado > 0) {
                                Toast.makeText(context, "Abono eliminado", Toast.LENGTH_SHORT).show()
                                onEliminar()
                            } else {
                                Toast.makeText(context, "Error al eliminar abono", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }) {
                    Text("Sí")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("No")
                }
            }
        )
    }
}
