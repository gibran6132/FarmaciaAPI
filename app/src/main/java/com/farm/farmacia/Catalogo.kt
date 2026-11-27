package com.farm.farmacia

data class Catalogo(
    val recurso: String = "",
    val generado: String = "",
    val total: Int = 0,
    val datos: List<Medicamento> = emptyList()
)
