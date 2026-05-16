package com.alan.routineos.ui.features.onboarding.navigation

import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import com.alan.routineos.ui.features.onboarding.presentation.OnboardingScreen
import com.alan.routineos.ui.features.onboarding.viewmodel.OnboardingViewModel

const val ONBOARDING_ROUTE = "onboarding"

fun NavController.navigateToOnboarding(navOptions: NavOptions? = null) {
    this.navigate(ONBOARDING_ROUTE, navOptions)
}

fun NavGraphBuilder.onboardingScreen(
    onFinish: () -> Unit
) {
    composable(ONBOARDING_ROUTE) {
        val viewModel: OnboardingViewModel = hiltViewModel()
        OnboardingScreen(
            onFinish = onFinish,
            viewModel = viewModel
        )
    }
}
