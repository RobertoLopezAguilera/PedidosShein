package com.example.pedidosshein.data.dao

import androidx.room.*
import com.example.pedidosshein.data.entities.Producto

@Dao
interface ProductoDao {

    @Query("SELECT * FROM Producto_table")
    fun getAllProductos(): List<Producto>

    @Query("SELECT * FROM Producto_table WHERE producto_id = :id")
    fun getProductoById(id: Int): Producto?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertProducto(vararg producto: Producto)

    @Query("DELETE FROM producto_table WHERE cliente_id = :clienteId")
    fun deleteProductoByClienteId(clienteId: Int)

    @Query("DELETE FROM producto_table WHERE producto_id = :productoId")
    fun deleteProductoById(productoId: Int): Int

    @Query("SELECT * FROM Producto_table WHERE cliente_id = :clienteId")
    fun getProductosByClienteId(clienteId: Int): List<Producto>

    @Update
    fun updateProducto(producto: Producto)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(productos: List<Producto>)
}
