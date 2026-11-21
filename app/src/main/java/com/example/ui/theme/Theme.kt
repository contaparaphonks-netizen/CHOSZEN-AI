package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

private val ChoszenDarkColorScheme = darkColorScheme(
    primary = VoltLime,
    onPrimary = OnVoltLime,
    primaryContainer = ColorTokens.LimeContainer,
    onPrimaryContainer = VoltLime,
    secondary = BroadcastCyan,
    onSecondary = OnBroadcastCyan,
    secondaryContainer = ColorTokens.CyanContainer,
    onSecondaryContainer = BroadcastCyan,
    tertiary = SolarGold,
    onTertiary = OnSolarGold,
    background = ObsidianBg,
    onBackground = TextPrimary,
    surface = StudioSurface,
    onSurface = TextPrimary,
    surfaceVariant = CardSurface,
    onSurfaceVariant = TextSecondary,
    surfaceContainerLow = StudioSurface,
    surfaceContainer = CardSurface,
    surfaceContainerHigh = ElevatedCardSurface,
    outline = HairlineBorder,
    outlineVariant = HairlineBorder,
    error = DangerRed,
    onError = ObsidianBg
)

private object ColorTokens {
    val LimeContainer = androidx.compose.ui.graphics.Color(0xFF243304)
    val CyanContainer = androidx.compose.ui.graphics.Color(0xFF042B35)
}

private val ChoszenShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun ChoszenEditAiTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = ChoszenDarkColorScheme,
        typography = ChoszenTypography,
        shapes = ChoszenShapes,
        content = content
    )
}
