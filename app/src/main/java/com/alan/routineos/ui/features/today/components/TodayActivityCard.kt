package com.alan.routineos.ui.features.today.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alan.routineos.ui.features.today.state.ResolvedNodeUi
import com.alan.routineos.ui.theme.ColorBorder
import com.alan.routineos.ui.theme.ColorSurface
import com.alan.routineos.ui.theme.ColorText
import com.alan.routineos.ui.theme.ColorTextDim
import com.alan.routineos.ui.theme.MetaMono
import com.alan.routineos.ui.theme.TitleNode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodayActivityCard(
    time: String,
    title: String,
    subtitle: String? = null,
    statusLabel: String,
    statusColor: Color,
    barColor: Color,
    isCancelled: Boolean = false,
    hasConflict: Boolean = false,
    resolvedNodes: List<ResolvedNodeUi> = emptyList(),
    onComplete: () -> Unit = {},
    onSkip: () -> Unit = {}
) {
    var expanded by remember { mutableStateOf(false) }
    val dismissState = rememberSwipeToDismissBoxState()

    LaunchedEffect(dismissState.currentValue) {
        when (dismissState.currentValue) {
            SwipeToDismissBoxValue.StartToEnd -> {
                onComplete(); dismissState.reset()
            }

            SwipeToDismissBoxValue.EndToStart -> {
                onSkip(); dismissState.reset()
            }

            else -> {}
        }
    }

    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            val direction = dismissState.dismissDirection
            val color = when (direction) {
                SwipeToDismissBoxValue.StartToEnd -> Color(0xFF4CAF50)
                SwipeToDismissBoxValue.EndToStart -> Color(0xFF333333)
                else -> Color.Transparent
            }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color, RoundedCornerShape(12.dp))
                    .padding(horizontal = 20.dp),
                contentAlignment = if (direction == SwipeToDismissBoxValue.StartToEnd) Alignment.CenterStart else Alignment.CenterEnd
            ) {
                Icon(
                    if (direction == SwipeToDismissBoxValue.StartToEnd) Icons.Default.Check else Icons.Default.Close,
                    contentDescription = null,
                    tint = Color.White
                )
            }
        },
        modifier = Modifier.alpha(if (isCancelled) 0.4f else 1f)
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
                        .width(3.dp)
                        .fillMaxHeight()
                        .background(barColor)
                )

                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .weight(1f)
                ) {
                    Text(time, style = MetaMono, color = ColorTextDim)
                    Text(title, style = TitleNode.copy(fontSize = 18.sp), color = ColorText)
                    if (subtitle != null) {
                        Text(subtitle, style = MetaMono, color = ColorTextDim)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            color = statusColor.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = statusLabel,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                style = MetaMono.copy(fontSize = 10.sp),
                                color = statusColor
                            )
                        }

                        if (hasConflict) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                color = Color(0xFFE65100).copy(alpha = 0.15f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    "⚠ conflicto",
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                    style = MetaMono.copy(fontSize = 10.sp),
                                    color = Color(0xFFE65100)
                                )
                            }
                        }
                    }

                    AnimatedVisibility(visible = expanded) {
                        Column(modifier = Modifier.padding(top = 12.dp)) {
                            resolvedNodes.forEach { node ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(
                                            start = (node.depth * 12).dp,
                                            top = 2.dp,
                                            bottom = 2.dp
                                        ), verticalAlignment = Alignment.CenterVertically
                                ) {
                                    val prefix = if (node.depth > 0) "· " else ""
                                    Text(
                                        text = "$prefix${node.name}",
                                        style = TitleNode.copy(
                                            fontSize = 13.sp,
                                            fontWeight = if (node.depth == 0) FontWeight.W500 else FontWeight.W400
                                        ),
                                        color = ColorText,
                                        modifier = Modifier.weight(1f)
                                    )
                                    if (node.valueSummary != null) {
                                        Text(
                                            node.valueSummary,
                                            style = TitleNode.copy(fontSize = 10.sp),
                                            color = Color(0xFF444444)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                if (resolvedNodes.isNotEmpty()) {
                    IconButton(
                        onClick = { expanded = !expanded },
                        modifier = Modifier.align(Alignment.CenterVertically)
                    ) {
                        Icon(
                            if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = null,
                            tint = ColorTextDim
                        )
                    }
                }
            }
        }
    }
}
