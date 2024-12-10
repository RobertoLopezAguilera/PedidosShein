package com.example.pedidosshein

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.pedidosshein.data.adapters.AbonoAdapter
import com.example.pedidosshein.data.database.AppDatabase
import com.example.pedidosshein.data.entities.Abono
import com.example.pedidosshein.databinding.ActivityAbonoBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AbonoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAbonoBinding
    private lateinit var abonoAdapter: AbonoAdapter
    private lateinit var db: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inicializar View Binding
        binding = ActivityAbonoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = AppDatabase.getInstance(this)

        // Configurar RecyclerView
        binding.rvAbonos.layoutManager = LinearLayoutManager(this)
        abonoAdapter = AbonoAdapter(emptyList()) { abono ->
            onAbonoClick(abono)
        }
        binding.rvAbonos.adapter = abonoAdapter

        // Configurar navegación inferior
        configurarBottomNavigation()

        // Cargar abonos
        cargarAbonos()
    }

    private fun cargarAbonos() {
        GlobalScope.launch(Dispatchers.IO) {
            val abonos = db.abonoDao().getAllAbonos()
            withContext(Dispatchers.Main) {
                abonoAdapter.setAbonos(abonos)
            }
        }
    }

    private fun onAbonoClick(abono: Abono) {
        // Acciones al hacer clic en un abono
        // Por ejemplo, puedes abrir una actividad de detalles de abono
    }

    private fun configurarBottomNavigation() {
        binding.bottomNavigationView.selectedItemId = R.id.menu_abonos // Selecciona "Abonos"

        binding.bottomNavigationView.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.menu_clientes -> {
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    finish() // Opcional: cerrar la actividad actual para evitar acumulación
                    true
                }
                R.id.menu_productos -> {
                    val intent = Intent(this, ProductosActivity::class.java)
                    startActivity(intent)
                    finish() // Opcional
                    true
                }
                R.id.menu_abonos -> {
                    // Ya estamos en la actividad de abonos
                    true
                }
                else -> false
            }
        }
    }
}
