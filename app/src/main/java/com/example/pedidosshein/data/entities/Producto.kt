package com.example.pedidosshein.data.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Producto_table")
data class Producto(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "producto_id") val id: Int = 0,
    @ColumnInfo(name = "nombre") val nombre: String?,
    @ColumnInfo(name = "precio") val precio: Double,
    @ColumnInfo(name = "cliente_id") val clienteId: Int,
    @ColumnInfo(name = "foto_uri") val fotoUri: String? = null,
    @ColumnInfo(name = "fecha_pedido") val fechaPedido: String? = null
)

fun Producto.toMap(): Map<String, Any> {
    return hashMapOf(
        "id" to id.toLong(), // Convertir a Long
        "nombre" to (nombre ?: ""),
        "precio" to precio,
        "clienteId" to clienteId.toLong(), // Convertir a Long
        "fotoUri" to (fotoUri ?: ""),
        "fechaPedido" to (fechaPedido ?: "")
    )
}

fun Map<String, Any>.toProducto(): Producto {
    return Producto(
        id = try {
            when (val idValue = this["id"]) {
                is Long -> idValue.toInt()
                is Int -> idValue
                is Double -> idValue.toInt()
                else -> 0
            }
        } catch (e: Exception) {
            0
        },
        nombre = (this["nombre"] as? String).takeIf { !it.isNullOrEmpty() },
        precio = (this["precio"] as? Double) ?: (this["precio"] as? Long)?.toDouble() ?: 0.0,
        clienteId = try {
            when (val clienteIdValue = this["clienteId"]) {
                is Long -> clienteIdValue.toInt()
                is Int -> clienteIdValue
                is Double -> clienteIdValue.toInt()
                else -> 0
            }
        } catch (e: Exception) {
            0
        },
        fotoUri = (this["fotoUri"] as? String).takeIf { !it.isNullOrEmpty() },
        fechaPedido = (this["fechaPedido"] as? String).takeIf { !it.isNullOrEmpty() }
    )
}

// Data class para agrupar pedidos
data class PedidoGrupo(
    val fecha: String,
    val productos: List<Producto>,
    val abonos: List<Abono>,
    val totalProductos: Double,
    val totalAbonos: Double,
    val restante: Double
)