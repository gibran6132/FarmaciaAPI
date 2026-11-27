package com.farm.farmacia

// Ozuna Ramos Gibran

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface FarmaciaAPI {
    @GET("catalogo_medicamentos")
    suspend fun obtenerMedicamentos(): Catalogo

    @POST("catalogo_medicamentos")
    suspend fun nuevoMedicamento(@Body medicamento: Medicamento): Response<ResponseBody>

    @PUT("catalogo_medicamentos/{clave}")
    suspend fun actualizarMedicamento(@Path("clave") clave: String, @Body medicamento: Medicamento): Response<ResponseBody>

    @DELETE("catalogo_medicamentos/{clave}")
    suspend fun eliminarMedicamento(@Path("clave") clave: String): Response<ResponseBody>
}
