package com.frontend.educationalapplication.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF9AA9FF),
    secondary = Color(0xFF58CAE1),
    tertiary = Color(0xFFFFCB68),
    background = LearnerDarkBackground,
    surface = LearnerDarkCard,
    surfaceVariant = Color(0xFF202737),
    primaryContainer = Color(0xFF252D3E),
    onPrimaryContainer = LearnerDarkText,
    outline = Color(0xFF364154),
    onPrimary = LearnerDarkBackground,
    onBackground = LearnerDarkText,
    onSurface = LearnerDarkText,
    onSurfaceVariant = LearnerDarkMutedText
)

private val LightColorScheme = lightColorScheme(
    primary = LearnerPrimary,
    secondary = LearnerTeal,
    tertiary = LearnerYellow,
    background = LearnerBackground,
    surface = LearnerCard,
    surfaceVariant = LearnerCard,
    primaryContainer = LearnerPrimaryTint,
    onPrimaryContainer = LearnerText,
    outline = LearnerBorder,
    onPrimary = Color.White,
    onBackground = LearnerText,
    onSurface = LearnerText,
    onSurfaceVariant = LearnerMutedText
)

@Composable
fun EducationalApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
