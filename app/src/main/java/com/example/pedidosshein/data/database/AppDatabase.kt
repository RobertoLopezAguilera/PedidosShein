package com.example.pedidosshein.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.pedidosshein.data.dao.AbonoDao
import com.example.pedidosshein.data.dao.ClienteDao
import com.example.pedidosshein.data.dao.ProductoDao
import com.example.pedidosshein.data.entities.Abono
import com.example.pedidosshein.data.entities.Cliente
import com.example.pedidosshein.data.entities.Producto

@Database(
    entities = [Cliente::class, Abono::class, Producto::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun clienteDao(): ClienteDao
    abstract fun abonoDao(): AbonoDao
    abstract fun productoDao(): ProductoDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        // Migración de versión 1 a 2
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Añade la nueva columna para la foto
                database.execSQL("ALTER TABLE Producto_table ADD COLUMN foto_uri TEXT DEFAULT NULL")
            }
        }

        // Método singleton para obtener la instancia de la base de datos
        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "PedidosSheinDB"
                )
                    .addMigrations(MIGRATION_1_2) // Añade la migración aquí
                    .build()
                INSTANCE = instance
                instance
            }
        }

        // Alias para getInstance
        fun getDatabase(context: Context): AppDatabase {
            return getInstance(context)
        }
    }
}