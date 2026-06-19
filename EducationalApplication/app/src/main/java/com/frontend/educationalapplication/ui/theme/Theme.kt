package com.frontend.educationalapplication.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = LearnerPrimary,
    secondary = LearnerTeal,
    tertiary = LearnerYellow,
    background = LearnerDarkBackground,
    surface = LearnerDarkCard,
    surfaceVariant = LearnerDarkCard,
    onPrimary = Color.White,
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
