package com.ledgerly.expense.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.ledgerly.expense.domain.repository.ThemeMode

private val LightColors = lightColorScheme(
    primary = Green40, onPrimary = NeutralF, primaryContainer = Green90, onPrimaryContainer = Green10,
    secondary = Teal40, secondaryContainer = Teal80,
    tertiary = Amber40, tertiaryContainer = Amber80,
    background = NeutralF, surface = NeutralF,
    error = ErrorRed,
)

private val DarkColors = darkColorScheme(
    primary = Green80, onPrimary = Green20, primaryContainer = Green20, onPrimaryContainer = Green90,
    secondary = Teal80, tertiary = Amber80,
    background = NeutralDark, surface = NeutralSurfaceDark,
    error = ErrorRedDark,
)

/**
 * App theme. Honors the user's [ThemeMode] preference and, on Android 12+, the
 * optional Material You dynamic color scheme.
 */
@Composable
fun LedgerlyTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit,
) {
    val dark = when (themeMode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
    }
    val context = LocalContext.current
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ->
            if (dark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        dark -> DarkColors
        else -> LightColors
    }
    MaterialTheme(
        colorScheme = colorScheme,
        typography = LedgerlyTypography,
        content = content,
    )
}
