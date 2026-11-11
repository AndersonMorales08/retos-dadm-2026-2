package com.example.directoriodeempresas.frontend.filter

import com.example.directoriodeempresas.frontend.model.Classification
import com.example.directoriodeempresas.frontend.model.CompanyListUiState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.directoriodeempresas.R
import com.example.directoriodeempresas.frontend.components.ClassificationDropdown
import com.example.directoriodeempresas.frontend.components.FolioBottomBar
import com.example.directoriodeempresas.frontend.components.FolioTextField
import com.example.directoriodeempresas.frontend.components.FolioTopBar

@Composable
fun CompanyFilterScreen(
    state: CompanyListUiState,
    query: String,
    onQueryChange: (String) -> Unit,
    onClassificationChange: (Classification?) -> Unit,
    onClearFilters: () -> Unit,
    onBack: () -> Unit,
    onOpenCompany: (String) -> Unit,
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            FolioTopBar(
                title = stringResource(R.string.filter_title),
                onBack = onBack,
                actionLabel = stringResource(R.string.reset),
                onAction = onClearFilters,
            )
        },
        bottomBar = { FolioBottomBar() },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 24.dp, end = 24.dp,
                top = padding.calculateTopPadding() + 8.dp,
                bottom = padding.calculateBottomPadding() + 24.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item(key = "headline") {
                Column {
                    Text(stringResource(R.string.filter_headline), style = MaterialTheme.typography.headlineMedium)
                    Text(
                        stringResource(R.string.filter_sub), style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            item(key = "filters") {
                OutlinedCard(
                    shape = MaterialTheme.shapes.large,
                    colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                ) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        FolioTextField(
                            value = query, onValueChange = onQueryChange,
                            label = stringResource(R.string.field_name),
                        )
                        ClassificationDropdown(
                            selected = state.classification,
                            onSelected = onClassificationChange,
                            label = stringResource(R.string.field_classification),
                            placeholder = stringResource(R.string.all_classifications),
                            clearLabel = stringResource(R.string.all_classifications),
                        )
                        FiltersStatus(state.activeFilters)
                    }
                }
            }
            item(key = "results") {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        stringResource(R.string.filter_results, state.companies.size, state.totalCount),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f),
                    )
                    TextButton(onClick = onClearFilters) {
                        Text(stringResource(R.string.clear_all), fontWeight = FontWeight.Bold)
                    }
                }
            }
            if (!state.isLoading && state.companies.isEmpty()) {
                item(key = "empty") {
                    Text(
                        stringResource(R.string.no_results), style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            item(key = "footer") {
                Column(Modifier.padding(top = 12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(stringResource(R.string.filter_footer_title), style = MaterialTheme.typography.titleSmall)
                    Text(
                        stringResource(R.string.filter_footer_body), style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun FiltersStatus(activeFilters: Int) {
    val text = when (activeFilters) {
        0 -> R.string.filters_none
        1 -> R.string.filters_one
        else -> R.string.filters_both
    }
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Icon(Icons.Outlined.CheckCircle, null, Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
        Text(
            stringResource(text), style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.primary, modifier = Modifier.weight(1f),
        )
        Icon(Icons.Outlined.Tune, null, Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
    }
}
