package com.example.directoriodeempresas.frontend.detail

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Mail
import androidx.compose.material.icons.outlined.NorthEast
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.directoriodeempresas.R
import com.example.directoriodeempresas.frontend.model.Company
import com.example.directoriodeempresas.frontend.components.CompanyAvatar
import com.example.directoriodeempresas.frontend.components.FolioTopBar
import com.example.directoriodeempresas.frontend.labelRes
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.O)
private val DateFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("MMM d, yyyy")

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CompanyDetailScreen(
    company: Company,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
    onWebsiteClick: (String) -> Unit = {},
    onPhoneClick: (String) -> Unit = {},
    onEmailClick: (String) -> Unit = {},
) {
    var showDeleteSheet by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { FolioTopBar(stringResource(R.string.company_details), onBack) },
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            CompanyAvatar(company.initials, size = 80.dp)
            Spacer(Modifier.height(16.dp))
            Text(company.name, style = MaterialTheme.typography.headlineMedium, textAlign = TextAlign.Center)
            Spacer(Modifier.height(8.dp))
            Surface(shape = RoundedCornerShape(8.dp), color = MaterialTheme.colorScheme.primaryContainer) {
                Text(
                    stringResource(company.classification?.labelRes ?: R.string.class_no_category),
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            Spacer(Modifier.height(12.dp))

            DetailCard {
                if (company.websiteUrl.isNotBlank()) ContactRow(
                    Icons.Outlined.Language, stringResource(R.string.website_url), company.displayWebsite,
                    Icons.Outlined.NorthEast,
                ) { onWebsiteClick(company.websiteUrl) }
                if (company.phone.isNotBlank()) ContactRow(
                    Icons.Outlined.Phone, stringResource(R.string.phone), company.phone,
                    Icons.AutoMirrored.Outlined.KeyboardArrowRight,
                ) { onPhoneClick(company.phone) }
                if (company.email.isNotBlank()) ContactRow(
                    Icons.Outlined.Mail, stringResource(R.string.email), company.email,
                    Icons.AutoMirrored.Outlined.KeyboardArrowRight,
                ) { onEmailClick(company.email) }
            }
            Spacer(Modifier.height(16.dp))
            DetailCard {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(stringResource(R.string.products_services), style = MaterialTheme.typography.titleMedium)
                    Text(
                        company.productsAndServices, style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Spacer(Modifier.height(20.dp))
            Button(
                onClick = onEdit, modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = MaterialTheme.shapes.medium,
            ) {
                Icon(Icons.Outlined.Edit, null, Modifier.size(18.dp))
                Spacer(Modifier.size(8.dp))
                Text(stringResource(R.string.edit_company), fontWeight = FontWeight.Bold)
            }
            TextButton(
                onClick = { showDeleteSheet = true },
                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
            ) {
                Icon(Icons.Outlined.Delete, null, Modifier.size(18.dp))
                Spacer(Modifier.size(8.dp))
                Text(stringResource(R.string.delete_company), fontWeight = FontWeight.SemiBold)
            }
            Spacer(Modifier.height(16.dp))
        }
    }

    if (showDeleteSheet) {
        DeleteConfirmationSheet(
            company = company,
            onCancel = { showDeleteSheet = false },
            onConfirm = { showDeleteSheet = false; onDelete() },
        )
    }
}

@Composable
private fun DetailCard(content: @Composable () -> Unit) {
    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) { Column { content() } }
}

@Composable
private fun ContactRow(
    icon: ImageVector, label: String, value: String, trailing: ImageVector, onClick: () -> Unit,
) {
    Row(
        Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Box(
            Modifier.size(40.dp).clip(RoundedCornerShape(12.dp)).background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center,
        ) { Icon(icon, null, tint = MaterialTheme.colorScheme.primary) }
        Column(Modifier.weight(1f)) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
        }
        Icon(trailing, null, Modifier.size(18.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DeleteConfirmationSheet(company: Company, onCancel: () -> Unit, onConfirm: () -> Unit) {
    ModalBottomSheet(onDismissRequest = onCancel, containerColor = MaterialTheme.colorScheme.surface) {
        DeleteConfirmationContent(company, onCancel, onConfirm)
    }
}

/** Separado del ModalBottomSheet para poder previsualizarlo. */
@Composable
internal fun DeleteConfirmationContent(company: Company, onCancel: () -> Unit, onConfirm: () -> Unit) {
    Column(Modifier.padding(horizontal = 24.dp).padding(bottom = 24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Box(
            Modifier.size(48.dp).clip(RoundedCornerShape(14.dp)).background(MaterialTheme.colorScheme.errorContainer),
            contentAlignment = Alignment.Center,
        ) { Icon(Icons.Outlined.Delete, null, tint = MaterialTheme.colorScheme.error) }
        Text(stringResource(R.string.delete_title), style = MaterialTheme.typography.headlineSmall)
        Text(
            stringResource(R.string.delete_message, company.name),
            style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Surface(
            shape = MaterialTheme.shapes.medium, color = MaterialTheme.colorScheme.background,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        ) {
            Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                CompanyAvatar(company.initials, size = 40.dp)
                Column {
                    Text(company.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                    Text(
                        stringResource(company.classification?.labelRes ?: R.string.class_no_category) + " · " +
                                company
                            .displayWebsite,
                        style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.padding(top = 4.dp)) {
            OutlinedButton(onClick = onCancel, modifier = Modifier.weight(1f).height(52.dp), shape = MaterialTheme.shapes.medium) {
                Text(stringResource(R.string.cancel), fontWeight = FontWeight.Bold)
            }
            Button(
                onClick = onConfirm, modifier = Modifier.weight(1f).height(52.dp), shape = MaterialTheme.shapes.medium,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error, contentColor = MaterialTheme.colorScheme.onError),
            ) { Text(stringResource(R.string.delete_company), fontWeight = FontWeight.Bold) }
        }
    }
}
