package com.farm.farmacia

// Modelo simple para deserializar el JSON de la API
data class Catalogo(
    val recurso: String = "",
    val generado: String = "",
    val total: Int = 0,
    val datos: List<Medicamento> = emptyList()
)
