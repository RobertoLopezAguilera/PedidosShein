package com.example.pedidosshein.data.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.pedidosshein.data.dao.AbonoDao
import com.example.pedidosshein.data.dao.ClienteDao
import com.example.pedidosshein.data.dao.ProductoDao
import com.example.pedidosshein.data.entities.Abono
import com.example.pedidosshein.data.entities.Cliente
import com.example.pedidosshein.data.entities.Producto

@Database(
    entities = [Cliente::class, Abono::class, Producto::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun clienteDao(): ClienteDao
    abstract fun abonoDao(): AbonoDao
    abstract fun productoDao(): ProductoDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: android.content.Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "PedidosSheinDB"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
