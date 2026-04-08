package com.ram.orai.oraic

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import platform.UIKit.UIView
import platform.WebKit.WKWebView
import platform.WebKit.WKWebViewConfiguration
import platform.Foundation.NSURL
import platform.Foundation.NSURLRequest
import org.jetbrains.compose.resources.readResourceBytes
import oraic.composeapp.generated.resources.Res
import oraic.composeapp.generated.resources.oral

@Composable
actual fun OdontogramWebView(modifier: Modifier, state: DentalState?) {
    androidx.compose.runtime.LaunchedEffect(state) {
        // Update visualization when state changes
        // Note: This would need to be implemented with a way to access the webView
    }
    
    androidx.compose.ui.interop.UIKitView(
        factory = {
            val config = WKWebViewConfiguration()
            val webView = WKWebView(frame = it.bounds, configuration = config)
            
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
                
                val htmlContent = """
                    <!DOCTYPE html>
                    <html>
                    <head>
                        <meta name="viewport" content="width=device-width, initial-scale=1.0, user-scalable=no">
                        <style>
                            * {
                                margin: 0;
                                padding: 0;
                                box-sizing: border-box;
                            }
                            html, body {
                                width: 100%;
                                height: 100%;
                                overflow: auto;
                                padding: 0;
                                margin: 0;
                            }
                            body {
                                display: flex;
                                justify-content: center;
                                align-items: center;
                                background: white;
                                width: 100%;
                                height: 100vh;
                                padding: 10px;
                                margin: 0;
                                box-sizing: border-box;
                                overflow: auto;
                            }
                            svg {
                                width: auto;
                                height: auto;
                                max-width: 90%;
                                max-height: 85vh;
                                display: block;
                                margin: 0 auto;
                                position: relative;
                            }
                        </style>
                    </head>
                    <body>
                        $svgContent
                    </body>
                    </html>
                """.trimIndent()
                
                webView.loadHTMLString(htmlContent, baseURL = null)
                
                // Inject visualization script after page loads
                if (state != null) {
                    webView.evaluateJavaScript(generateVisualizationScript(state)) { result, error ->
                        if (error != null) {
                            println("Error executing visualization script: $error")
                        }
                    }
                }
            } catch (e: Exception) {
                webView.loadHTMLString("<html><body><p>Error loading SVG: ${e.message}</p></body></html>", baseURL = null)
            }
            
            webView
        },
        update = { webView ->
            // Update visualization when state changes
            if (state != null) {
                webView.evaluateJavaScript(generateVisualizationScript(state)) { result, error ->
                    if (error != null) {
                        println("Error executing visualization script: $error")
                    }
                }
            }
        },
        modifier = modifier.fillMaxSize()
    )
}

