package com.frontend.educationalapplication.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable

val LearnerPrimary = Color(0xFF4F6EF7)
val LearnerPrimaryTint = Color(0xFFE8ECFB)
val LearnerBackground = Color(0xFFF8FAFC)
val LearnerCard = Color(0xFFFFFFFF)
val LearnerText = Color(0xFF111827)
val LearnerMutedText = Color(0xFF4B5563)
val LearnerBorder = Color(0xFFE5E7EB)
val LearnerYellow = Color(0xFFFBBF24)
val LearnerCoral = Color(0xFFF2545B)
val LearnerTeal = Color(0xFF22B8CF)

val LearnerDarkBackground = Color(0xFF0E1118)
val LearnerDarkCard = Color(0xFF171C27)
val LearnerDarkText = Color(0xFFF8FAFC)
val LearnerDarkMutedText = Color(0xFFCBD5E1)

private val DarkIndigoCard = Color(0xFF37346E)
private val DarkBlueCard = Color(0xFF1F4B70)
private val DarkRoseCard = Color(0xFF69364F)
private val DarkVioletCard = Color(0xFF4A3C72)
private val DarkCoralCard = Color(0xFF713845)

@Composable
fun learnerCardAccent(accent: Color): Color {
    if (MaterialTheme.colorScheme.background != LearnerDarkBackground) return accent

    return when (accent) {
        LearnerPrimary -> DarkBlueCard
        LearnerCoral -> DarkCoralCard
        Color(0xFF6A63FF) -> DarkIndigoCard
        Color(0xFF4674F5), Color(0xFF3F74F6) -> DarkBlueCard
        Color(0xFFE88CB9) -> DarkRoseCard
        Color(0xFF7A6BEA) -> DarkVioletCard
        else -> accent
    }
}
