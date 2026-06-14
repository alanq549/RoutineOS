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
                progress = { uiState.currentStep.toFloat() / uiState.totalSteps.toFloat() },
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
                        1 -> StepRoutineName(
                            name = uiState.routineName,
                            onNameChange = viewModel::updateRoutineName
                        )
                        2 -> StepTypeChoice(
                            onChoice = viewModel::setCustomizationChoice
                        )
                        3 -> {
                            if (!uiState.isCustomizingTypes) {
                                StepSchedule(
                                    selectedDays = uiState.selectedDays,
                                    startTime = uiState.startTime,
                                    endTime = uiState.endTime,
                                    onToggleDay = viewModel::toggleDay,
                                    onStartTimeChange = viewModel::updateStartTime,
                                    onEndTimeChange = viewModel::updateEndTime
                                )
                            } else {
                                StepNodeTypes(
                                    nodeTypes = uiState.nodeTypes,
                                    onAdd = viewModel::addNodeType,
                                    onRemove = viewModel::removeNodeType
                                )
                            }
                        }
                        4 -> {
                            if (uiState.nodeTypes.any { it.hasMetrics }) {
                                StepMetadataFields(
                                    nodeTypes = uiState.nodeTypes,
                                    onUpdateSchemas = viewModel::updateNodeTypeSchemas
                                )
                            } else {
                                StepSchedule(
                                    selectedDays = uiState.selectedDays,
                                    startTime = uiState.startTime,
                                    endTime = uiState.endTime,
                                    onToggleDay = viewModel::toggleDay,
                                    onStartTimeChange = viewModel::updateStartTime,
                                    onEndTimeChange = viewModel::updateEndTime
                                )
                            }
                        }
                        5 -> StepSchedule(
                            selectedDays = uiState.selectedDays,
                            startTime = uiState.startTime,
                            endTime = uiState.endTime,
                            onToggleDay = viewModel::toggleDay,
                            onStartTimeChange = viewModel::updateStartTime,
                            onEndTimeChange = viewModel::updateEndTime
                        )
                    }
                }
            }

            // Hide "Next" button on Step 2 since the choice cards trigger the next step
            if (uiState.currentStep != 2) {
                Button(
                    onClick = {
                        if (uiState.currentStep < uiState.totalSteps) viewModel.nextStep()
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
                        3 -> if (uiState.isCustomizingTypes) uiState.nodeTypes.isNotEmpty() else (uiState.isTimeValid && uiState.selectedDays.isNotEmpty())
                        uiState.totalSteps -> uiState.isTimeValid && uiState.selectedDays.isNotEmpty()
                        else -> true
                    }
                ) {
                    val buttonText = if (uiState.currentStep < uiState.totalSteps) "Siguiente paso" else "Comenzar mi viaje"
                    Text(
                        text = buttonText,
                        style = TitleNode.copy(color = Color.White)
                    )
                }
            }
        }
    }
}
