package com.example.directoriodeempresas.frontend.detail

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.directoriodeempresas.frontend.SharedViewModel
import kotlinx.coroutines.launch

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun CompanyDetailRoute(
    viewModel: CompanyDetailViewModel = viewModel(),
    sharedViewModel: SharedViewModel,
    onBack: () -> Unit,
    onEdit: () -> Unit,
) {
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        val newCompany = viewModel.retrieveProductsServices(sharedViewModel.sharedCompany)
        sharedViewModel.sharedCompany = newCompany
    }

    CompanyDetailScreen(
        onBack = onBack,
        onEdit = onEdit,
        onDelete = {
            scope.launch {
                viewModel.deleteEmpresa(sharedViewModel.sharedCompany.phone)
            }
        },
        onWebsiteClick = { },
        onPhoneClick = { },
        onEmailClick = { },
        company = sharedViewModel.sharedCompany
    )
}