package com.example.pedidosshein

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.pedidosshein.data.database.AppDatabase
import com.example.pedidosshein.data.entities.Abono
import com.example.pedidosshein.databinding.ActivityEditarAbonoBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class EditarAbonoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditarAbonoBinding
    private lateinit var db: AppDatabase
    private var abonoId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inicializar View Binding
        binding = ActivityEditarAbonoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = AppDatabase.getInstance(this)

        // Obtener el ID del abono del Intent
        abonoId = intent.getIntExtra("ABONO_ID", -1)

        if (abonoId != -1) {
            cargarAbono()
        } else {
            Toast.makeText(this, "Error: Abono no encontrado", Toast.LENGTH_SHORT).show()
            finish()
        }

        // Guardar cambios
        binding.btnGuardarAbono.setOnClickListener {
            guardarCambios()
        }
    }

    private fun cargarAbono() {
        GlobalScope.launch(Dispatchers.IO) {
            val abono = db.abonoDao().getAbonoById(abonoId)
            abono?.let {
                withContext(Dispatchers.Main) {
                    binding.etMontoAbono.setText(it.monto.toString())
                    binding.etFechaAbono.setText(it.fecha)
                }
            }
        }
    }

    private fun guardarCambios() {
        val nuevoMontoTexto = binding.etMontoAbono.text.toString().trim()
        val nuevoMonto = nuevoMontoTexto.toDoubleOrNull()
        val nuevaFecha = binding.etFechaAbono.text.toString().trim()

        if (nuevoMonto == null || nuevoMonto <= 0.0) {
            Toast.makeText(this, "El monto debe ser un número válido mayor a 0", Toast.LENGTH_SHORT).show()
            return
        }

        if (nuevaFecha.isEmpty()) {
            Toast.makeText(this, "La fecha no puede estar vacía", Toast.LENGTH_SHORT).show()
            return
        }

        GlobalScope.launch(Dispatchers.IO) {
            try {
                val abono = Abono(
                    id = abonoId,
                    clienteId = db.abonoDao().getAbonoById(abonoId)?.clienteId ?: 0,
                    monto = nuevoMonto,
                    fecha = nuevaFecha
                )
                db.abonoDao().updateAbono(abono)

                withContext(Dispatchers.Main) {
                    Toast.makeText(this@EditarAbonoActivity, "Abono actualizado correctamente", Toast.LENGTH_SHORT).show()

                    // Indicar que el abono fue actualizado
                    setResult(RESULT_OK)
                    finish()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@EditarAbonoActivity, "Error al actualizar el abono: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

}
