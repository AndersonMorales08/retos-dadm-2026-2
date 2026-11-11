package com.example.directoriodeempresas.frontend.filter

import android.annotation.SuppressLint
import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.directoriodeempresas.frontend.list.CompanyListViewModel

@Composable
fun CompanyFilterRoute(
    onBack: () -> Unit,
    // Mismo ViewModel que la lista: se ata a la Activity, no a la entrada del back stack
    @SuppressLint("ContextCastToActivity") viewModel: CompanyListViewModel = viewModel(
        viewModelStoreOwner = LocalContext.current as ComponentActivity
    ),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val query by viewModel.query.collectAsStateWithLifecycle()

    CompanyFilterScreen(
        state = state,
        query = query,
        onQueryChange = viewModel::onQueryChange,
        onClassificationChange = viewModel::onClassificationChange,
        onClearFilters = viewModel::clearFilters,
        onBack = onBack,
        onOpenCompany = {},
    )
}