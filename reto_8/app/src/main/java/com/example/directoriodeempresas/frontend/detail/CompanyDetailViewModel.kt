package com.example.directoriodeempresas.frontend.detail

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import com.example.directoriodeempresas.backend.repository.DirectEmpresarialRepository
import com.example.directoriodeempresas.frontend.model.Company
class CompanyDetailViewModel(
    application: Application
): AndroidViewModel(application) {

    suspend fun retrieveProductsServices(company: Company): Company {
        val repository = DirectEmpresarialRepository(context =
            getApplication())

        Log.d("Recuperación de base de datos", repository.retrieveProductsServices(company))
        return company.copy(productsAndServices = repository.retrieveProductsServices(company) )
    }

    suspend fun deleteEmpresa(phone: String) {
        val repository = DirectEmpresarialRepository(context =
            getApplication())

        try {
            repository.deleteEmpresa(phone)
        } catch (e: Exception) {

        }
    }


}