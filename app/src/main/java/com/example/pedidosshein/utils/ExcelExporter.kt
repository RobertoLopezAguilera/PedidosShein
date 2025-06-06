package com.example.pedidosshein.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Environment
import androidx.core.app.NotificationCompat
import androidx.core.content.FileProvider
import com.example.pedidosshein.data.database.AppDatabase
import org.apache.poi.ss.usermodel.WorkbookFactory
import java.io.File
import java.io.FileOutputStream

object ExcelExporter {

    fun exportToExcel(context: Context) {
        val db = AppDatabase.getDatabase(context)

        val workbook = WorkbookFactory.create(false)

        // Hoja de Clientes
        val clientesSheet = workbook.createSheet("Clientes")
        val clientes = db.clienteDao().getAllClientes()
        val headerRowClientes = clientesSheet.createRow(0)
        headerRowClientes.createCell(0).setCellValue("ID")
        headerRowClientes.createCell(1).setCellValue("Nombre")
        headerRowClientes.createCell(2).setCellValue("Teléfono")
        clientes.forEachIndexed { index, cliente ->
            val row = clientesSheet.createRow(index + 1)
            row.createCell(0).setCellValue(cliente.id.toString())
            row.createCell(1).setCellValue(cliente.nombre)
            row.createCell(2).setCellValue(cliente.telefono)
        }

        // Hoja de Productos
        val productosSheet = workbook.createSheet("Productos")
        val productos = db.productoDao().getAllProductos()
        val headerRowProductos = productosSheet.createRow(0)
        headerRowProductos.createCell(0).setCellValue("ID Producto")
        headerRowProductos.createCell(1).setCellValue("Nombre Producto")
        headerRowProductos.createCell(2).setCellValue("Precio")
        headerRowProductos.createCell(3).setCellValue("Cliente ID")
        productos.forEachIndexed { index, producto ->
            val row = productosSheet.createRow(index + 1)
            row.createCell(0).setCellValue(producto.id.toString())
            row.createCell(1).setCellValue(producto.nombre)
            row.createCell(2).setCellValue(producto.precio.toString())
            row.createCell(3).setCellValue(producto.clienteId.toString())
        }

        // Hoja de Abonos
        val abonosSheet = workbook.createSheet("Abonos")
        val abonos = db.abonoDao().getAllAbonos()
        val headerRowAbonos = abonosSheet.createRow(0)
        headerRowAbonos.createCell(0).setCellValue("ID Abono")
        headerRowAbonos.createCell(1).setCellValue("Cantidad")
        headerRowAbonos.createCell(2).setCellValue("Fecha")
        headerRowAbonos.createCell(3).setCellValue("Cliente ID")
        abonos.forEachIndexed { index, abono ->
            val row = abonosSheet.createRow(index + 1)
            row.createCell(0).setCellValue(abono.id.toString())
            row.createCell(1).setCellValue(abono.monto.toString())
            row.createCell(2).setCellValue(abono.fecha.toString())
            row.createCell(3).setCellValue(abono.clienteId.toString())
        }

        // Guardar el archivo
        val file = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "Datos_Clientes.xlsx")
        FileOutputStream(file).use {
            workbook.write(it)
        }

        workbook.close()

        // Notificación de éxito
        showExportNotification(context, file)
    }

    private fun showExportNotification(context: Context, file: File) {
        val channelId = "export_channel"
        val channelName = "Exportación de Datos"
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Crear un canal de notificación para dispositivos con Android 8.0+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_DEFAULT)
            notificationManager.createNotificationChannel(channel)
        }

        // Crear un intent para abrir el archivo
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
        val openFileIntent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        // Verificar si existe una aplicación capaz de abrir el archivo
        val pendingIntent = if (openFileIntent.resolveActivity(context.packageManager) != null) {
            PendingIntent.getActivity(context, 0, openFileIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        } else {
            null
        }

        // Crear la notificación
        val notificationBuilder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.stat_sys_download_done)
            .setContentTitle("Exportación Completa")
            .setContentText("Los datos han sido exportados correctamente.")
            .setAutoCancel(true)

        // Agregar la acción para abrir el archivo si es posible
        if (pendingIntent != null) {
            notificationBuilder.setContentIntent(pendingIntent)
        }

        // Mostrar la notificación
        notificationManager.notify(1, notificationBuilder.build())
    }

}
