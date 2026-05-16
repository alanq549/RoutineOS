package com.alan.routineos.ui.features.library.navigation

import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import com.alan.routineos.ui.features.library.presentation.LibraryScreen
import com.alan.routineos.ui.features.library.viewmodel.LibraryViewModel

const val LIBRARY_ROUTE = "library"

fun NavController.navigateToLibrary(navOptions: NavOptions? = null) {
    this.navigate(LIBRARY_ROUTE, navOptions)
}

fun NavGraphBuilder.libraryScreen(
    onNavigateToBuilder: (String) -> Unit,
    onNavigateToTypes: () -> Unit
) {
    composable(LIBRARY_ROUTE) {
        val viewModel: LibraryViewModel = hiltViewModel()
        LibraryScreen(
            viewModel = viewModel,
            onNavigateToBuilder = onNavigateToBuilder,
            onNavigateToTypes = onNavigateToTypes
        )
    }
}
