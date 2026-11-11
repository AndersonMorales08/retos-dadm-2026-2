package com.example.directoriodeempresas.frontend.list

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import com.example.directoriodeempresas.backend.repository.DirectEmpresarialRepository
import com.example.directoriodeempresas.frontend.model.CompanyListUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import com.example.directoriodeempresas.frontend.model.Classification
import kotlinx.coroutines.flow.SharingStarted
import java.text.Normalizer

class CompanyListViewModel(application: Application) : AndroidViewModel(application) {
    // Estado interno con la lista COMPLETA
    private val _uiState = MutableStateFlow(CompanyListUiState())

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    private val _classification = MutableStateFlow<Classification?>(null)

    // Estado que consume la UI: lista filtrada + contadores
    val uiState: StateFlow<CompanyListUiState> =
        combine(_uiState, _query, _classification) { state, q, clas ->
            val text = q.trim().normalized()
            val filtered = state.companies.filter { company ->
                (text.isEmpty() || company.name.normalized().contains(text)) &&
                        (clas == null || company.classification == clas)
            }
            state.copy(
                companies = filtered,
                classification = clas,
                totalCount = state.companies.size,
                activeFilters = (if (text.isNotEmpty()) 1 else 0) +
                        (if (clas != null) 1 else 0),
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CompanyListUiState())

    fun onQueryChange(newQuery: String) {
        _query.value = newQuery
    }

    fun onClassificationChange(classification: Classification?) {
        _classification.value = classification
    }

    fun clearFilters() {
        _query.value = ""
        _classification.value = null
    }

    suspend fun retrieveEmpresas() {
        val repository = DirectEmpresarialRepository(context = getApplication())
        val companies = repository.retrieveEmpresas()
        Log.d("Recuperación de base de datos", "$companies")
        _uiState.update { it.copy(companies = companies, isLoading = false) }
    }

    // Ignora mayúsculas y tildes ("fabrica" encuentra "Fábrica")
    private fun String.normalized(): String =
        Normalizer.normalize(this, Normalizer.Form.NFD)
            .replace("\\p{InCombiningDiacriticalMarks}+".toRegex(), "")
            .lowercase()
}