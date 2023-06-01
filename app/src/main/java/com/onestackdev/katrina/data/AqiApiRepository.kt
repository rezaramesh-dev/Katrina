package com.onestackdev.katrina.data

import com.onestackdev.katrina.utils.Constants.API_KEY_AQI
import javax.inject.Inject
import javax.inject.Named

class AqiApiRepository @Inject constructor(@Named("AqiRetrofit") private val apiService: AqiApiServices) {

    suspend fun getAQI(lat: String, long: String) = apiService.getAQI(lat, long, API_KEY_AQI)
}