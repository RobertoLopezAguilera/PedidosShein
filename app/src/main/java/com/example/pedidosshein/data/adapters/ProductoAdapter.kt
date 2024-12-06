package com.example.pedidosshein.data.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.pedidosshein.data.entities.Producto
import com.example.pedidosshein.databinding.ItemProductoBinding

class ProductoAdapter(
    private var productos: List<Producto>,
    private val onProductoClick: (Producto) -> Unit // Callback para manejar clics
) : RecyclerView.Adapter<ProductoAdapter.ProductoViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductoViewHolder {
        val binding = ItemProductoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ProductoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ProductoViewHolder, position: Int) {
        val producto = productos[position]
        holder.bind(producto, onProductoClick)
    }

    override fun getItemCount(): Int = productos.size

    fun setProductos(productos: List<Producto>) {
        this.productos = productos
        notifyDataSetChanged()
    }

    class ProductoViewHolder(private val binding: ItemProductoBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(producto: Producto, onProductoClick: (Producto) -> Unit) {
            binding.tvProductoNombre.text = producto.nombre
            binding.tvProductoPrecio.text = "\$${"%.2f".format(producto.precio)}"

            // Detectar clics y ejecutar el callback
            binding.root.setOnClickListener {
                onProductoClick(producto)
            }
        }
    }
}
