package com.alan.routineos.ui.features.planner.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alan.routineos.ui.theme.*

@Composable
fun PlannerActivityCard(
    time: String,
    title: String,
    color: Color,
    tags: List<String>,
    tagColors: List<Color>? = null
) {
    Surface(
        color = ColorSurface,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth(),
        border = androidx.compose.foundation.BorderStroke(1.dp, ColorBorder)
    ) {
        Row(modifier = Modifier.height(IntrinsicSize.Min)) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .background(color)
            )

            Column(modifier = Modifier.padding(16.dp)) {
                Text(time, style = MetaMono, color = ColorTextDim)
                Text(title, style = TitleNode.copy(fontSize = 18.sp), color = ColorText)

                if (tags.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        tags.forEachIndexed { index, tag ->
                            val currentTagColor = tagColors?.getOrNull(index) ?: color
                            Surface(
                                color = currentTagColor.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = tag,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    style = MetaMono.copy(fontSize = 10.sp),
                                    color = currentTagColor
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
