package com.ram.orai.oraic

import android.content.Context
import androidx.compose.ui.platform.LocalContext
import java.io.BufferedReader
import java.io.InputStreamReader

actual fun getSvgContent(context: Any?): String {
    val ctx = context as? Context ?: run {
        android.util.Log.e("ReportSharingHelper", "Context is null")
        return ""
    }
    
    // Try loading from assets/oral.svg first (most common location)
    return try {
        android.util.Log.d("ReportSharingHelper", "Attempting to load SVG from assets/oral.svg")
        val inputStream = ctx.assets.open("oral.svg")
        val content = BufferedReader(InputStreamReader(inputStream, "UTF-8")).use { reader ->
            reader.readText()
        }
        android.util.Log.d("ReportSharingHelper", "Successfully loaded SVG from assets/oral.svg")
        android.util.Log.d("ReportSharingHelper", "SVG length: ${content.length}")
        android.util.Log.d("ReportSharingHelper", "SVG starts with: ${content.take(100)}")
        android.util.Log.d("ReportSharingHelper", "SVG contains <svg tag: ${content.contains("<svg", ignoreCase = true)}")
        content
    } catch (e: java.io.FileNotFoundException) {
        android.util.Log.e("ReportSharingHelper", "SVG file not found at assets/oral.svg: ${e.message}")
        try {
            android.util.Log.d("ReportSharingHelper", "Attempting to load SVG from assets/drawable/oral.svg")
            val inputStream = ctx.assets.open("drawable/oral.svg")
            val content = BufferedReader(InputStreamReader(inputStream, "UTF-8")).use { reader ->
                reader.readText()
            }
            android.util.Log.d("ReportSharingHelper", "Successfully loaded SVG from assets/drawable/oral.svg, length: ${content.length}")
            content
        } catch (e2: Exception) {
            android.util.Log.e("ReportSharingHelper", "Failed to load from assets/drawable/oral.svg: ${e2.message}")
            try {
                // Try loading from compose resources
                android.util.Log.d("ReportSharingHelper", "Attempting to load SVG from compose resources")
                val resources = ctx.resources
                val resourceId = resources.getIdentifier("oral", "drawable", ctx.packageName)
                if (resourceId != 0) {
                    val inputStream = resources.openRawResource(resourceId)
                    val content = BufferedReader(InputStreamReader(inputStream, "UTF-8")).use { reader ->
                        reader.readText()
                    }
                    android.util.Log.d("ReportSharingHelper", "Successfully loaded SVG from compose resources, length: ${content.length}")
                    content
                } else {
                    android.util.Log.e("ReportSharingHelper", "Resource 'oral' not found in drawable")
                    android.util.Log.e("ReportSharingHelper", "Please ensure oral.svg exists in src/main/assets/oral.svg")
                    ""
                }
            } catch (e3: Exception) {
                android.util.Log.e("ReportSharingHelper", "All attempts to load SVG failed", e3)
                android.util.Log.e("ReportSharingHelper", "Error 1: ${e.message}")
                android.util.Log.e("ReportSharingHelper", "Error 2: ${e2.message}")
                android.util.Log.e("ReportSharingHelper", "Error 3: ${e3.message}")
                e.printStackTrace()
                e2.printStackTrace()
                e3.printStackTrace()
                ""
            }
        }
    } catch (e: Exception) {
        android.util.Log.e("ReportSharingHelper", "Unexpected error loading SVG: ${e.message}", e)
        e.printStackTrace()
        ""
    }
}

