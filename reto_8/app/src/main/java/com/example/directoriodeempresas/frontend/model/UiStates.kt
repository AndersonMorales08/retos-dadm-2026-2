package com.example.directoriodeempresas.frontend.model

import androidx.compose.runtime.Immutable

@Immutable
data class CompanyListUiState(
    val isLoading: Boolean = false,
    val companies: List<Company> = emptyList(),
    val totalCount: Int = 0,
    val classification: Classification? = null,
    val activeFilters: Int = 0,
    val deletedCompanyName: String? = null,
)

@Immutable
data class CompanyFormUiState(
    val isEditing: Boolean = false,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val originalName: String = "",
    val name: String = "",
    val classification: Classification? = null,
    val website: String = "",
    val phone: String = "",
    val email: String = "",
    val products: String = "",
    val showErrors: Boolean = false,
) {
    val nameError get() = showErrors && name.isBlank()
    val classificationError get() = showErrors && classification == null
    val productsError get() = showErrors && products.isBlank()
}
