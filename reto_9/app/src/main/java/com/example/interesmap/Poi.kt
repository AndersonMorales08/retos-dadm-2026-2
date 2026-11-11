package com.example.reto9gps

enum class PoiCategory(
    val label: String,
    val searchTerms: List<String>,
    val color: Long
) {
    HEALTH("Salud", listOf("hospital", "clínica", "farmacia"), 0xFFD32F2F),
    TOURISM("Turismo", listOf("museo", "atracción turística"), 0xFF7B1FA2),
    FOOD("Comida", listOf("restaurante", "cafetería"), 0xFFF57C00),
    EDUCATION("Educación", listOf("universidad", "colegio"), 0xFF00897B)
}

data class Poi(
    val id: String,
    val name: String,
    val lat: Double,
    val lon: Double,
    val category: PoiCategory
)