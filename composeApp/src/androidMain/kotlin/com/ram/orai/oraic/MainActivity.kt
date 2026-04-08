package com.ram.orai.oraic

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        setContent {
            // Main app content
            val context = LocalContext.current
            val driverFactory = remember { DatabaseDriverFactory(context) }
            val pdfGenerator = remember { createPdfReportGenerator(context) }
            val shareHelper = remember { createShareHelper(context) }
            App(
                driverFactory = driverFactory,
                pdfGenerator = pdfGenerator,
                shareHelper = shareHelper,
                context = context
            )
        }
    }
}


