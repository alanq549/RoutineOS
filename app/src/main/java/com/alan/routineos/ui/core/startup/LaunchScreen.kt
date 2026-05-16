package com.alan.routineos.ui.core.startup

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alan.routineos.R
import com.alan.routineos.ui.theme.ColorBg
import com.alan.routineos.ui.theme.JetBrainsMono
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun LaunchScreen(
    state: AppStartupState,
    onFinish: (AppStartupState) -> Unit
) {
    val alpha = remember { Animatable(0f) }
    val scale = remember { Animatable(0.85f) }

    LaunchedEffect(state) {
        if (state is AppStartupState.Loading) return@LaunchedEffect

        // Animación premium: escala con rebote y fade in simultáneos
        launch {
            scale.animateTo(1f, tween(600, easing = EaseOutBack))
        }
        launch {
            alpha.animateTo(1f, tween(600))
        }

        delay(1200) // Tiempo para apreciar la animación
        onFinish(state)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ColorBg),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Image(
                painter = painterResource(R.drawable.icono_2),
                contentDescription = null,
                modifier = Modifier
                    .size(100.dp)
                    .alpha(alpha.value)
                    .graphicsLayer {
                        scaleX = scale.value
                        scaleY = scale.value
                    }
            )

            Spacer(Modifier.height(16.dp))

            Text(
                text = "RoutineOS",
                style = androidx.compose.ui.text.TextStyle(
                    fontFamily = JetBrainsMono,
                    fontSize = 24.sp,
                    color = Color(0xFFE6EDF3),
                    letterSpacing = 2.sp
                ),
                modifier = Modifier.alpha(alpha.value)
            )
        }
    }
}
