package com.alan.routineos.ui.features.system.presentation

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import com.alan.routineos.ui.features.system.tabs.ActivitiesTab
import com.alan.routineos.ui.features.system.tabs.AdjustmentsTab
import com.alan.routineos.ui.theme.ColorBg
import com.alan.routineos.ui.theme.ColorExec
import com.alan.routineos.ui.theme.ColorSurface
import com.alan.routineos.ui.theme.ColorTextDim
import com.alan.routineos.ui.theme.MetaMono
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun SystemScreen(
    onNavigateToBuilder: (String, String?, String?) -> Unit
) {
    val pagerState = rememberPagerState(pageCount = { 2 })
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        containerColor = ColorBg,
        topBar = {
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
                val titles = listOf("ACTIVIDADES", "CONFIGURACIÓN")
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
    ) { padding ->
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) { page ->
            when (page) {
                0 -> ActivitiesTab(onNavigateToBuilder = onNavigateToBuilder)
                1 -> AdjustmentsTab()
                else -> Unit
            }
        }
    }
}
