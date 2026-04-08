package com.ram.orai.oraic

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream

actual class ShareHelper(private val context: Context) {
    actual fun shareFile(file: File, mimeType: String, fileName: String) {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        
        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_STREAM, uri)
            type = mimeType
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        
        val chooserTitle = when {
            mimeType == "text/csv" -> "Share CSV Export"
            mimeType == "application/pdf" -> "Share Case History Report"
            else -> "Share File"
        }
        val chooserIntent = Intent.createChooser(shareIntent, chooserTitle)
        chooserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooserIntent)
    }
    
    actual fun sharePdfReport(patient: Patient, pdfBytes: ByteArray) {
        try {
            val fileName = generateReportFileName(patient)
            val file = File(context.getExternalFilesDir(null), fileName)
            FileOutputStream(file).use { it.write(pdfBytes) }
            
            shareFile(file, "application/pdf", fileName)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    actual fun shareCsvFile(csvContent: String, fileName: String) {
        try {
            val file = File(context.getExternalFilesDir(null), fileName)
            FileOutputStream(file).use { 
                it.write(csvContent.toByteArray(Charsets.UTF_8))
            }
            
            shareFile(file, "text/csv", fileName)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

actual fun createShareHelper(context: Any?): ShareHelper {
    return ShareHelper(context as Context)
}


