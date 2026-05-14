package com.alan.routineos.ui.features.today.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.alan.routineos.ui.theme.ColorPlan
import com.alan.routineos.ui.theme.MetaMono

@Composable
fun TimelineIndicator() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(ColorPlan, CircleShape)
        )
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            thickness = 2.dp,
            color = ColorPlan
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text("AHORA", style = MetaMono, color = ColorPlan)
    }
}
