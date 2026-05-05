package com.alan.routineos.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alan.routineos.ui.theme.*

// ==========================================
// MODELOS DE MUESTRA (Temporales)
// ==========================================


// ==========================================
// COMPONENTE
// ==========================================

@Composable
fun ActivityTimelineItem(activity: ActivityModel) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Column(
            modifier = Modifier.width(56.dp),
            horizontalAlignment = Alignment.End
        ) {
            Text(
                text = activity.time,
                fontSize = 13.sp,
                fontFamily = JetBrainsMono,
                fontWeight = FontWeight.Medium,
                color = if (activity.isDone) TextSecondary else TextPrimary
            )
            Text(
                text = activity.duration,
                fontSize = 10.sp,
                fontFamily = JetBrainsMono,
                color = TextSecondary
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(if (activity.isDone) NeonEmerald else Color.Transparent)
                    .border(2.dp, if (activity.isDone) NeonEmerald else Color.White.copy(alpha = 0.2f), CircleShape)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        ActivityCard(activity = activity)
    }
}

@Composable
private fun ActivityCard(activity: ActivityModel) {
    val typeColor = when (activity.type) {
        ActivityType.Habit -> HabitGreen
        ActivityType.Task -> TaskBlue
        ActivityType.Workout -> WorkoutRed
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(if (activity.isDone) GlassWhite.copy(alpha = 0.02f) else GlassWhite)
            .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(20.dp))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = activity.title,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (activity.isDone) TextSecondary else TextPrimary,
                textDecoration = if (activity.isDone) TextDecoration.LineThrough else null
            )
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(if (activity.isDone) NeonEmerald else Color.Transparent)
                    .border(2.dp, if (activity.isDone) NeonEmerald else Color.White.copy(alpha = 0.2f), CircleShape)
                    .clickable { /* Acción de check */ },
                contentAlignment = Alignment.Center
            ) {
                if (activity.isDone) {
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = "Completado",
                        tint = BgDark,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
        }

        if (activity.description.isNotEmpty()) {
            Text(
                text = activity.description,
                fontSize = 13.sp,
                color = TextSecondary,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        Row(modifier = Modifier.padding(top = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(typeColor))
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = activity.type.name.uppercase(),
                fontSize = 9.sp,
                fontFamily = JetBrainsMono,
                fontWeight = FontWeight.Bold,
                color = typeColor
            )
        }
    }
}