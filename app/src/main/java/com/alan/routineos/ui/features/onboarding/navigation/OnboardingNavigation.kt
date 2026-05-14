package com.alan.routineos.ui.features.onboarding.navigation

import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.alan.routineos.ui.features.onboarding.presentation.OnboardingScreen
import com.alan.routineos.ui.features.onboarding.viewmodel.OnboardingViewModel

fun NavGraphBuilder.onboardingScreen(
    onFinish: () -> Unit
) {
    composable("onboarding") {
        val viewModel: OnboardingViewModel = hiltViewModel()
        OnboardingScreen(
            onFinish = onFinish,
            viewModel = viewModel
        )
    }
}
