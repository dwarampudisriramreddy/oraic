package com.ram.orai.oraic

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.jetbrains.compose.resources.readResourceBytes
import oraic.composeapp.generated.resources.Res
import oraic.composeapp.generated.resources.oral
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Text

@Composable
actual fun OdontogramWebView(modifier: Modifier, state: DentalState?) {
    Box(modifier = modifier.fillMaxSize()) {
        try {
            val svgBytes = readResourceBytes(Res.drawable.oral)
            var svgContent = String(svgBytes)
            
            // Add viewBox and preserveAspectRatio to SVG if it doesn't have one (for proper scaling and centering)
            if (!svgContent.contains("viewBox=")) {
                val widthMatch = Regex("""width\s*=\s*["']([^"']+)["']""").find(svgContent)
                val heightMatch = Regex("""height\s*=\s*["']([^"']+)["']""").find(svgContent)
                if (widthMatch != null && heightMatch != null) {
                    val width = widthMatch.groupValues[1]
                    val height = heightMatch.groupValues[1]
                    val viewBox = "viewBox=\"0 0 $width $height\""
                    val preserveAspectRatio = "preserveAspectRatio=\"xMidYMid meet\""
                    svgContent = Regex("""<svg([^>]*)>""").replace(svgContent) { matchResult ->
                        val attrs = matchResult.groupValues[1]
                        if (!attrs.contains("viewBox=")) {
                            "<svg $viewBox $preserveAspectRatio$attrs>"
                        } else {
                            matchResult.value
                        }
                    }
                }
            } else if (!svgContent.contains("preserveAspectRatio=")) {
                // Add preserveAspectRatio if viewBox exists but preserveAspectRatio doesn't
                svgContent = Regex("""<svg([^>]*)>""").replace(svgContent) { matchResult ->
                    val attrs = matchResult.groupValues[1]
                    if (!attrs.contains("preserveAspectRatio=")) {
                        "<svg $attrs preserveAspectRatio=\"xMidYMid meet\">"
                    } else {
                        matchResult.value
                    }
                }
            }
            
            // For Wasm, use iframe to display SVG as HTML
            org.jetbrains.compose.web.dom.Iframe {
                attributes {
                    style {
                        property("width", "100%")
                        property("height", "100%")
                        property("border", "none")
                    }
                }
                // Set srcdoc to embed SVG as HTML
                val htmlContent = """
                    <!DOCTYPE html>
                    <html>
                    <head>
                        <style>
                            * { margin: 0; padding: 0; box-sizing: border-box; }
                            html, body { width: 100%; height: 100%; overflow: auto; padding: 0; margin: 0; }
                            body { display: flex; justify-content: center; align-items: center; background: white; width: 100%; height: 100vh; padding: 10px; box-sizing: border-box; overflow: auto; }
                            svg { width: auto; height: auto; max-width: 90%; max-height: 85vh; display: block; margin: 0 auto; position: relative; }
                        </style>
                    </head>
                    <body>
                        $svgContent
                    </body>
                    </html>
                """.trimIndent()
                attr("srcdoc", htmlContent)
            }
        } catch (e: Exception) {
            Div {
                Text("Error loading SVG: ${e.message}")
            }
        }
    }
}

