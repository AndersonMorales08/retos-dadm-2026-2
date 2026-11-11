package com.example.directoriodeempresas.frontend.form

import android.app.Application
import android.content.ContentValues
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.AndroidViewModel
import com.example.directoriodeempresas.backend.repository.DirectEmpresarialRepository
import com.example.directoriodeempresas.frontend.model.Classification
import com.example.directoriodeempresas.frontend.model.Company
import com.example.directoriodeempresas.frontend.model.CompanyFormUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class CompanyFormViewModel(
    application: Application,
    isEditing: Boolean = false,
    name: String = "",
    classification: Classification? = null,
    website: String = "",
    phone: String = "",
    email: String = "",
    products: String = "",
) : AndroidViewModel(application) {
    private val _uiState = MutableStateFlow(
        CompanyFormUiState(
            isEditing = isEditing,
            name = name,
            classification = classification,
            website = website,
            phone = phone,
            email = email,
            products = products
        )
    )
    val uiState: StateFlow<CompanyFormUiState> = _uiState.asStateFlow()

    init {
        // Aquí ya puedes usar ambos parámetros
        val contexto = getApplication<Application>().applicationContext
    }

    fun onNameChange(v: String) = _uiState.update { it.copy(name = v) }
    fun onClassificationChange(v: Classification?) = _uiState.update { it.copy(classification = v) }
    fun onWebsiteChange(v: String) = _uiState.update { it.copy(website = v) }
    fun onPhoneChange(v: String) = _uiState.update { it.copy(phone = v) }
    fun onEmailChange(v: String) = _uiState.update { it.copy(email = v) }
    fun onProductsChange(v: String) = _uiState.update { it.copy(products = v) }


    suspend fun save() {
        val company: Company = Company(
            name = uiState.value.name,
            classification = uiState.value.classification,
            websiteUrl = uiState.value.website,
            phone = uiState.value.phone,
            email = uiState.value.email,
            productsAndServices = uiState.value.products
        )
        val repository: DirectEmpresarialRepository = DirectEmpresarialRepository(context =
            getApplication())
        val responsesProductServices: MutableList<Long> = mutableListOf()
        val companyBd: ContentValues = repository.empresa2Entry(company)
        val productServicesBd: MutableList<ContentValues> = repository.productsServices2Entry(company)

        val responseEmpresa: Long = repository.insertEmpresa(companyBd)

        if(responseEmpresa == (-1).toLong()) {
            Log.d("ERROR_DB", "Falló al insertar empresa")
        }

        for(prodServ in productServicesBd) {
            val responseProductService = repository.insertProductosServicios(prodServ)
            responsesProductServices.add(responseProductService)
        }

        for(res in responsesProductServices) {
            if(res == (-1).toLong()) {
                Log.d("ERROR_DB", "Falló al insertar product/servicio")
            }
        }

    }

    suspend fun update() {
        val company: Company = Company(
            name = uiState.value.name,
            classification = uiState.value.classification,
            websiteUrl = uiState.value.website,
            phone = uiState.value.phone,
            email = uiState.value.email,
            productsAndServices = uiState.value.products
        )
        val repository: DirectEmpresarialRepository = DirectEmpresarialRepository(context =
            getApplication())

        try {
            val responseEmpresa: Int = repository.updateEmpresa(company)

            if (responseEmpresa == -1) {
                Log.d("ERROR_DB", "Falló al insertar empresa")
            }

            val responseProductService = repository.updateProductsServices(company)

            if (!responseProductService) {
                Log.d("ERROR_DB", "Falló al insertar product/servicio")
            }
        } catch (e: Exception) {

        }


    }
}