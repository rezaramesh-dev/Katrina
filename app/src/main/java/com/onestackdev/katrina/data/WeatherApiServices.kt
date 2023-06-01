package com.onestackdev.katrina.data

import com.onestackdev.katrina.model.WeatherAPIModel
import retrofit2.Response
import retrofit2.http.*

interface WeatherApiServices {

    @GET("forecast.json")
    suspend fun getWeather(
        @Query("key") tokenApi: String,
        @Query("q") latLong: String,
        @Query("days") days: String,
        @Query("aqi") aqi: String,
        @Query("alerts") alert: String
    ): Response<WeatherAPIModel>
}