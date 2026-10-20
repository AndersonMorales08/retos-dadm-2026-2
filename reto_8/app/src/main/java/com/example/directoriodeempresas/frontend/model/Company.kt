package com.example.directoriodeempresas.frontend.model

import androidx.compose.runtime.Immutable

enum class Classification { Consultory, CustomDevelopment, SoftwareFactory }

@Immutable
data class Company(
    val name: String,
    val classification: Classification?,
    val websiteUrl: String,
    val phone: String,
    val email: String,
    val productsAndServices: String
) {
    // Computed once per instance (not on every recomposition).
    val initials: String = name.split(' ')
        .filter { it.firstOrNull()?.isLetter() == true }
        .take(2)
        .joinToString("") { it.first().uppercase() }

    val displayWebsite: String = websiteUrl
        .removePrefix("https://").removePrefix("http://").trimEnd('/')
}
