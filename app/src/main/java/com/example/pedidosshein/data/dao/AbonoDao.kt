package com.example.pedidosshein.data.dao

import androidx.room.*
import com.example.pedidosshein.data.entities.Abono
import com.example.pedidosshein.data.entities.Producto

@Dao
interface AbonoDao {

    @Query("SELECT * FROM Abono_table")
    fun getAllAbonos(): List<Abono>

    @Query("SELECT * FROM Abono_table WHERE abono_id = :id")
    fun getAbonoById(id: Int): Abono?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAbono(vararg abono: Abono)

    @Query("DELETE FROM abono_table WHERE cliente_id = :clienteId")
    fun deleteAbonoByClienteId(clienteId: Int)

    @Delete
    fun deleteAbono(vararg abono: Abono)

    @Query("DELETE FROM abono_table WHERE abono_id = :id")
    fun deleteAbonoById(id: Int): Int

    @Query("SELECT * FROM Abono_table WHERE cliente_id = :clienteId")
    fun getAbonosByClienteId(clienteId: Int): List<Abono>

    @Update
    fun updateAbono(abono: Abono)
}
