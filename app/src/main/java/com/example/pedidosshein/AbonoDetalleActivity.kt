package com.example.pedidosshein

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.pedidosshein.data.database.AppDatabase
import com.example.pedidosshein.databinding.ActivityAbonoDetalleBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AbonoDetalleActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAbonoDetalleBinding
    private lateinit var db: AppDatabase
    private var abonoId: Int = -1

    // Registrar el resultado de la edición del abono
    private val editarAbonoLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                recargarAbono()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inicializar View Binding
        binding = ActivityAbonoDetalleBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = AppDatabase.getInstance(this)

        // Obtener los datos del Intent
        abonoId = intent.getIntExtra("ABONO_ID", -1)
        if (abonoId == -1) {
            Toast.makeText(this, "Error: Abono no encontrado", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        recargarAbono()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_abono_detalle, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_editar -> {
                editarAbono()
                return true
            }
            R.id.action_eliminar -> {
                eliminarAbono()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun recargarAbono() {
        GlobalScope.launch(Dispatchers.IO) {
            val abono = db.abonoDao().getAbonoById(abonoId)
            withContext(Dispatchers.Main) {
                if (abono != null) {
                    binding.tvAbonoMonto.text = "Monto: \$${"%.2f".format(abono.monto)}"
                    binding.tvAbonoFecha.text = "Fecha: ${abono.fecha}"
                } else {
                    Toast.makeText(this@AbonoDetalleActivity, "Abono no encontrado", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        }
    }

    private fun editarAbono() {
        GlobalScope.launch(Dispatchers.IO) {
            val abono = db.abonoDao().getAbonoById(abonoId)
            withContext(Dispatchers.Main) {
                if (abono != null) {
                    val intent = Intent(this@AbonoDetalleActivity, EditarAbonoActivity::class.java)
                    intent.putExtra("ABONO_ID", abonoId)
                    editarAbonoLauncher.launch(intent) // Usar launcher para iniciar la actividad
                } else {
                    Toast.makeText(this@AbonoDetalleActivity, "Abono no encontrado", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun eliminarAbono() {
        AlertDialog.Builder(this)
            .setTitle("Eliminar Abono")
            .setMessage("¿Estás seguro de que deseas eliminar este abono?")
            .setPositiveButton("Sí") { _, _ ->
                GlobalScope.launch(Dispatchers.IO) {
                    try {
                        val abonosEliminados = db.abonoDao().deleteAbonoById(abonoId)
                        withContext(Dispatchers.Main) {
                            if (abonosEliminados > 0) {
                                Toast.makeText(this@AbonoDetalleActivity, "Abono eliminado", Toast.LENGTH_SHORT).show()
                                finish()
                            } else {
                                Toast.makeText(this@AbonoDetalleActivity, "Error al eliminar el abono", Toast.LENGTH_SHORT).show()
                            }
                        }
                    } catch (e: Exception) {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(this@AbonoDetalleActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
            .setNegativeButton("No", null)
            .show()
    }
}
