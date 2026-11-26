package com.farm.farmacia
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

//Ozuna Ramos Gibran

object Retrofit {

    private const val BASE_URL =
        "https://servicios-proyecto-ed.vercel.app/api/v1/"

    val api: FarmaciaAPI by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(FarmaciaAPI::class.java)
    }
}