package com.example.pedidosshein.data.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Abono_table")
data class Abono(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "abono_id") val id: Int = 0,
    @ColumnInfo(name = "cliente_id") val clienteId: Int,
    @ColumnInfo(name = "monto") val monto: Double,
    @ColumnInfo(name = "fecha") val fecha: String?
)

fun Abono.toMap(): Map<String, Any?> {
    return mapOf(
        "id" to id,
        "clienteId" to clienteId,
        "monto" to monto,
        "fecha" to fecha
    )
}

fun Map<String, Any?>.toAbono(): Abono {
    return Abono(
        id = (this["id"] as Long?)?.toInt() ?: 0,
        clienteId = (this["clienteId"] as Long?)?.toInt() ?: 0,
        monto = this["monto"] as? Double ?: 0.0,
        fecha = this["fecha"] as? String
    )
}
