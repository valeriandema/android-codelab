package com.sap.codelab.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val Primary = Color(0xFF9CCC65)
private val PrimaryDark = Color(0xFF6B9B37)
private val Accent = Color(0xFFFF7043)

private val CodelabColors = lightColorScheme(
    primary = Primary,
    onPrimary = Color.White,
    primaryContainer = PrimaryDark,
    onPrimaryContainer = Color.White,
    secondary = Accent,
    onSecondary = Color.White,
    tertiary = Accent,
)

@Composable
internal fun CodelabTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = CodelabColors,
        content = content,
    )
}
