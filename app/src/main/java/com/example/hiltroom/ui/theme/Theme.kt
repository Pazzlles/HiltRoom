package com.example.hiltroom.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = Green80,
    secondary = GreenGrey80,
    tertiary = Mint80,
    background = GreenBackgroundDark,
    surface = GreenSurfaceDark,
    surfaceVariant = GreenSurfaceVariantDark,
    secondaryContainer = GreenSecondaryContainerDark,
    onSecondaryContainer = Green80,
    outline = GreenOutlineDark,
)

private val LightColorScheme = lightColorScheme(
    primary = Green40,
    secondary = GreenGrey40,
    tertiary = Mint40,
    background = GreenBackgroundLight,
    surface = GreenSurfaceLight,
    surfaceVariant = GreenSurfaceVariantLight,
    secondaryContainer = GreenSecondaryContainerLight,
    onSecondaryContainer = Green40,
    outline = GreenOutlineLight,
)

@Composable
fun HiltRoomTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> LightColorScheme
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
