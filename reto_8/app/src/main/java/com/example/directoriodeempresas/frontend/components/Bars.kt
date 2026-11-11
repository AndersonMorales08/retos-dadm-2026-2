package com.example.directoriodeempresas.frontend.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Business
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.example.directoriodeempresas.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FolioTopBar(
    title: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onAction: () -> Unit = {},
) {
    TopAppBar(
        title = { Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Normal) },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = stringResource(R.string.back))
            }
        },
        actions = {
            if (actionLabel != null) {
                TextButton(onClick = onAction) { Text(actionLabel, fontWeight = FontWeight.Bold) }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
        modifier = modifier,
    )
}

/** Only "Companies" is implemented; Saved/Settings are placeholders. */
@Composable
fun FolioBottomBar(modifier: Modifier = Modifier) {
    val colors = NavigationBarItemDefaults.colors(
        indicatorColor = Color.Transparent,
        selectedIconColor = MaterialTheme.colorScheme.primary,
        selectedTextColor = MaterialTheme.colorScheme.primary,
        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    NavigationBar(modifier, containerColor = MaterialTheme.colorScheme.surface) {
        NavigationBarItem(true, {}, { Icon(Icons.Outlined.Business, null) },
            label = { Text(stringResource(R.string.nav_companies)) }, colors = colors)
        NavigationBarItem(false, {}, { Icon(Icons.Outlined.BookmarkBorder, null) },
            label = { Text(stringResource(R.string.nav_saved)) }, colors = colors)
        NavigationBarItem(false, {}, { Icon(Icons.Outlined.Tune, null) },
            label = { Text(stringResource(R.string.nav_settings)) }, colors = colors)
    }
}
