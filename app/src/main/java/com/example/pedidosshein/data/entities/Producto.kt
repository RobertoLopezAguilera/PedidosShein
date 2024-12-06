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
    @ColumnInfo(name = "cliente_id") val clienteId: Int // Agregar campo clienteId para la relación
)

