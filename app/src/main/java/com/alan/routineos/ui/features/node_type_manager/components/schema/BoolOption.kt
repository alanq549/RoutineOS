package com.alan.routineos.ui.features.node_type_manager.components.schema

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alan.routineos.ui.theme.TitleNode

@Composable
fun BoolOption(
    label: String,
    selected: Boolean,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bg  = if (selected) color.copy(alpha = .15f) else Color(0xFF1A1A1A)
    val fg  = if (selected) color else Color(0xFF555555)
    val brd = if (selected) color.copy(alpha = .5f) else Color(0xFF2A2A2A)

    Surface(
        modifier = modifier
            .height(44.dp)
            .clickable { onClick() },
        color = bg,
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(if (selected) 1.dp else 0.5.dp, brd)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                label,
                style = TitleNode.copy(fontSize = 13.sp, fontWeight = FontWeight.Medium),
                color = fg
            )
        }
    }
}
