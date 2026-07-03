package com.agrima.campuscart.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = IndigoDarkPrimary,
    onPrimary = IndigoDarkOnPrimary,
    primaryContainer = IndigoDarkPrimaryContainer,
    onPrimaryContainer = IndigoDarkOnPrimaryContainer,
    secondary = IndigoDarkSecondary,
    onSecondary = IndigoDarkOnSecondary,
    secondaryContainer = IndigoDarkSecondaryContainer,
    onSecondaryContainer = IndigoDarkOnSecondaryContainer,
    tertiary = IndigoDarkTertiary,
    onTertiary = IndigoDarkOnTertiary,
    tertiaryContainer = IndigoDarkTertiaryContainer,
    onTertiaryContainer = IndigoDarkOnTertiaryContainer,
    background = IndigoDarkBackground,
    onBackground = IndigoDarkOnBackground,
    surface = IndigoDarkSurface,
    onSurface = IndigoDarkOnSurface,
    surfaceVariant = IndigoDarkSurfaceVariant,
    onSurfaceVariant = IndigoDarkOnSurfaceVariant,
    outline = IndigoDarkOutline
)

private val LightColorScheme = lightColorScheme(
    primary = IndigoLightPrimary,
    onPrimary = IndigoLightOnPrimary,
    primaryContainer = IndigoLightPrimaryContainer,
    onPrimaryContainer = IndigoLightOnPrimaryContainer,
    secondary = IndigoLightSecondary,
    onSecondary = IndigoLightOnSecondary,
    secondaryContainer = IndigoLightSecondaryContainer,
    onSecondaryContainer = IndigoLightOnSecondaryContainer,
    tertiary = IndigoLightTertiary,
    onTertiary = IndigoLightOnTertiary,
    tertiaryContainer = IndigoLightTertiaryContainer,
    onTertiaryContainer = IndigoLightOnTertiaryContainer,
    background = IndigoLightBackground,
    onBackground = IndigoLightOnBackground,
    surface = IndigoLightSurface,
    onSurface = IndigoLightOnSurface,
    surfaceVariant = IndigoLightSurfaceVariant,
    onSurfaceVariant = IndigoLightOnSurfaceVariant,
    outline = IndigoLightOutline
)

@Composable
fun CampusCartTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is disabled by default to preserve custom branding, but can be enabled if desired.
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

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}