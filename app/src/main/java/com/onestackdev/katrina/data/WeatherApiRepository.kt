package com.onestackdev.katrina.data

import com.onestackdev.katrina.utils.Constants.API_KEY_WEATHER
import javax.inject.Inject
import javax.inject.Named

class WeatherApiRepository @Inject constructor(@Named("WeatherRetrofit") private val apiService: WeatherApiServices) {

    suspend fun getWeather(latLng: String) = apiService.getWeather(
        tokenApi = API_KEY_WEATHER, latLong = latLng, days = "1", aqi = "no", alert = "no"
    )
}