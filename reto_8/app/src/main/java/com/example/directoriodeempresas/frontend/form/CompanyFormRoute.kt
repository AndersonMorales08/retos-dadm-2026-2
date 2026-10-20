package com.example.directoriodeempresas.frontend.form

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch

@Composable
fun CompanyFormRoute(
    onClose: () -> Unit,
    viewModel: CompanyFormViewModel = viewModel(),
) {

    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()

    CompanyFormScreen(
        state = state,
        onClose = onClose,
        onNameChange = { name -> viewModel.onNameChange(name) },
        onClassificationChange = { clasificacion -> viewModel.onClassificationChange(clasificacion) },
        onWebsiteChange = { web -> viewModel.onWebsiteChange(web) },
        onPhoneChange = { phone -> viewModel.onPhoneChange(phone) },
        onEmailChange = { email -> viewModel.onEmailChange(email) },
        onProductsChange = { product -> viewModel.onProductsChange(product) },
        onSave = {
            scope.launch {
                if(state.isEditing) {
                    viewModel.update()
                } else {
                    viewModel.save()
                }
            }
        },
    )

}