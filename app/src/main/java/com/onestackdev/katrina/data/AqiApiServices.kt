package com.onestackdev.katrina.data

import com.onestackdev.katrina.model.AqiResponse
import retrofit2.Response
import retrofit2.http.*

interface AqiApiServices {

    @GET("nearest_city")
    suspend fun getAQI(
        @Query("lat") lat: String,
        @Query("lon") long: String,
        @Query("key") tokenApi: String
    ): Response<AqiResponse>
}