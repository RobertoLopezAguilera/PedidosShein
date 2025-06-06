package com.example.pedidosshein

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.pedidosshein.data.database.AppDatabase
import com.example.pedidosshein.data.entities.toAbono
import com.example.pedidosshein.data.entities.toCliente
import com.example.pedidosshein.data.entities.toMap
import com.example.pedidosshein.data.entities.toProducto
import com.example.pedidosshein.utils.ExcelExporter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class PerfilActivity : AppCompatActivity() {
    private lateinit var db: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportActionBar?.hide() // 👈 Oculta la ActionBar de AppCompatActivity

        db = AppDatabase.getInstance(this)

        setContent {
            PerfilScreen(
                onRespaldar = { respaldarBaseDeDatos() },
                onRestaurar = { restaurarBaseDeDatos() },
                onExportar = { verificarPermisosYExportar() },
                onNavigateTo = { route ->
                    when (route) {
                        "clientes" -> startActivity(Intent(this, MainActivity::class.java))
                        "productos" -> startActivity(Intent(this, ProductosActivity::class.java))
                        "abonos" -> startActivity(Intent(this, AbonoActivity::class.java))
                        "perfil" -> { /* Ya estás aquí */ }
                    }
                }
            )
        }
    }


    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun PerfilScreen(
        onRespaldar: () -> Unit,
        onRestaurar: () -> Unit,
        onExportar: () -> Unit,
        onNavigateTo: (String) -> Unit
    ) {
        // Paleta de colores
        val primaryColor = colorResource(id = R.color.purple_500)
        val primaryContainer = colorResource(id = R.color.purple_200)
        val background = colorResource(id = R.color.background)
        val surface = colorResource(id = R.color.surface)
        val onSurface = colorResource(id = R.color.on_surface)
        val successColor = colorResource(id = R.color.green_400)
        val infoColor = colorResource(id = R.color.blue_400)
        val warningColor = colorResource(id = R.color.orange_400)

        // Estado para diálogos de confirmación
        var showBackupConfirmation by remember { mutableStateOf(false) }
        var showRestoreConfirmation by remember { mutableStateOf(false) }
        var showExportConfirmation by remember { mutableStateOf(false) }

        // Diálogos de confirmación
        if (showBackupConfirmation) {
            AlertDialog(
                onDismissRequest = { showBackupConfirmation = false },
                title = { Text("Confirmar Respaldo") },
                text = { Text("¿Estás seguro de que deseas respaldar todos los datos a la nube?") },
                confirmButton = {
                    Button(
                        onClick = {
                            showBackupConfirmation = false
                            onRespaldar()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = primaryColor)
                    ) {
                        Text("Confirmar")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showBackupConfirmation = false },
                        colors = ButtonDefaults.textButtonColors(contentColor = onSurface)
                    ) {
                        Text("Cancelar")
                    }
                },
                containerColor = surface
            )
        }

        if (showRestoreConfirmation) {
            AlertDialog(
                onDismissRequest = { showRestoreConfirmation = false },
                title = { Text("Confirmar Restauración") },
                text = { Text("¿Estás seguro de que deseas restaurar los datos desde la nube? Esto sobrescribirá los datos locales.") },
                confirmButton = {
                    Button(
                        onClick = {
                            showRestoreConfirmation = false
                            onRestaurar()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = warningColor)
                    ) {
                        Text("Confirmar")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showRestoreConfirmation = false },
                        colors = ButtonDefaults.textButtonColors(contentColor = onSurface)
                    ) {
                        Text("Cancelar")
                    }
                },
                containerColor = surface
            )
        }

        if (showExportConfirmation) {
            AlertDialog(
                onDismissRequest = { showExportConfirmation = false },
                title = { Text("Confirmar Exportación") },
                text = { Text("¿Estás seguro de que deseas exportar todos los datos a un archivo Excel?") },
                confirmButton = {
                    Button(
                        onClick = {
                            showExportConfirmation = false
                            onExportar()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = infoColor)
                    ) {
                        Text("Confirmar")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showExportConfirmation = false },
                        colors = ButtonDefaults.textButtonColors(contentColor = onSurface)
                    ) {
                        Text("Cancelar")
                    }
                },
                containerColor = surface
            )
        }

        Scaffold(
            topBar = {
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
                                "Mi Perfil",  // Título único
                                color = Color.White,
                                style = MaterialTheme.typography.titleLarge
                            )
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = Color.Transparent,
                            actionIconContentColor = Color.White
                        )
                    )
                }
            },
            bottomBar = {
                NavigationBar(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(8.dp),
                    containerColor = surface
                ) {
                    NavigationBarItem(
                        selected = false,
                        onClick = { onNavigateTo("clientes") },
                        icon = {
                            Icon(
                                painter = painterResource(id = R.drawable.clientes),
                                contentDescription = "Clientes",
                                modifier = Modifier.size(24.dp),
                                tint = primaryColor
                            )
                        },
                        label = {
                            Text(
                                text = "Clientes",
                                color = primaryColor,
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    )
                    NavigationBarItem(
                        selected = false,
                        onClick = { onNavigateTo("productos") },
                        icon = {
                            Icon(
                                painter = painterResource(id = R.drawable.pedidos),
                                contentDescription = "Productos",
                                modifier = Modifier.size(24.dp),
                                tint = primaryColor
                            )
                        },
                        label = {
                            Text(
                                text = "Productos",
                                color = primaryColor,
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    )
                    NavigationBarItem(
                        selected = false,
                        onClick = { onNavigateTo("abonos") },
                        icon = {
                            Icon(
                                painter = painterResource(id = R.drawable.abonos),
                                contentDescription = "Abonos",
                                modifier = Modifier.size(24.dp),
                                tint = primaryColor
                            )
                        },
                        label = {
                            Text(
                                text = "Abonos",
                                color = primaryColor,
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    )
                    NavigationBarItem(
                        selected = true,
                        onClick = { /* Ya estamos aquí */ },
                        icon = {
                            Icon(
                                painter = painterResource(id = R.drawable.iconamoonprofilecirclefill),
                                contentDescription = "Perfil",
                                modifier = Modifier.size(24.dp),
                                tint = primaryColor
                            )
                        },
                        label = {
                            Text(
                                text = "Perfil",
                                color = primaryColor,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    )
                }
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize()
                    .background(background),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Sección de información del usuario
                val currentUser = FirebaseAuth.getInstance().currentUser
                val email = currentUser?.email ?: "Usuario no identificado"

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = surface)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(64.dp)
                                    .background(
                                        color = primaryColor.copy(alpha = 0.2f),
                                        shape = CircleShape
                                    )
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.iconamoonprofilecirclefill),
                                    contentDescription = "Perfil",
                                    modifier = Modifier.size(36.dp),
                                    tint = primaryColor
                                )
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            Column {
                                Text(
                                    text = "Bienvenido",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = onSurface.copy(alpha = 0.6f)
                                )
                                Text(
                                    text = email,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }

                // Sección de acciones (sin título redundante)
                FilledTonalButton(
                    onClick = { showBackupConfirmation = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = infoColor.copy(alpha = 0.2f),
                        contentColor = infoColor
                    )
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_backup),
                        contentDescription = "Respaldar",
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Respaldar a la Nube")
                }

                FilledTonalButton(
                    onClick = { showRestoreConfirmation = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = warningColor.copy(alpha = 0.2f),
                        contentColor = warningColor
                    )
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_restore),
                        contentDescription = "Restaurar",
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Restaurar desde la Nube")
                }

                FilledTonalButton(
                    onClick = { showExportConfirmation = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = successColor.copy(alpha = 0.2f),
                        contentColor = successColor
                    )
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.icon_excel),
                        contentDescription = "Exportar",
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Exportar a Excel")
                }

                Spacer(modifier = Modifier.weight(1f))
            }
        }
    }

    private fun respaldarBaseDeDatos() {
        val dbFirebase = FirebaseFirestore.getInstance()
        val currentUser = FirebaseAuth.getInstance().currentUser
        val userId = currentUser?.uid ?: return

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val clientes = db.clienteDao().getAllClientes()
                clientes.forEach { cliente ->
                    dbFirebase.collection("Usuarios").document(userId)
                        .collection("Clientes").document(cliente.id.toString()).set(cliente.toMap())
                }

                val abonos = db.abonoDao().getAllAbonos()
                abonos.forEach { abono ->
                    dbFirebase.collection("Usuarios").document(userId)
                        .collection("Abonos").document(abono.id.toString()).set(abono.toMap())
                }

                val productos = db.productoDao().getAllProductos()
                productos.forEach { producto ->
                    dbFirebase.collection("Usuarios").document(userId)
                        .collection("Productos").document(producto.id.toString()).set(producto.toMap())
                }

                withContext(Dispatchers.Main) {
                    Toast.makeText(this@PerfilActivity, "Base de datos respaldada exitosamente", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@PerfilActivity, "Error al respaldar la base de datos: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun restaurarBaseDeDatos() {
        val dbFirebase = FirebaseFirestore.getInstance()
        val currentUser = FirebaseAuth.getInstance().currentUser
        val userId = currentUser?.uid ?: return

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val clientesSnapshot = dbFirebase.collection("Usuarios").document(userId)
                    .collection("Clientes").get().await()
                val clientes = clientesSnapshot.documents.mapNotNull { it.data?.toCliente() }
                db.clienteDao().insertAll(clientes)

                val abonosSnapshot = dbFirebase.collection("Usuarios").document(userId)
                    .collection("Abonos").get().await()
                val abonos = abonosSnapshot.documents.mapNotNull { it.data?.toAbono() }
                db.abonoDao().insertAll(abonos)

                val productosSnapshot = dbFirebase.collection("Usuarios").document(userId)
                    .collection("Productos").get().await()
                val productos = productosSnapshot.documents.mapNotNull { it.data?.toProducto() }
                db.productoDao().insertAll(productos)

                withContext(Dispatchers.Main) {
                    Toast.makeText(this@PerfilActivity, "Base de datos restaurada exitosamente", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@PerfilActivity, "Error al restaurar la base de datos: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun verificarPermisosYExportar() {
        val permiso = Manifest.permission.WRITE_EXTERNAL_STORAGE
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            when {
                ContextCompat.checkSelfPermission(this, permiso) == PackageManager.PERMISSION_GRANTED -> {
                    exportarDatosAExcel()
                }
                shouldShowRequestPermissionRationale(permiso) -> {
                    Toast.makeText(this, "Se requiere permiso para exportar", Toast.LENGTH_SHORT).show()
                }
                else -> {
                    requestPermissions(arrayOf(permiso), REQUEST_CODE_WRITE_EXTERNAL)
                }
            }
        } else {
            exportarDatosAExcel()
        }
    }

    private fun exportarDatosAExcel() {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                ExcelExporter.exportToExcel(this@PerfilActivity)
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@PerfilActivity, "Datos exportados exitosamente", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@PerfilActivity, "Error al exportar: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    companion object {
        private const val REQUEST_CODE_WRITE_EXTERNAL = 100
    }
}
