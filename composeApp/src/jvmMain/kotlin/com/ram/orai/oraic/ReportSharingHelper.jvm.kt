package com.ram.orai.oraic

import java.nio.charset.StandardCharsets

actual fun getSvgContent(context: Any?): String {
    return try {
        // Load SVG from classpath resources
        val resourcePaths = listOf(
            "drawable/oral.svg",
            "oral.svg",
            "composeResources/drawable/oral.svg",
            "/drawable/oral.svg",
            "/oral.svg",
            "/composeResources/drawable/oral.svg"
        )
        
        var resourceStream: java.io.InputStream? = null
        val classLoader = object {}.javaClass.classLoader
        
        for (path in resourcePaths) {
            try {
                resourceStream = classLoader?.getResourceAsStream(path)
                if (resourceStream != null) {
                    println("DEBUG ReportSharingHelper: Found SVG resource at: $path")
                    return resourceStream.use { stream ->
                        String(stream.readBytes(), StandardCharsets.UTF_8).trim()
                    }
                } else {
                    println("DEBUG ReportSharingHelper: Resource not found at: $path")
                }
            } catch (e: Exception) {
                println("DEBUG ReportSharingHelper: Exception trying path $path: ${e.message}")
                // Continue trying other paths
            }
        }
        
        println("ERROR ReportSharingHelper: SVG file not found for PDF generation. Please ensure oral.svg exists in composeResources/drawable/")
        "" // Return empty string if not found
    } catch (e: Exception) {
        e.printStackTrace()
        println("ERROR ReportSharingHelper: Error loading SVG: ${e.message}")
        "" // Return empty string if error
    }
}

