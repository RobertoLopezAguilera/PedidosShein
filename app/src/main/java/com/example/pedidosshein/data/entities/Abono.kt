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
