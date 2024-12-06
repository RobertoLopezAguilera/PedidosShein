package com.example.pedidosshein

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.pedidosshein.data.database.AppDatabase
import com.example.pedidosshein.data.entities.Abono
import com.example.pedidosshein.databinding.ActivityAgregarAbonoBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class AgregarAbonoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAgregarAbonoBinding
    private lateinit var db: AppDatabase
    private var clienteId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAgregarAbonoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = AppDatabase.getInstance(this)

        // Obtener el ID del cliente desde el Intent
        clienteId = intent.getIntExtra("CLIENTE_ID", -1)

        // Asignar la fecha actual al campo de fecha
        val currentDate = getCurrentDate()
        binding.etAbonoFecha.setText(currentDate)

        // Manejar la acción de agregar abono
        binding.btnAgregarAbono.setOnClickListener {
            val monto = binding.etAbonoMonto.text.toString().toDoubleOrNull()
            val fecha = binding.etAbonoFecha.text.toString()

            if (monto != null && fecha.isNotEmpty()) {
                // Crear el nuevo abono y guardarlo en la base de datos
                val abono = Abono(clienteId = clienteId, monto = monto, fecha = fecha)

                GlobalScope.launch(Dispatchers.IO) {
                    db.abonoDao().insertAbono(abono)
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@AgregarAbonoActivity, "Abono agregado", Toast.LENGTH_SHORT).show()
                        finish() // Cerrar la actividad después de agregar el abono
                    }
                }
            } else {
                Toast.makeText(this, "Por favor, ingrese un monto válido", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun getCurrentDate(): String {
        // Obtener la fecha actual en formato "yyyy-MM-dd"
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(Date()) // Devuelve la fecha actual como string
    }
}
