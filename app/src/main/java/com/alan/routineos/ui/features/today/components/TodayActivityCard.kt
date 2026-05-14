package com.alan.routineos.ui.features.today.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alan.routineos.ui.theme.*

@Composable
fun TodayActivityCard(
    time: String,
    title: String,
    statusLabel: String,
    statusColor: Color,
    borderColor: Color,
    subItems: List<String> = emptyList()
) {
    Surface(
        color = ColorSurface,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth(),
        border = androidx.compose.foundation.BorderStroke(1.dp, ColorBorder)
    ) {
        Row(modifier = Modifier.height(IntrinsicSize.Min)) {
            // Left color indicator
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .background(borderColor)
            )

            Column(modifier = Modifier.padding(16.dp)) {
                Text(time, style = MetaMono, color = ColorTextDim)
                Text(title, style = TitleNode.copy(fontSize = 18.sp), color = ColorText)
                Spacer(modifier = Modifier.height(8.dp))

                Surface(
                    color = statusColor.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = statusLabel,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        style = MetaMono,
                        color = statusColor
                    )
                }

                if (subItems.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    subItems.forEach { item ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier
                                .size(4.dp)
                                .background(statusColor, CircleShape))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                item,
                                style = MetaMono.copy(fontSize = 12.sp),
                                color = ColorTextDim
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                }
            }
        }
    }
}
