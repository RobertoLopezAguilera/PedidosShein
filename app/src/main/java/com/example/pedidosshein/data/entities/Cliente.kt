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

fun Cliente.toMap(): Map<String, Any> {
    return hashMapOf(
        "id" to id.toLong(), // Convertir a Long
        "nombre" to (nombre ?: ""),
        "telefono" to (telefono ?: "")
    )
}

fun Map<String, Any>.toCliente(): Cliente {
    return Cliente(
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
        telefono = (this["telefono"] as? String).takeIf { !it.isNullOrEmpty() }
    )
}