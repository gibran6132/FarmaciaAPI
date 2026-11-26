package com.farm.farmacia
//Ozuna Ramos Gibran
import retrofit2.http.GET

interface FarmaciaAPI {
    @GET("catalogo_medicamentos")
    suspend fun obtenerMedicamentos(): Catalogo
}
