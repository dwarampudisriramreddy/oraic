package com.ram.orai.oraic

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.SwingPanel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.batik.swing.JSVGCanvas
import java.awt.BorderLayout
import java.io.File
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import javax.swing.JPanel

@Composable
actual fun OdontogramWebView(
    modifier: Modifier,
    state: DentalState?
) {
    var svgContent by remember { mutableStateOf<String?>(null) }
    var modifiedSvg by remember { mutableStateOf<String?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var tempFile by remember { mutableStateOf<File?>(null) }
    
    // Load SVG content from classpath resources
    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            try {
                // Try multiple resource paths (order matters - most likely first)
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
                
                // Debug: Try to list available resources (for troubleshooting)
                try {
                    val resourcesUrl = classLoader?.getResources("drawable")
                    if (resourcesUrl != null) {
                        println("DEBUG OdontogramWebView: Found resources directory 'drawable'")
                    }
                } catch (e: Exception) {
                    // Ignore - just debugging
                }
                
                // Try classpath resources first
                for (path in resourcePaths) {
                    try {
                        resourceStream = classLoader?.getResourceAsStream(path)
                        if (resourceStream != null) {
                            println("DEBUG OdontogramWebView: Found SVG resource at: $path")
                            svgContent = resourceStream.use { stream ->
                                String(stream.readBytes(), StandardCharsets.UTF_8).trim()
                            }
                            break
                        } else {
                            println("DEBUG OdontogramWebView: Resource not found at: $path")
                        }
                    } catch (e: Exception) {
                        println("DEBUG OdontogramWebView: Exception trying path $path: ${e.message}")
                        // Continue trying other paths
                    }
                }
                
                if (svgContent == null) {
                    // Try to find the resource URL to see what's available
                    try {
                        val resourceUrl = classLoader?.getResource("drawable/oral.svg")
                        println("DEBUG OdontogramWebView: Resource URL for drawable/oral.svg: $resourceUrl")
                    } catch (e: Exception) {
                        println("DEBUG OdontogramWebView: Could not get resource URL: ${e.message}")
                    }
                    
                    errorMessage = "SVG file not found. Please ensure oral.svg exists in composeResources/drawable/"
                    println("ERROR OdontogramWebView: $errorMessage")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                errorMessage = "Error loading SVG: ${e.message}"
                svgContent = null
                println("ERROR OdontogramWebView: Error loading SVG: ${e.message}")
            }
        }
    }
    
    // Modify SVG when state changes and create temp file
    LaunchedEffect(state, svgContent) {
        if (svgContent != null) {
            withContext(Dispatchers.IO) {
                modifiedSvg = if (state != null) {
                    try {
                        println("DEBUG OdontogramWebView: Modifying SVG with state")
                        val modified = modifySvgXml(svgContent!!, state)
                        println("DEBUG OdontogramWebView: SVG modification complete, length: ${modified.length}")
                        modified
                    } catch (e: Exception) {
                        e.printStackTrace()
                        errorMessage = "Error modifying SVG: ${e.message}"
                        svgContent
                    }
                } else {
                    svgContent
                }
                
                // Create temporary file for Batik to load
                if (modifiedSvg != null) {
                    try {
                        // Clean up old temp file
                        tempFile?.delete()
                        
                        // Create new temp file with unique name to force reload
                        val temp = File.createTempFile("oral_${System.currentTimeMillis()}_", ".svg")
                        temp.writeText(modifiedSvg!!, StandardCharsets.UTF_8)
                        temp.deleteOnExit()
                        val oldTemp = tempFile
                        tempFile = temp
                        println("DEBUG OdontogramWebView: Created temp file: ${temp.absolutePath}")
                        // Delete old temp file after a delay to ensure new one is loaded
                        oldTemp?.delete()
                    } catch (e: Exception) {
                        e.printStackTrace()
                        errorMessage = "Error creating temp file: ${e.message}"
                    }
                }
            }
        }
    }
    
    Box(modifier = modifier.fillMaxSize()) {
        when {
            errorMessage != null -> {
                Text(
                    text = errorMessage!!,
                    modifier = Modifier.fillMaxSize()
                )
            }
            svgContent == null -> {
                Text("Loading SVG...", modifier = Modifier.fillMaxSize())
            }
            modifiedSvg == null -> {
                Text("Processing SVG...", modifier = Modifier.fillMaxSize())
            }
            else -> {
                SwingPanel(
                    factory = {
                        try {
                            val canvas = JSVGCanvas().apply {
                                isEnabled = true
                            }
                            
                            // Load SVG from temp file using URI
                            if (tempFile != null && tempFile!!.exists()) {
                                try {
                                    val uri = tempFile!!.toURI().toString()
                                    println("DEBUG OdontogramWebView: Factory - Loading SVG from: $uri")
                                    canvas.uri = uri
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                    errorMessage = "Error loading SVG: ${e.message}"
                                }
                            }
                            
                            JPanel(BorderLayout()).apply {
                                add(canvas, BorderLayout.CENTER)
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                            errorMessage = "Error initializing SVG canvas: ${e.message}"
                            JPanel(BorderLayout()).apply {
                                add(javax.swing.JLabel("Error: ${e.message}"), BorderLayout.CENTER)
                            }
                        }
                    },
                    update = { panel ->
                        if (tempFile != null && tempFile!!.exists() && panel.componentCount > 0) {
                            val canvas = panel.getComponent(0) as? JSVGCanvas
                            if (canvas != null) {
                                try {
                                    val uri = tempFile!!.toURI().toString()
                                    val currentUri = canvas.uri
                                    if (currentUri != uri) {
                                        println("DEBUG OdontogramWebView: Update - URI changed from '$currentUri' to '$uri'")
                                        canvas.uri = uri
                                    } else {
                                        println("DEBUG OdontogramWebView: Update - URI unchanged, forcing repaint")
                                        canvas.repaint()
                                    }
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                    println("DEBUG OdontogramWebView: Error updating canvas: ${e.message}")
                                }
                            }
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}
