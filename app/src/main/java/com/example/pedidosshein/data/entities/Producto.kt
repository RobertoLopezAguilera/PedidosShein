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
    @ColumnInfo(name = "cliente_id") val clienteId: Int, // Agregar campo clienteId para la relación
    @ColumnInfo(name = "foto_uri") val fotoUri: String? = null // Ruta o URI de la imagen
)

// Funciones de extensión para Producto
fun Producto.toMap(): Map<String, Any?> {
    return mapOf(
        "id" to id,
        "nombre" to nombre,
        "precio" to precio,
        "clienteId" to clienteId
    )
}

fun Map<String, Any?>.toProducto(): Producto {
    return Producto(
        id = (this["id"] as Long?)?.toInt() ?: 0,
        nombre = this["nombre"] as? String,
        precio = this["precio"] as? Double ?: 0.0,
        clienteId = (this["clienteId"] as Long?)?.toInt() ?: 0
    )
}

