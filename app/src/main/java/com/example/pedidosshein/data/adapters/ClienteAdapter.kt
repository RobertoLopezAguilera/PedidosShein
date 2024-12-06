package com.example.pedidosshein.data.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.pedidosshein.ClienteDetalleActivity
import com.example.pedidosshein.data.entities.Cliente
import com.example.pedidosshein.databinding.ItemClienteBinding

class ClienteAdapter(private var clientes: List<Cliente>) : RecyclerView.Adapter<ClienteAdapter.ClienteViewHolder>() {

    // Variable para almacenar el listener
    private var listener: ((Cliente) -> Unit)? = null

    // Función para permitir establecer el listener
    fun setOnItemClickListener(listener: (Cliente) -> Unit) {
        this.listener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ClienteViewHolder {
        val binding = ItemClienteBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ClienteViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ClienteViewHolder, position: Int) {
        val cliente = clientes[position]
        holder.bind(cliente)
    }

    override fun getItemCount(): Int = clientes.size

    inner class ClienteViewHolder(private val binding: ItemClienteBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(cliente: Cliente) {
            binding.tvNombre.text = cliente.nombre
            binding.tvTelefono.text = cliente.telefono

            // Se ejecuta cuando el item es clickeado
            binding.root.setOnClickListener {
                listener?.invoke(cliente)  // Llamar al listener con el cliente clickeado
            }
        }
    }

    fun setClientes(clientes: List<Cliente>) {
        this.clientes = clientes
        notifyDataSetChanged()
    }
}
