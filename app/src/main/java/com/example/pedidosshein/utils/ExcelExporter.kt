package com.example.pedidosshein.utils

import android.content.Context
import android.os.Environment
import com.example.pedidosshein.data.database.AppDatabase
import org.apache.poi.ss.usermodel.WorkbookFactory
import java.io.File
import java.io.FileOutputStream

object ExcelExporter {

    fun exportToExcel(context: Context) {
        val db = AppDatabase.getDatabase(context)

        val workbook = WorkbookFactory.create(false)
        val sheet = workbook.createSheet("Clientes")

        val clientes = db.clienteDao().getAllClientes()

        // Agrega encabezados
        val headerRow = sheet.createRow(0)
        headerRow.createCell(0).setCellValue("ID")
        headerRow.createCell(1).setCellValue("Nombre")
        headerRow.createCell(2).setCellValue("Teléfono")

        // Agrega datos
        clientes.forEachIndexed { index, cliente ->
            val row = sheet.createRow(index + 1)
            row.createCell(0).setCellValue(cliente.id.toString())
            row.createCell(1).setCellValue(cliente.nombre)
            row.createCell(2).setCellValue(cliente.telefono)
        }

        val file = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "Clientes.xlsx")
        FileOutputStream(file).use {
            workbook.write(it)
        }

        workbook.close()
    }
}
