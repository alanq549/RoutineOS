package com.alan.routineos.ui.features.library.navigation

import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.alan.routineos.ui.features.library.presentation.LibraryScreen
import com.alan.routineos.ui.features.library.viewmodel.LibraryViewModel
import com.alan.routineos.ui.navigation.Screen

fun NavGraphBuilder.libraryScreen(
    onNavigateToBuilder: (String) -> Unit,
    onNavigateToTypes: () -> Unit
) {
    composable(Screen.Library.route) {
        val viewModel: LibraryViewModel = hiltViewModel()
        LibraryScreen(
            viewModel = viewModel,
            onNavigateToBuilder = onNavigateToBuilder,
            onNavigateToTypes = onNavigateToTypes
        )
    }
}
