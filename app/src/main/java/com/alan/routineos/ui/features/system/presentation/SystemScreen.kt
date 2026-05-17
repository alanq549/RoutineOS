package com.alan.routineos.ui.features.system.presentation

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.alan.routineos.ui.features.system.tabs.ActivitiesTab
import com.alan.routineos.ui.features.system.tabs.AdjustmentsTab
import com.alan.routineos.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun SystemScreen(
    onNavigateToBuilder: (String) -> Unit
) {
    val pagerState = rememberPagerState(pageCount = { 2 })
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        containerColor = ColorBg,
        topBar = {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("MI SISTEMA", style = MetaMono, color = ColorText)
                    Text("5 actividades", style = MetaMono, color = ColorTextDim)
                }
                TabRow(
                    selectedTabIndex = pagerState.currentPage,
                    containerColor = ColorSurface,
                    contentColor = ColorExec,
                    indicator = { tabPositions ->
                        if (pagerState.currentPage < tabPositions.size) {
                            TabRowDefaults.SecondaryIndicator(
                                Modifier.tabIndicatorOffset(tabPositions[pagerState.currentPage]),
                                color = ColorExec
                            )
                        }
                    }
                ) {
                    val titles = listOf("ACTIVIDADES", "AJUSTES")
                    titles.forEachIndexed { index, title ->
                        Tab(
                            selected = pagerState.currentPage == index,
                            onClick = {
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(index)
                                }
                            },
                            text = {
                                Text(
                                    text = title,
                                    style = MetaMono,
                                    color = if (pagerState.currentPage == index) ColorExec else ColorTextDim
                                )
                            }
                        )
                    }
                }
            }
        }
    ) { padding ->
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) { page ->
            when (page) {
                0 -> ActivitiesTab(onNavigateToBuilder)
                1 -> AdjustmentsTab()
                else -> Unit
            }
        }
    }
}
