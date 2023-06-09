package com.onestackdev.katrina.screens.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.content.IntentSender
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsStatusCodes
import com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY
import com.onestackdev.katrina.R
import com.onestackdev.katrina.data.AqiApiRepository
import com.onestackdev.katrina.data.WeatherApiRepository
import com.onestackdev.katrina.databinding.FragmentHomeBinding
import com.onestackdev.katrina.model.Hour
import com.onestackdev.katrina.model.WeatherAPIModel
import com.onestackdev.katrina.screens.activities.MainActivity
import com.onestackdev.katrina.utils.Constants.LOCATION_REQUEST_CODE
import com.onestackdev.katrina.utils.buildLocationCallback
import com.onestackdev.katrina.utils.buildLocationRequest
import com.onestackdev.katrina.utils.handler
import com.onestackdev.katrina.utils.returnImageWeather
import com.onestackdev.katrina.utils.setUpLocation
import com.onestackdev.katrina.utils.setUpRecyclerHorizontal
import com.onestackdev.katrina.utils.tempFormat
import com.onestackdev.katrina.viewmodel.TodayWeatherAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding

    @Inject
    lateinit var weatherApi: WeatherApiRepository

    @Inject
    lateinit var aqiApi: AqiApiRepository

    @Inject
    lateinit var todayAdapter: TodayWeatherAdapter

    //Location
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback

    private var runnable: Runnable? = null

    companion object {
        var isGpsOn = false
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(layoutInflater, container, false)

        checkGPS()

        turnOnGps()

        return binding.root
    }

    private fun checkGPS() {
        runnable = kotlinx.coroutines.Runnable {
            if (isGpsOn) {
                setupLocation()
                runnable = null
            }
            try {
                Handler(Looper.getMainLooper()).postDelayed(runnable!!, 200)
            } catch (e: Exception) {
                Log.i("ERROR", e.message.toString())
            }
        }
        try {
            Handler(Looper.getMainLooper()).postDelayed(runnable!!, 0)
        } catch (e: Exception) {
            Log.i("ERROR", e.message.toString())
        }
    }

    private fun setupLocation() {
        locationRequest = buildLocationRequest()

        locationRequest = buildLocationRequest()
        locationCallback = buildLocationCallback()
        fusedLocationProviderClient =
            setUpLocation(MainActivity.activity, locationRequest, locationCallback)

        CoroutineScope(Main).launch { getLocation() }

    }

    private fun turnOnGps() {

        val locationRequest = LocationRequest.Builder(PRIORITY_HIGH_ACCURACY, 3000)
            .setWaitForAccurateLocation(false)
            .setMinUpdateIntervalMillis(3000)
            .setMaxUpdateDelayMillis(10000)
            .build()

        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
        LocationServices.getSettingsClient(MainActivity.activity)
            .checkLocationSettings(builder.build())
            .addOnSuccessListener {
                val status = it.locationSettingsStates
                if (status!!.isLocationPresent) {
                    isGpsOn = true
                }
            }
            .addOnFailureListener {
                val statusCode = (it as ResolvableApiException).statusCode
                if (statusCode == LocationSettingsStatusCodes.RESOLUTION_REQUIRED) {
                    try {
                        it.startResolutionForResult(MainActivity.activity, LOCATION_REQUEST_CODE)
                    } catch (_: IntentSender.SendIntentException) {
                    }
                }
            }
            .addOnCanceledListener { }
    }

    private suspend fun getLocation() {
        delay(1000)
        if (ActivityCompat.checkSelfPermission(
                MainActivity.activity, Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                MainActivity.activity, Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        fusedLocationProviderClient.lastLocation.addOnSuccessListener {
            try {
                getWeather("${it.latitude}, ${it.longitude}")
                getAQI(it.latitude.toString(), it.longitude.toString())
            } catch (_: Exception) {
                CoroutineScope(Main).launch { getLocation() }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun getWeather(latLong: String) {
        CoroutineScope(Main).launch(handler) {
            //73.036479, -85.152025
            val response = weatherApi.getWeather(latLong)
            when (response.code()) {
                200 -> {
                    response.body().apply {

                        this?.forecast?.let {
                            setupRvWeatherToday(it.forecastday[0].hour, response.body())
                        }

                        binding.tvCity.text = "${this!!.location.name}, ${location.country}"
                        binding.tvDate.text = location.localtime
                        binding.tvTemperature.text = tempFormat(this.current.temp_c.toString())

                        binding.tvWind.text = current.wind_kph.toString() + " Km"
                        binding.tvHumidity.text = "%" + current.humidity.toString()
                        binding.status.text = current.condition.text

                        binding.tvMinTemp.text =
                            tempFormat(forecast.forecastday[0].day.maxtemp_c.toString())

                        Glide.with(MainActivity.activity).load(
                            returnImageWeather(
                                current.condition.code, current.is_day, MainActivity.activity
                            )
                        ).into(binding.imageWeatherStatus)
                    }
                }
            }
        }
    }

    private fun setupRvWeatherToday(hour: List<Hour>, weather: WeatherAPIModel?) {
        val selectHour = ArrayList<Hour>()

        for (items in hour) {
            if (items.time_epoch > weather?.location?.localtime_epoch!!) selectHour.add(items)
        }

        todayAdapter.differ.submitList(selectHour)

        binding.rvWeatherToday.apply {
            setUpRecyclerHorizontal(MainActivity.activity, this)
            adapter = todayAdapter
        }
    }

    @SuppressLint("SetTextI18n")
    private fun getAQI(lat: String, lon: String) {
        CoroutineScope(Main).launch(handler) {
            val response = aqiApi.getAQI(lat = lat, long = lon)
            when (response.code()) {
                200 -> {
                    response.body()?.data.apply {
                        binding.tvAQI.text = this?.current?.pollution?.aqius.toString()
                        binding.consAnimLoading.visibility = View.GONE
                        binding.consMain.visibility = View.VISIBLE

                        if (this?.current?.pollution?.aqius!! < 50) {

                            binding.tvStatusAQI.text = "Good"
                            binding.tvAQI.setTextColor(Color.parseColor("#00FF3A"))
                            binding.consAQI.setBackgroundResource(R.drawable.back_status_aqi_green)
                            binding.consStatus.setBackgroundResource(R.drawable.back_statusi_green)

                        } else if (this.current.pollution.aqius < 100) {

                            binding.tvStatusAQI.text = "Moderate"
                            binding.tvAQI.setTextColor(Color.parseColor("#AE9D00"))
                            binding.consAQI.setBackgroundResource(R.drawable.back_status_aqi_yellow)
                            binding.consStatus.setBackgroundResource(R.drawable.back_status_yellow)

                        } else if (this.current.pollution.aqius < 150) {

                            binding.tvStatusAQI.text = "Unhealthy for sensitive"
                            binding.tvAQI.setTextColor(Color.parseColor("#D16400"))
                            binding.consStatus.setBackgroundResource(R.drawable.back_status_orange)
                            binding.consAQI.setBackgroundResource(R.drawable.back_status_aqi_orange)

                        } else if (this.current.pollution.aqius < 200) {

                            binding.tvStatusAQI.text = "Unhealthy"
                            binding.tvAQI.setTextColor(Color.parseColor("#C50000"))
                            binding.consStatus.setBackgroundResource(R.drawable.back_status_red)
                            binding.consAQI.setBackgroundResource(R.drawable.back_status_aqi_red)

                        } else if (this.current.pollution.aqius < 250) {

                            binding.tvStatusAQI.text = "Very unhealthy"
                            binding.tvAQI.setTextColor(Color.parseColor("#C50000"))
                            binding.consStatus.setBackgroundResource(R.drawable.back_status_red)
                            binding.consAQI.setBackgroundResource(R.drawable.back_status_aqi_red)

                        } else if (this.current.pollution.aqius < 300) {

                            binding.tvStatusAQI.text = "Very unhealthy"
                            binding.tvAQI.setTextColor(Color.parseColor("#B200F0"))
                            binding.consStatus.setBackgroundResource(R.drawable.back_status_purpel)
                            binding.consAQI.setBackgroundResource(R.drawable.back_status_aqi_purpel)

                        } else if (this.current.pollution.aqius < 350) {

                            binding.tvStatusAQI.text = "Hazardous"
                            binding.tvAQI.setTextColor(Color.parseColor("#B200F0"))
                            binding.consStatus.setBackgroundResource(R.drawable.back_status_purpel)
                            binding.consAQI.setBackgroundResource(R.drawable.back_status_aqi_purpel)
                        }
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        runnable = null
    }
}