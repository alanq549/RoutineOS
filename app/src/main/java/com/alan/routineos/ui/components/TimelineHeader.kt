package com.alan.routineos.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alan.routineos.ui.theme.JetBrainsMono
import com.alan.routineos.ui.theme.TextSecondary

@Composable
fun TimelineHeader() {
    Text(
        text = "TIMELINE // HOY",
        fontSize = 11.sp,
        fontFamily = JetBrainsMono,
        fontWeight = FontWeight.Bold,
        color = TextSecondary,
        modifier = Modifier.padding(bottom = 8.dp)
    )
}