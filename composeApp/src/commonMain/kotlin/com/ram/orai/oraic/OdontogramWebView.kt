package com.ram.orai.oraic

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier

@Composable
expect fun OdontogramWebView(
    modifier: Modifier = Modifier,
    state: DentalState? = null
)

