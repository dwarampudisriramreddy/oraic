package com.ram.orai.oraic

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter
import org.jetbrains.compose.resources.painterResource
import oraic.composeapp.generated.resources.Res
import oraic.composeapp.generated.resources.oral

@Composable
actual fun rememberOdontogramPainter(): Painter? {
    return painterResource(Res.drawable.oral)
}

