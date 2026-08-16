package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val ReelShortColorScheme = darkColorScheme(
    primary = ReelRedPrimary,
    onPrimary = ReelTextPrimary,
    primaryContainer = ReelRedDark,
    onPrimaryContainer = ReelTextPrimary,
    secondary = ReelGoldPrimary,
    onSecondary = ReelBackgroundDark,
    secondaryContainer = ReelGoldDark,
    onSecondaryContainer = ReelTextPrimary,
    tertiary = ReelRedLight,
    background = ReelBackgroundDark,
    onBackground = ReelTextPrimary,
    surface = ReelSurfaceDark,
    onSurface = ReelTextPrimary,
    surfaceVariant = ReelSurfaceVariantDark,
    onSurfaceVariant = ReelTextSecondary
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // ReelShort is a cinematic dark-first streaming experience
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = ReelBackgroundDark.toArgb()
            window.navigationBarColor = ReelBackgroundDark.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = false
        }
    }

    MaterialTheme(
        colorScheme = ReelShortColorScheme,
        typography = Typography,
        content = content
    )
}
