package com.example.directoriodeempresas.frontend.list

import android.annotation.SuppressLint
import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.directoriodeempresas.frontend.SharedViewModel

@Composable
fun CompanyListRoute(
    @SuppressLint("ContextCastToActivity") viewModel: CompanyListViewModel = viewModel(
        viewModelStoreOwner = LocalContext.current as ComponentActivity
    ),
    onAddCompany: () -> Unit,
    onOpenFilter: () -> Unit,
    sharedViewModel: SharedViewModel,
    navController: NavHostController
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val query by viewModel.query.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.retrieveEmpresas()
    }

    CompanyListScreen(
        state,
        query = query,
        onQueryChange = viewModel::onQueryChange,
        onDismissBanner = {},
        onAddCompany = onAddCompany,
        onOpenCompany = {},
        onOpenFilter = onOpenFilter,
        sharedViewModel = sharedViewModel,
        navController = navController,
    )
}