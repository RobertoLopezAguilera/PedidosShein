package com.example.pedidosshein

import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.pedidosshein.MainActivity
import com.example.pedidosshein.data.database.AppDatabase
import com.example.pedidosshein.data.entities.toAbono
import com.example.pedidosshein.data.entities.toCliente
import com.example.pedidosshein.data.entities.toMap
import com.example.pedidosshein.data.entities.toProducto
import com.example.pedidosshein.utils.ExcelExporter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class PerfilActivity : AppCompatActivity() {
    private lateinit var db: AppDatabase
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_perfil)

        // Botones
        val btnRespaldar = findViewById<Button>(R.id.btnRespaldar)
        val btnRestaurar = findViewById<Button>(R.id.btnRestaurar)
        val btnExportarExcel = findViewById<Button>(R.id.btnExportarExcel)

        db = AppDatabase.getInstance(this)

        val sharedPreferences = getSharedPreferences("UserSession", Context.MODE_PRIVATE)
        val userEmail = sharedPreferences.getString("USER_EMAIL", null)
        val currentUser = FirebaseAuth.getInstance().currentUser

        val userId = currentUser?.uid
        // Acción para respaldar la base de datos
        btnRespaldar.setOnClickListener {
            showConfirmationDialog(
                title = "Respaldar Base de Datos",
                message = "¿Estás seguro de que deseas respaldar la base de datos?",
                onConfirm = { respaldarBaseDeDatos() }
            )
        }

        // Acción para restaurar la base de datos
        btnRestaurar.setOnClickListener {
            showConfirmationDialog(
                title = "Restaurar Base de Datos",
                message = "Esto sobrescribirá los datos actuales. ¿Deseas continuar?",
                onConfirm = { restaurarBaseDeDatos() }
            )
        }

        // Acción para exportar datos a Excel
        btnExportarExcel.setOnClickListener {
            exportarDatosAExcel()
        }
    }

    // Función para mostrar un cuadro de confirmación
    private fun showConfirmationDialog(title: String, message: String, onConfirm: () -> Unit) {
        AlertDialog.Builder(this).apply {
            setTitle(title)
            setMessage(message)
            setPositiveButton("Sí") { _: DialogInterface, _: Int -> onConfirm() }
            setNegativeButton("No", null)
            create()
            show()
        }
    }

    // Función para respaldar la base de datos
    private fun respaldarBaseDeDatos() {
        val dbFirebase = FirebaseFirestore.getInstance()
        val currentUser = FirebaseAuth.getInstance().currentUser
        val userId = currentUser?.uid ?: return

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                // Subir clientes
                val clientes = db.clienteDao().getAllClientes()
                clientes.forEach { cliente ->
                    dbFirebase.collection("Usuarios").document(userId)
                        .collection("Clientes").document(cliente.id.toString()).set(cliente.toMap())
                }

                // Subir abonos
                val abonos = db.abonoDao().getAllAbonos()
                abonos.forEach { abono ->
                    dbFirebase.collection("Usuarios").document(userId)
                        .collection("Abonos").document(abono.id.toString()).set(abono.toMap())
                }

                // Subir productos
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
                // Restaurar clientes
                val clientesSnapshot = dbFirebase.collection("Usuarios").document(userId)
                    .collection("Clientes").get().await()
                val clientes = clientesSnapshot.documents.mapNotNull { it.data?.toCliente() }
                db.clienteDao().insertAll(clientes)

                // Restaurar abonos
                val abonosSnapshot = dbFirebase.collection("Usuarios").document(userId)
                    .collection("Abonos").get().await()
                val abonos = abonosSnapshot.documents.mapNotNull { it.data?.toAbono() }
                db.abonoDao().insertAll(abonos)

                // Restaurar productos
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

    // Función para exportar datos a Excel
    private fun exportarDatosAExcel() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                ExcelExporter.exportToExcel(this@PerfilActivity)
                runOnUiThread {
                    Toast.makeText(this@PerfilActivity, "Datos exportados a Excel exitosamente", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(this@PerfilActivity, "Error al exportar: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}
