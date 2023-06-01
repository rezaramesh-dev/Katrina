package com.onestackdev.katrina.data

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.onestackdev.katrina.utils.Constants.BASE_URL_AQI
import com.onestackdev.katrina.utils.Constants.BASE_URL_WEATHER
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ApiModule {

    @Provides
    @Singleton
    @Named("WeatherUrl")
    fun WeatherUrl() = BASE_URL_WEATHER

    @Provides
    @Singleton
    @Named("AQIUrl")
    fun AQIUrl() = BASE_URL_AQI

    @Provides
    @Singleton
    fun ConnectionTimeOut() = 60

    @Provides
    @Singleton
    fun ProvideGson(): Gson = GsonBuilder().setLenient().create()

    @Provides
    @Singleton
    fun ProvideOKHttpClient() = OkHttpClient.Builder().connectTimeout(12, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS).build()

    @Provides
    @Singleton
    @Named("WeatherRetrofit")
    fun ProvideRetrofit(
        @Named("WeatherUrl") baseUrl: String, gson: Gson, client: OkHttpClient
    ): WeatherApiServices = Retrofit.Builder().baseUrl(baseUrl).client(client)
        .addConverterFactory(GsonConverterFactory.create(gson)).build()
        .create(WeatherApiServices::class.java)

    @Provides
    @Singleton
    @Named("AqiRetrofit")
    fun ProvideGoogleRetrofit(
        @Named("AQIUrl") baseUrl: String, gson: Gson, client: OkHttpClient
    ): AqiApiServices = Retrofit.Builder().baseUrl(baseUrl).client(client)
        .addConverterFactory(GsonConverterFactory.create(gson)).build()
        .create(AqiApiServices::class.java)
}