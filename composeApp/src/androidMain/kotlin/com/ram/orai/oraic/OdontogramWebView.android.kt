package com.ram.orai.oraic

import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import android.os.Handler
import android.os.Looper
import java.io.BufferedReader
import java.io.InputStreamReader

@Composable
actual fun OdontogramWebView(modifier: Modifier, state: DentalState?) {
    val context = LocalContext.current
    val webViewRef = remember { mutableStateOf<WebView?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var svgContent by remember { mutableStateOf<String?>(null) }
    var modifiedSvg by remember { mutableStateOf<String?>(null) }
    var isPageLoaded by remember { mutableStateOf(false) }
    
    // Load SVG content once
    LaunchedEffect(Unit) {
        try {
            val inputStream = context.assets.open("oral.svg")
            svgContent = BufferedReader(InputStreamReader(inputStream, "UTF-8")).use { reader ->
                reader.readText()
            }
        } catch (e: Exception) {
            try {
                val inputStream = context.assets.open("drawable/oral.svg")
                svgContent = BufferedReader(InputStreamReader(inputStream, "UTF-8")).use { reader ->
                    reader.readText()
                }
            } catch (e2: Exception) {
                errorMessage = "Failed to load SVG: ${e.message ?: e2.message}"
            }
        }
    }
    
    // Modify SVG when state changes (like JVM implementation)
    LaunchedEffect(state, svgContent) {
        if (svgContent != null) {
            modifiedSvg = if (state != null) {
                try {
                    modifySvgXml(svgContent!!, state)
                } catch (e: Exception) {
                    e.printStackTrace()
                    errorMessage = "Error modifying SVG: ${e.message}"
                    svgContent
                }
            } else {
                svgContent
            }
        }
    }
    
    // Helper function to create HTML content
    fun createHtmlContent(svg: String): String {
        return """
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
                        margin: 0;
                        padding: 0;
                        width: 100%;
                        height: 100%;
                    }
                    body {
                        display: block !important;
                        visibility: visible !important;
                        opacity: 1 !important;
                        background: #f0f0f0 !important;
                        width: 100% !important;
                        height: 100vh !important;
                        padding: 10px;
                        margin: 0;
                        box-sizing: border-box;
                        overflow: hidden;
                        -webkit-user-select: none;
                        user-select: none;
                    }
                    #svgContainer {
                        width: 100% !important;
                        height: 500px !important;
                        min-height: 500px !important;
                        overflow: hidden;
                        display: flex !important;
                        align-items: center;
                        justify-content: center;
                    }
                    #svgRoot, svg {
                        display: block !important;
                        width: 100% !important;
                        height: auto !important;
                        min-height: 400px !important;
                        max-width: 100% !important;
                        max-height: 100% !important;
                        visibility: visible !important;
                        opacity: 1 !important;
                        margin: 0 auto;
                        position: relative;
                        background: white;
                    }
                    #svg-container {
                        width: 100%;
                        height: 100%;
                        overflow: hidden;
                    }
                </style>
            </head>
            <body>
                <div id="svgContainer" style="width:100%; height:500px;">
                    <div id="svg-container">
                        $svg
                    </div>
                </div>
                <script>
                    // Force SVG repaint after load (Android WebView fix)
                    (function() {
                        setTimeout(function() {
                            const svg = document.querySelector('svg');
                            if (svg) {
                                svg.style.display = 'none';
                                svg.getBoundingClientRect(); // force reflow
                                svg.style.display = 'block';
                                if (!svg.id) {
                                    svg.id = 'svgRoot';
                                }
                            }
                        }, 50);
                    })();
                </script>
            </body>
            </html>
        """.trimIndent()
    }
    
    AndroidView(
        factory = { ctx ->
            WebView(ctx).apply {
                webViewRef.value = this
                setBackgroundColor(0xFFFFFFFF.toInt())
                // Disable hardware acceleration for SVG rendering (Android WebView fix)
                setLayerType(View.LAYER_TYPE_SOFTWARE, null)
                webViewClient = object : WebViewClient() {
                    override fun onPageFinished(view: WebView?, url: String?) {
                        super.onPageFinished(view, url)
                        isPageLoaded = true
                    }
                    
                    override fun onReceivedError(view: WebView?, errorCode: Int, description: String?, failingUrl: String?) {
                        super.onReceivedError(view, errorCode, description, failingUrl)
                        errorMessage = "WebView Error ($errorCode): $description"
                    }
                }
                settings.javaScriptEnabled = true
                settings.loadWithOverviewMode = true
                settings.useWideViewPort = true
                settings.builtInZoomControls = false
                settings.displayZoomControls = false
                settings.domStorageEnabled = true
                settings.setSupportZoom(false)
                settings.setSupportMultipleWindows(false)
                
                // Load initial SVG
                if (modifiedSvg != null) {
                    loadDataWithBaseURL("file:///android_asset/", createHtmlContent(modifiedSvg!!), "text/html", "UTF-8", null)
                } else if (errorMessage == null) {
                    val errorHtml = """
                        <!DOCTYPE html>
                        <html>
                        <head>
                            <meta name="viewport" content="width=device-width, initial-scale=1.0">
                            <style>
                                body {
                                    font-family: Arial, sans-serif;
                                    padding: 20px;
                                    text-align: center;
                                    background: #ffebee;
                                }
                                h3 { color: #c62828; }
                            </style>
                        </head>
                        <body>
                            <h3>Loading SVG...</h3>
                        </body>
                        </html>
                    """.trimIndent()
                    loadDataWithBaseURL("file:///android_asset/", errorHtml, "text/html", "UTF-8", null)
                }
            }
        },
        update = { webView ->
            // Reload HTML with modified SVG when state changes (like JVM)
            if (modifiedSvg != null && isPageLoaded) {
                Handler(Looper.getMainLooper()).postDelayed({
                    try {
                        val htmlContent = createHtmlContent(modifiedSvg!!)
                        webView.loadDataWithBaseURL("file:///android_asset/", htmlContent, "text/html", "UTF-8", null)
                    } catch (e: Exception) {
                        errorMessage = "Error updating SVG: ${e.message}"
                        e.printStackTrace()
                    }
                }, 50)
            }
        },
        modifier = modifier.fillMaxSize()
    )
    
    // Also update when modifiedSvg changes (reload the page)
    LaunchedEffect(modifiedSvg) {
        webViewRef.value?.let { webView ->
            if (modifiedSvg != null && isPageLoaded) {
                Handler(Looper.getMainLooper()).postDelayed({
                    try {
                        val htmlContent = createHtmlContent(modifiedSvg!!)
                        webView.loadDataWithBaseURL("file:///android_asset/", htmlContent, "text/html", "UTF-8", null)
                    } catch (e: Exception) {
                        errorMessage = "Error reloading SVG: ${e.message}"
                        e.printStackTrace()
                    }
                }, 50)
            }
        }
    }
    
    // Display error overlay if there's an error
    errorMessage?.let { error ->
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.TopCenter
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "Error Loading Odontogram",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = error,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        }
    }
}
