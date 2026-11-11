package com.example.directoriodeempresas.frontend.form

import com.example.directoriodeempresas.frontend.model.Classification
import com.example.directoriodeempresas.frontend.model.CompanyFormUiState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.directoriodeempresas.R
import com.example.directoriodeempresas.frontend.components.ClassificationDropdown
import com.example.directoriodeempresas.frontend.components.FolioTextField
import com.example.directoriodeempresas.frontend.components.FolioTopBar

@Composable
fun CompanyFormScreen(
    state: CompanyFormUiState,
    onClose: () -> Unit,
    onNameChange: (String) -> Unit,
    onClassificationChange: (Classification?) -> Unit,
    onWebsiteChange: (String) -> Unit,
    onPhoneChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onProductsChange: (String) -> Unit,
    onSave: () -> Unit,
) {
    val required = stringResource(R.string.required_error)
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            FolioTopBar(
                title = stringResource(if (state.isEditing) R.string.form_edit_title else R.string.form_add_title),
                onBack = onClose,
                actionLabel = stringResource(R.string.cancel),
                onAction = onClose,
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .consumeWindowInsets(padding)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Column {
                Text(
                    stringResource(if (state.isEditing) R.string.form_edit_headline else R.string.form_add_headline),
                    style = MaterialTheme.typography.headlineMedium,
                )
                Text(
                    if (state.isEditing) stringResource(R.string.form_edit_sub, state.originalName)
                    else stringResource(R.string.form_add_sub),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            FolioTextField(
                value = state.name, onValueChange = onNameChange,
                label = stringResource(R.string.field_name), required = true,
                errorText = required.takeIf { state.nameError },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            )
            ClassificationDropdown(
                selected = state.classification, onSelected = onClassificationChange,
                label = stringResource(R.string.field_classification), required = true,
                placeholder = stringResource(R.string.select_classification),
                errorText = required.takeIf { state.classificationError },
            )
            FolioTextField(
                value = state.website, onValueChange = onWebsiteChange,
                label = stringResource(R.string.website_url),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri, imeAction = ImeAction.Next),
            )
            FolioTextField(
                value = state.phone, onValueChange = onPhoneChange,
                label = stringResource(R.string.phone),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Next),
            )
            FolioTextField(
                value = state.email, onValueChange = onEmailChange,
                label = stringResource(R.string.email),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
            )
            FolioTextField(
                value = state.products, onValueChange = onProductsChange,
                label = stringResource(R.string.products_services), required = true,
                errorText = required.takeIf { state.productsError },
                singleLine = false, minLines = 3,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Default),
            )
            Button(
                onClick = onSave,
                enabled = !state.isSaving && !state.isLoading,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = MaterialTheme.shapes.medium,
            ) {
                Icon(if (state.isEditing) Icons.Outlined.Check else Icons.Outlined.Add, null, Modifier.size(18.dp))
                Spacer(Modifier.size(8.dp))
                Text(
                    stringResource(if (state.isEditing) R.string.save_changes else R.string.add_company_cd),
                    fontWeight = FontWeight.Bold,
                )
            }
            Text(
                stringResource(R.string.required_note),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(8.dp))
        }
    }
}
