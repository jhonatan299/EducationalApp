package com.frontend.educationalapplication.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

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
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = Typography,
        content = content
    )
}
