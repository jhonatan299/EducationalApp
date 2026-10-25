package com.frontend.educationalapplication.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.frontend.educationalapplication.R

// Figma "Educational App UI Kit" font.
val CircularDisplayFamily = FontFamily(
    Font(R.font.circular_std_book, FontWeight.Normal),
    Font(R.font.circular_std_medium, FontWeight.Medium),
    Font(R.font.circular_std_bold, FontWeight.Bold),
    Font(R.font.circular_std_black, FontWeight.ExtraBold),
    Font(R.font.circular_std_black, FontWeight.Black)
)

private val BaseTypography = Typography(
    headlineLarge = TextStyle(
        fontFamily = CircularDisplayFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 25.sp,
        lineHeight = 34.sp,
        letterSpacing = 0.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = CircularDisplayFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    titleLarge = TextStyle(
        fontFamily = CircularDisplayFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
        lineHeight = 25.sp,
        letterSpacing = 0.sp
    ),
    titleMedium = TextStyle(
        fontFamily = CircularDisplayFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 17.sp,
        lineHeight = 23.sp,
        letterSpacing = 0.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = CircularDisplayFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 17.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = CircularDisplayFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.sp
    ),
    labelLarge = TextStyle(
        fontFamily = CircularDisplayFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.sp
    ),
    labelMedium = TextStyle(
        fontFamily = CircularDisplayFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 13.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.sp
    )
)

// Slots not customized above still fall back to the system font, so apply Circular Std to all of them.
val Typography = BaseTypography.run {
    Typography(
        displayLarge = displayLarge.copy(fontFamily = CircularDisplayFamily),
        displayMedium = displayMedium.copy(fontFamily = CircularDisplayFamily),
        displaySmall = displaySmall.copy(fontFamily = CircularDisplayFamily),
        headlineLarge = headlineLarge,
        headlineMedium = headlineMedium,
        headlineSmall = headlineSmall.copy(fontFamily = CircularDisplayFamily, fontWeight = FontWeight.Bold),
        titleLarge = titleLarge,
        titleMedium = titleMedium,
        titleSmall = titleSmall.copy(fontFamily = CircularDisplayFamily),
        bodyLarge = bodyLarge,
        bodyMedium = bodyMedium,
        bodySmall = bodySmall.copy(fontFamily = CircularDisplayFamily),
        labelLarge = labelLarge,
        labelMedium = labelMedium,
        labelSmall = labelSmall.copy(fontFamily = CircularDisplayFamily)
    )
}
