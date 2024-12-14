package com.example.pedidosshein.data.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Cliente_table")
data class Cliente(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "cliente_id") val id: Int = 0,
    @ColumnInfo(name = "nombre") val nombre: String?,
    @ColumnInfo(name = "telefono") val telefono: String?
)

// Funciones de extensión para convertir entre Cliente y Map<String, Any>
fun Cliente.toMap(): Map<String, Any?> {
    return mapOf(
        "id" to id,
        "nombre" to nombre,
        "telefono" to telefono
    )
}

fun Map<String, Any?>.toCliente(): Cliente {
    return Cliente(
        id = (this["id"] as Long?)?.toInt() ?: 0,
        nombre = this["nombre"] as String?,
        telefono = this["telefono"] as String?
    )
}
