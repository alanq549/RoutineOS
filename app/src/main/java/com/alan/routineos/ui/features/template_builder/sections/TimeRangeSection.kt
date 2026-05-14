package com.alan.routineos.ui.features.template_builder.sections

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alan.routineos.ui.features.template_builder.components.TimeField
import com.alan.routineos.ui.theme.MetaMono

@Composable
fun TimeRangeSection() {
    Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)) {
        Text(
            "HORA DE INICIO",
            style = MetaMono.copy(fontSize = 9.sp),
            color = Color(0xFF555555)
        )
        Spacer(modifier = Modifier.height(4.dp))
        TimeField(time = "08:00 AM")
    }
}
