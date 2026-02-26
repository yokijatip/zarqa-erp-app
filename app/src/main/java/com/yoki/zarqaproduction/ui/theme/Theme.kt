package com.yoki.zarqaproduction.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary              = Blue800,
    onPrimary            = Color.White,
    primaryContainer     = Blue100,
    onPrimaryContainer   = Blue700,

    secondary            = Teal700,
    onSecondary          = Color.White,
    secondaryContainer   = Teal100,
    onSecondaryContainer = Teal700,

    background           = Slate50,
    onBackground         = Slate800,

    surface              = Color.White,
    onSurface            = Slate800,
    surfaceVariant       = Slate100,
    onSurfaceVariant     = Slate500,

    outline              = Slate200,
    outlineVariant       = Slate200,

    error                = ErrorRed,
    onError              = Color.White,
    errorContainer       = ErrorRedLight,
    onErrorContainer     = ErrorRed,
)

private val DarkColorScheme = darkColorScheme(
    primary              = Blue300,
    onPrimary            = Blue950,
    primaryContainer     = Blue950,
    onPrimaryContainer   = Blue300,

    secondary            = Teal300,
    onSecondary          = Slate950,

    background           = Slate950,
    onBackground         = Slate300,

    surface              = Slate900,
    onSurface            = Slate300,
    surfaceVariant       = Slate800,
    onSurfaceVariant     = Slate300,

    error                = ErrorRed,
    onError              = Color.White,
)

@Composable
fun ZarqaProductionTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography  = Typography,
        content     = content
    )
}
