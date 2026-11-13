package com.simradio.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColors = darkColorScheme(
    primary = Amber,
    onPrimary = InkSunken,
    primaryContainer = AmberContainerDark,
    onPrimaryContainer = AmberGlow,
    secondary = AmberGlow,
    onSecondary = Ink,
    secondaryContainer = NeutralDark,
    onSecondaryContainer = OnInkHigh,
    tertiary = SignalGreen,
    onTertiary = InkSunken,
    background = Ink,
    onBackground = OnInkHigh,
    surface = Ink,
    onSurface = OnInkHigh,
    surfaceVariant = InkRaised,
    onSurfaceVariant = OnInkMedium,
    surfaceContainerLowest = InkSunken,
    surfaceContainerLow = Ink,
    surfaceContainer = InkRaised,
    surfaceContainerHigh = NeutralDark,
    surfaceContainerHighest = InkSoft,
    outline = OutlineDark,
    outlineVariant = OutlineVariantDark,
    error = SignalRed,
    onError = InkSunken,
    inverseSurface = Paper,
    inverseOnSurface = OnPaperHigh,
    inversePrimary = AmberDeep,
)

private val LightColors = lightColorScheme(
    primary = AmberDeep,
    onPrimary = PaperLowest,
    primaryContainer = AmberContainerLight,
    onPrimaryContainer = AmberOnContainerLight,
    secondary = InkSoft,
    onSecondary = PaperLowest,
    secondaryContainer = NeutralLight,
    onSecondaryContainer = OnPaperHigh,
    tertiary = SignalGreenDeep,
    onTertiary = PaperLowest,
    background = Paper,
    onBackground = OnPaperHigh,
    surface = Paper,
    onSurface = OnPaperHigh,
    surfaceVariant = PaperRaised,
    onSurfaceVariant = OnPaperMedium,
    surfaceContainerLowest = PaperLowest,
    surfaceContainerLow = Paper,
    surfaceContainer = PaperRaised,
    surfaceContainerHigh = PaperSunken,
    surfaceContainerHighest = PaperDeepest,
    outline = OutlineLight,
    outlineVariant = OutlineVariantLight,
    error = SignalRedDeep,
    onError = PaperLowest,
    inverseSurface = Ink,
    inverseOnSurface = OnInkHigh,
    inversePrimary = Amber,
)

@Composable
fun SimRadioTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = SimRadioTypography,
        shapes = SimRadioShapes,
        content = content,
    )
}