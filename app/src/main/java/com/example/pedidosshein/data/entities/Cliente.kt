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
