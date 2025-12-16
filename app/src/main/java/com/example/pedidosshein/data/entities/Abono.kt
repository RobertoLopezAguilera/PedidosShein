package com.example.pedidosshein.data.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "Abono_table")
data class Abono(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "abono_id") val id: Int = 0,
    @ColumnInfo(name = "cliente_id") val clienteId: Int,
    @ColumnInfo(name = "monto") val monto: Double,
    @ColumnInfo(name = "fecha") val fecha: String?,
    @ColumnInfo(name = "fecha_producto_pedido") val fechaProductoPedido: String? = null
)

fun Abono.toMap(): Map<String, Any> {
    return hashMapOf(
        "id" to id.toLong(), // Convertir a Long para Firestore
        "clienteId" to clienteId.toLong(),
        "monto" to monto,
        "fecha" to (fecha ?: ""),
        "fechaProductoPedido" to (fechaProductoPedido ?: "")
    )
}

fun Map<String, Any>.toAbono(): Abono {
    return Abono(
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
        monto = (this["monto"] as? Double) ?: (this["monto"] as? Long)?.toDouble() ?: 0.0,
        fecha = (this["fecha"] as? String).takeIf { !it.isNullOrEmpty() },
        fechaProductoPedido = (this["fechaProductoPedido"] as? String).takeIf { !it.isNullOrEmpty() }
    )
}