package com.example.triqui.features.triqui.ui

import android.content.res.Configuration
import android.util.Log
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.window.core.layout.WindowSizeClass
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.triqui.core.repositories.TriquiRepository

enum class TriquiScreenNav() {
    Online,
    Offline
}
@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun TriquiScreen(
    windowSizeClass: WindowSizeClass = currentWindowAdaptiveInfo(supportLargeAndXLargeWidth =
        true).windowSizeClass,
    navController: NavHostController = rememberNavController()
) {
    val context = LocalContext.current

    val triquiViewModel: TriquiViewModel = viewModel(
        factory = viewModelFactory {
            initializer {
                // Aquí instancias tu ViewModel pasándole lo que necesita
                val savedStateHandle = createSavedStateHandle()
                TriquiViewModel(
                    state= savedStateHandle,
                    repository = TriquiRepository(context)
                )
            }
        }
    )

    val showTopAppBar = windowSizeClass.isHeightAtLeastBreakpoint(WindowSizeClass
        .HEIGHT_DP_MEDIUM_LOWER_BOUND)
    val bgColor = Color(0xFF132F20)
    val neonGreen = Color(0xFF4ADE80)

    val uiState by triquiViewModel.uiState.collectAsState()
    val level by triquiViewModel.level.collectAsStateWithLifecycle()

    NavHost(
        navController = navController,
        startDestination = TriquiScreenNav.Offline.name,
    ) {
        composable(route = TriquiScreenNav.Offline.name) {
            val configuration = LocalConfiguration.current
            val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
            if (isLandscape) {
                Log.d("Cambio de Orientación", "FUNCIONO $isLandscape")
                HorizontalTriquiScreen(
                    triquiViewModel,
                    bgColor,
                    neonGreen,
                    context,
                    uiState,
                    level
                )
            } else {
                VerticalOfflineScreen(
                    triquiViewModel,
                    bgColor,
                    neonGreen,
                    context,
                    uiState,
                    level,
                    onOnlineButtonClicked = { navController.navigate(TriquiScreenNav.Online.name) }
                )
            }
        }
        composable(route = TriquiScreenNav.Online.name) {
            VerticalOnlineScreen(triquiViewModel, bgColor, neonGreen, context, uiState, level,
                onOfflineButtonClicked ={ navController.navigate((TriquiScreenNav.Offline.name)) })
        }
    }
}