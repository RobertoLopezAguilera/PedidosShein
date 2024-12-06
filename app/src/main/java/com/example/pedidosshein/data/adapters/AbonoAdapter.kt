package com.example.pedidosshein.data.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.pedidosshein.data.entities.Abono
import com.example.pedidosshein.databinding.ItemAbonoBinding

class AbonoAdapter(
    private var abonos: List<Abono>,
    private val onAbonoClick: (Abono) -> Unit // Callback para manejar clics
) : RecyclerView.Adapter<AbonoAdapter.AbonoViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AbonoViewHolder {
        val binding = ItemAbonoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AbonoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AbonoViewHolder, position: Int) {
        val abono = abonos[position]
        holder.bind(abono, onAbonoClick)
    }

    override fun getItemCount(): Int = abonos.size

    // Función para actualizar la lista de abonos
    fun setAbonos(abonos: List<Abono>) {
        this.abonos = abonos
        notifyDataSetChanged()
    }

    class AbonoViewHolder(private val binding: ItemAbonoBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(abono: Abono, onAbonoClick: (Abono) -> Unit) {
            // Mostrar los datos del abono (monto y fecha)
            binding.tvAbonoMonto.text = "\$${"%.2f".format(abono.monto)}"
            binding.tvAbonoFecha.text = abono.fecha

            // Detectar clics y ejecutar el callback
            binding.root.setOnClickListener {
                onAbonoClick(abono)
            }
        }
    }
}
