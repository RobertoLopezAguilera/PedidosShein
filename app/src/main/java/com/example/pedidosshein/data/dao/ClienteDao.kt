package com.example.pedidosshein.data.dao

import androidx.room.*
import com.example.pedidosshein.data.entities.Cliente
import com.example.pedidosshein.data.entities.Producto

@Dao
interface ClienteDao {

    @Query("SELECT * FROM Cliente_table")
    fun getAllClientes(): List<Cliente>

    @Query("SELECT * FROM Cliente_table WHERE cliente_id = :id")
    fun getClienteById(id: Int): Cliente

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertCliente(vararg cliente: Cliente)

    @Delete
    fun deleteCliente(vararg cliente: Cliente)

    @Query("DELETE FROM Cliente_table WHERE cliente_id = :id")
    fun deleteClienteById(id: Int): Int

    @Update
    fun updateCliente(cliente: Cliente)

    @Query("SELECT * FROM Cliente_table WHERE LOWER(nombre) LIKE '%' || :query || '%'")
    fun buscarClientes(query: String): List<Cliente>

    @Delete
    fun deleteClientes(clientes: List<Cliente>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(clientes: List<Cliente>)

}
