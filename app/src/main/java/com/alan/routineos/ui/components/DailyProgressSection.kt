package com.alan.routineos.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alan.routineos.ui.theme.GlassWhite
import com.alan.routineos.ui.theme.NeonEmerald
import com.alan.routineos.ui.theme.TextPrimary
import com.alan.routineos.ui.theme.TextSecondary

@Composable
fun DailyProgressSection(completed: Int, total: Int) {
    val progress = if (total > 0) completed.toFloat() / total.toFloat() else 0f
    val animatedProgress by animateFloatAsState(targetValue = progress, label = "progress")

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(GlassWhite)
            .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(24.dp))
            .padding(20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Progreso del día",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
                Text(
                    text = "$completed de $total actividades completadas",
                    fontSize = 13.sp,
                    color = TextSecondary
                )
            }
            Text(
                text = "${(progress * 100).toInt()}%",
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                color = NeonEmerald,
                letterSpacing = (-1).sp
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        LinearProgressIndicator(
            progress = { animatedProgress },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(CircleShape),
            color = NeonEmerald,
            trackColor = Color.White.copy(alpha = 0.1f)
        )
    }
}