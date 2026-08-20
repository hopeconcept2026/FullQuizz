package com.example.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = CleanMinPrimary,
    onPrimary = CleanMinOnPrimary,
    primaryContainer = CleanMinPrimaryContainer,
    onPrimaryContainer = CleanMinOnPrimaryContainer,
    secondary = CleanMinPrimary,
    onSecondary = CleanMinOnPrimary,
    secondaryContainer = CleanMinSecondaryContainer,
    onSecondaryContainer = CleanMinOnSecondaryContainer,
    tertiary = CleanMinGreen,
    onTertiary = CleanMinOnPrimary,
    background = CleanMinBackground,
    onBackground = CleanMinTextPrimary,
    surface = CleanMinSurface,
    onSurface = CleanMinTextPrimary,
    surfaceVariant = CleanMinSurfaceVariant,
    onSurfaceVariant = CleanMinTextSecondary,
    outline = CleanMinOutline,
    outlineVariant = CleanMinOutlineVariant,
    error = CleanMinRed,
    errorContainer = CleanMinRedBg,
    onErrorContainer = CleanMinRed
)

private val DarkColorScheme = darkColorScheme(
    primary = CleanMinDarkPrimary,
    onPrimary = CleanMinDarkSurface,
    primaryContainer = CleanMinDarkPrimaryContainer,
    onPrimaryContainer = CleanMinDarkOnPrimaryContainer,
    secondary = CleanMinDarkPrimary,
    onSecondary = CleanMinDarkSurface,
    secondaryContainer = CleanMinDarkPrimaryContainer,
    onSecondaryContainer = CleanMinDarkOnPrimaryContainer,
    tertiary = CleanMinGreen,
    onTertiary = CleanMinDarkSurface,
    background = CleanMinDarkBackground,
    onBackground = CleanMinDarkTextPrimary,
    surface = CleanMinDarkSurface,
    onSurface = CleanMinDarkTextPrimary,
    surfaceVariant = CleanMinDarkSurfaceVariant,
    onSurfaceVariant = CleanMinDarkTextSecondary,
    outline = CleanMinDarkOutline,
    error = CleanMinRed
)

@Composable
fun FullQuizzTheme(
    darkTheme: Boolean = false, // Clean Minimalism default is the light refined palette
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            window?.let {
                it.statusBarColor = colorScheme.background.toArgb()
                it.navigationBarColor = CleanMinSurfaceVariant.toArgb()
                WindowCompat.getInsetsController(it, view).apply {
                    isAppearanceLightStatusBars = !darkTheme
                    isAppearanceLightNavigationBars = !darkTheme
                }
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
