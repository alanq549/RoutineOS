package com.alan.routineos.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.alan.routineos.R

val JetBrainsMono = FontFamily(
    Font(R.font.jetbrains_mono_regular, FontWeight.Normal),
    Font(R.font.jetbrains_mono_medium, FontWeight.Medium),
    Font(R.font.jetbrains_mono_bold, FontWeight.Bold)
)

val MonoTimer = TextStyle(
    fontFamily = JetBrainsMono,
    fontSize = 36.sp,
    fontWeight = FontWeight.Bold
)

val TitleNode = TextStyle(
    fontFamily = JetBrainsMono,
    fontSize = 14.sp,
    fontWeight = FontWeight.Medium
)

val MetaMono = TextStyle(
    fontFamily = JetBrainsMono,
    fontSize = 10.sp,
    letterSpacing = 1.5.sp
)

val Typography = Typography(
    bodyLarge = TextStyle(
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    )
)