package com.example.directoriodeempresas.frontend

import androidx.lifecycle.ViewModel
import com.example.directoriodeempresas.frontend.model.Company
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.example.directoriodeempresas.frontend.model.Classification

class SharedViewModel: ViewModel() {
    var sharedCompany by mutableStateOf<Company>(
        Company(
            name = "",
            classification = Classification.Consultory,
            websiteUrl = "",
            phone = "",
            email = "",
            productsAndServices = ""
        )
    )

    fun cambiarEmpresa(company: Company) {
        sharedCompany = company
    }
}