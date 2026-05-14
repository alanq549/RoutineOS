package com.alan.routineos.ui.features.onboarding.presentation

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.alan.routineos.ui.features.onboarding.components.*
import com.alan.routineos.ui.features.onboarding.viewmodel.OnboardingViewModel
import com.alan.routineos.ui.theme.*

@Composable
fun OnboardingScreen(
    onFinish: () -> Unit,
    viewModel: OnboardingViewModel
) {
    val uiState by viewModel.uiState.collectAsState()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = ColorBg
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            LinearProgressIndicator(
                progress = { uiState.currentStep / 4f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp),
                color = ColorExec,
                trackColor = ColorBorder
            )
            
            Spacer(modifier = Modifier.height(32.dp))

            Box(modifier = Modifier.weight(1f)) {
                AnimatedContent(targetState = uiState.currentStep, label = "step") { step ->
                    when (step) {
                        1 -> StepRoutineName(uiState.routineName, viewModel::updateRoutineName)
                        2 -> StepNodeTypes(
                            nodeTypes = uiState.nodeTypes,
                            onAdd = viewModel::addNodeType,
                            onRemove = viewModel::removeNodeType
                        )
                        3 -> StepMetadataFields(
                            nodeTypes = uiState.nodeTypes,
                            onUpdateSchemas = viewModel::updateNodeTypeSchemas
                        )
                        4 -> StepSchedule(
                            selectedDays = uiState.selectedDays,
                            startTime = uiState.startTime,
                            onToggleDay = viewModel::toggleDay,
                            onTimeChange = viewModel::updateStartTime
                        )
                    }
                }
            }

            Button(
                onClick = {
                    if (uiState.currentStep < 4) viewModel.nextStep()
                    else {
                        viewModel.finishOnboarding()
                        onFinish()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ColorExec),
                shape = RoundedCornerShape(12.dp),
                enabled = when(uiState.currentStep) {
                    1 -> uiState.routineName.isNotBlank()
                    2 -> uiState.nodeTypes.isNotEmpty()
                    else -> true
                }
            ) {
                Text(
                    text = if (uiState.currentStep < 4) "Continuar" else "Crear mi primera rutina",
                    style = TitleNode.copy(color = Color.White)
                )
            }
        }
    }
}
