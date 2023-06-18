package com.onestackdev.katrina.screens.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.content.IntentSender
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsStatusCodes
import com.google.android.gms.location.Priority
import com.onestackdev.katrina.data.WeatherApiRepository
import com.onestackdev.katrina.databinding.FragmentWeekBinding
import com.onestackdev.katrina.model.Hour
import com.onestackdev.katrina.screens.activities.MainActivity
import com.onestackdev.katrina.utils.Constants
import com.onestackdev.katrina.utils.buildLocationCallback
import com.onestackdev.katrina.utils.buildLocationRequest
import com.onestackdev.katrina.utils.handler
import com.onestackdev.katrina.utils.setUpLocation
import com.onestackdev.katrina.utils.setUpRecyclerHorizontal
import com.onestackdev.katrina.utils.setUpRecyclerVERTICAL
import com.onestackdev.katrina.viewmodel.WeekWeatherAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@AndroidEntryPoint
class WeekFragment : Fragment() {

    private lateinit var binding: FragmentWeekBinding

    @Inject
    lateinit var weatherApi: WeatherApiRepository

    @Inject
    lateinit var weatherAdapter: WeekWeatherAdapter

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
        binding = FragmentWeekBinding.inflate(layoutInflater, container, false)

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

        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 3000)
            .setWaitForAccurateLocation(false)
            .setMinUpdateIntervalMillis(3000)
            .setMaxUpdateDelayMillis(10000)
            .build()

        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
        LocationServices.getSettingsClient(MainActivity.activity)
            .checkLocationSettings(builder.build())
            .addOnSuccessListener {
                val status = it.locationSettingsStates
                if (status!!.isLocationPresent) isGpsOn = true

            }
            .addOnFailureListener {
                val statusCode = (it as ResolvableApiException).statusCode
                if (statusCode == LocationSettingsStatusCodes.RESOLUTION_REQUIRED) {
                    try {
                        it.startResolutionForResult(
                            MainActivity.activity,
                            Constants.LOCATION_REQUEST_CODE
                        )
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
                getWeatherTenDays("${it.latitude}, ${it.longitude}")
            } catch (_: Exception) {
                CoroutineScope(Main).launch { getLocation() }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun getWeatherTenDays(latLong: String) {
        CoroutineScope(Main).launch(handler) {
            val response = weatherApi.getWeatherTenDays(latLong)
            when (response.code()) {
                200 -> {
                    response.body().apply {
                        this?.forecast?.let { setupRvWeatherWeek(it.forecastday[0].hour) }
                    }
                }
            }
        }
    }

    private fun setupRvWeatherWeek(hours: List<Hour>) {
        val selectHour = ArrayList<Hour>()

        val format = SimpleDateFormat("yyyy-MM-dd HH:", Locale.ROOT)
        val hour = format.format(Date())

        /*val calendar= Calendar.getInstance()
        val hourOfDay: Int = calendar.get(Calendar.HOUR_OF_DAY)*/

        for (items in hours) {
            if (items.time == "${hour}00")selectHour.add(items)
        }

        weatherAdapter.differ.submitList(selectHour)

        binding.rvWeatherWeek.apply {
            setUpRecyclerVERTICAL(MainActivity.activity, this)
            adapter = weatherAdapter
        }
    }
}