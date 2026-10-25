package com.frontend.educationalapplication.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val LightColorScheme = lightColorScheme(
    primary = LearnerPrimary,
    secondary = LearnerBlue,
    tertiary = LearnerPink,
    background = LearnerBackground,
    surface = LearnerCard,
    surfaceVariant = LearnerCardVariant,
    surfaceContainer = LearnerCard,
    surfaceContainerLow = LearnerCard,
    surfaceContainerHigh = LearnerCardVariant,
    surfaceContainerHighest = LearnerCardVariant,
    primaryContainer = LearnerPrimaryTint,
    onPrimaryContainer = LearnerPrimary,
    secondaryContainer = LearnerNavActiveBg,
    onSecondaryContainer = LearnerNavActive,
    // Darker than LearnerBorder so outlined text fields stay visible on white.
    outline = Color(0xFFD5DAE8),
    outlineVariant = LearnerBorder,
    onPrimary = Color.White,
    onBackground = LearnerText,
    onSurface = LearnerText,
    onSurfaceVariant = LearnerMutedText,
    error = LearnerCoral
)

// Figma uses 10dp corners for cards, inputs and buttons.
private val LearnerShapes = Shapes(
    extraSmall = RoundedCornerShape(6.dp),
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(10.dp),
    large = RoundedCornerShape(10.dp),
    extraLarge = RoundedCornerShape(16.dp)
)

@Composable
fun EducationalApplicationTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = Typography,
        shapes = LearnerShapes,
        content = content
    )
}
