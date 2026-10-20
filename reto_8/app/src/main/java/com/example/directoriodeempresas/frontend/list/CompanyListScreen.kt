package com.example.directoriodeempresas.frontend.list

import com.example.directoriodeempresas.frontend.model.CompanyListUiState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Business
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.directoriodeempresas.R
import com.example.directoriodeempresas.frontend.DirectEmpresaNav
import com.example.directoriodeempresas.frontend.SharedViewModel
import com.example.directoriodeempresas.frontend.components.CompanyCard
import com.example.directoriodeempresas.frontend.components.FolioTextField

@Composable
fun CompanyListScreen(
    state: CompanyListUiState,
    query: String,
    onQueryChange: (String) -> Unit,
    onDismissBanner: () -> Unit,
    onAddCompany: () -> Unit,
    onOpenCompany: (String) -> Unit,
    onOpenFilter: () -> Unit,
    sharedViewModel: SharedViewModel,
    navController: NavHostController
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
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
            item(key = "header", contentType = "header") { AppHeader() }
            item(key = "title", contentType = "title") { TitleRow(onAddCompany) }
            state.deletedCompanyName?.let { name ->
                item(key = "banner", contentType = "banner") {
                    DeletedBanner(name, onDismissBanner, Modifier.animateItem())
                }
            }
            item(key = "search", contentType = "search") {
                SearchRow(query, onQueryChange, hasFilters = state.activeFilters > 0, onOpenFilter = onOpenFilter)
            }
            item(key = "count", contentType = "count") { CountRow(state.companies.size) }
            if (!state.isLoading && state.companies.isEmpty()) {
                item(key = "empty", contentType = "empty") {
                    Text(
                        stringResource(R.string.empty_list),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            items(state.companies, key = { it.phone }) { company ->
                CompanyCard(company, {
                    sharedViewModel.cambiarEmpresa(company)
                    navController.navigate(DirectEmpresaNav.DetallesEmpresa.name)
                })
            }
        }
    }
}

@Composable
private fun AppHeader() {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Box(
            Modifier.size(40.dp).clip(RoundedCornerShape(12.dp)).background(MaterialTheme.colorScheme.primary),
            contentAlignment = Alignment.Center,
        ) { Icon(Icons.Outlined.Business, null, tint = MaterialTheme.colorScheme.onPrimary) }
        Text("Directorio", style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight
            (1f))
        Box(
            Modifier.size(40.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center,
        ) { Text("DE", style = MaterialTheme.typography.labelMedium, color = MaterialTheme
            .colorScheme.primary) }
    }
}

@Composable
private fun TitleRow(onAddCompany: () -> Unit) {
    Row(Modifier.padding(top = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(stringResource(R.string.companies_title), style = MaterialTheme.typography.headlineLarge)
            Text(
                stringResource(R.string.companies_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        FilledIconButton(onClick = onAddCompany, modifier = Modifier.size(56.dp), shape = MaterialTheme.shapes.large) {
            Icon(Icons.Outlined.Add, contentDescription = stringResource(R.string.add_company_cd))
        }
    }
}

@Composable
private fun DeletedBanner(name: String, onDismiss: () -> Unit, modifier: Modifier = Modifier) {
    Surface(modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large, color = MaterialTheme.colorScheme.primaryContainer) {
        Row(Modifier.padding(start = 16.dp, top = 12.dp, bottom = 12.dp, end = 4.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Icon(Icons.Outlined.CheckCircle, null, Modifier.padding(top = 2.dp), tint = MaterialTheme.colorScheme.primary)
            Column(Modifier.weight(1f)) {
                Text(stringResource(R.string.banner_title), style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onPrimaryContainer)
                Text(stringResource(R.string.banner_message, name), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onPrimaryContainer)
            }
            IconButton(onClick = onDismiss) {
                Icon(Icons.Outlined.Close, stringResource(R.string.dismiss), tint = MaterialTheme.colorScheme.onPrimaryContainer)
            }
        }
    }
}

@Composable
private fun SearchRow(query: String, onQueryChange: (String) -> Unit, hasFilters: Boolean, onOpenFilter: () -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.Top) {
        FolioTextField(
            value = query, onValueChange = onQueryChange,
            modifier = Modifier.weight(1f),
            placeholder = stringResource(R.string.search_placeholder),
            leadingIcon = Icons.Outlined.Search,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        )
        OutlinedIconButton(
            onClick = onOpenFilter,
            modifier = Modifier.size(56.dp),
            shape = MaterialTheme.shapes.large,
            colors = IconButtonDefaults.outlinedIconButtonColors(containerColor = MaterialTheme.colorScheme.surface),
        ) {
            BadgedBox(badge = { if (hasFilters) Badge() }) {
                Icon(Icons.Outlined.Tune, contentDescription = stringResource(R.string.filter_cd))
            }
        }
    }
}

@Composable
private fun CountRow(count: Int) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(
            pluralStringResource(R.plurals.companies_count, count, count).uppercase(),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f),
        )
        Text(stringResource(R.string.sort_name), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
