package com.onestackdev.katrina.screens.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.LocationRequest
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import com.onestackdev.katrina.data.AqiApiRepository
import com.onestackdev.katrina.data.WeatherApiRepository
import com.onestackdev.katrina.databinding.FragmentHomeBinding
import com.onestackdev.katrina.screens.activities.MainActivity
import com.onestackdev.katrina.utils.buildLocationRequest
import com.onestackdev.katrina.utils.checkLocationStatus
import com.onestackdev.katrina.utils.handler
import com.onestackdev.katrina.utils.turnOnLocation
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding

    private lateinit var locationRequest: LocationRequest

    private lateinit var locationManager: LocationManager

    @Inject
    lateinit var weatherApi: WeatherApiRepository

    @Inject
    lateinit var aqiApi: AqiApiRepository

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(layoutInflater, container, false)

        setupLocation()

        setupGetLocation()

        return binding.root
    }

    private fun setupGetLocation() {
        locationRequest = buildLocationRequest()
        accessFindLocation()
        if (!checkLocationStatus()) turnOnLocation(MainActivity.activity, locationRequest)
    }

    private fun accessFindLocation() {
        Dexter.withContext(MainActivity.activity)
            .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
            .withListener(object : PermissionListener {
                override fun onPermissionGranted(permissionGrantedResponse: PermissionGrantedResponse) {
                    if (ActivityCompat.checkSelfPermission(
                            MainActivity.activity, Manifest.permission.ACCESS_FINE_LOCATION
                        ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                            MainActivity.activity, Manifest.permission.ACCESS_COARSE_LOCATION
                        ) != PackageManager.PERMISSION_GRANTED
                    ) return

                    locationRequest = buildLocationRequest()

                    locationManager.requestLocationUpdates(
                        LocationManager.NETWORK_PROVIDER,
                        10000L,
                        10000f,
                        locationListener
                    )
                }

                override fun onPermissionDenied(permissionDeniedResponse: PermissionDeniedResponse) {
                }

                override fun onPermissionRationaleShouldBeShown(
                    permissionRequest: PermissionRequest,
                    permissionToken: PermissionToken
                ) {
                }
            }).check()
    }

    private fun setupLocation() {
        // Create persistent LocationManager reference
        locationManager =
            (MainActivity.activity.getSystemService(AppCompatActivity.LOCATION_SERVICE) as LocationManager?)!!
        if (ActivityCompat.checkSelfPermission(
                MainActivity.activity, Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                MainActivity.activity, Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        locationManager.requestLocationUpdates(
            LocationManager.NETWORK_PROVIDER,
            10000L,
            10000f,
            locationListener
        )
    }

    @SuppressLint("SetTextI18n")
    private fun getWeather(latLong: String) {
        CoroutineScope(Main).launch(handler) {
            val response = weatherApi.getWeather(latLong)
            when (response.code()) {
                200 -> {
                    response.body().apply {
                        binding.tvCity.text = "${this!!.location.region}, ${location.country}"
                        binding.tvDate.text = location.localtime
                        binding.tvTemperature.text = if (this.current.temp_c < 1.0) {
                            "-" + current.temp_c.toString().replace(".0", "")
                        } else current.temp_c.toString().replace(".0", "")

                        binding.tvWind.text = current.wind_kph.toString() + " Km"
                        binding.tvHumidity.text = "%" + current.humidity.toString()
                        binding.status.text = current.condition.text

                        binding.tvMinTemp.text =
                            forecast.forecastday[0].day.maxtemp_c.toString().replace(".1", "")

                       /* Glide.with(MainActivity.activity)
                            .load(
                                returnImageWeather(
                                    current.condition.code,
                                    current.is_day,
                                    MainActivity.activity
                                )
                            )
                            .into(binding.imageWeatherStatus)*/
                    }
                }
            }
        }
    }

    private fun getAQI(lat: String, lon: String) {
        CoroutineScope(Main).launch(handler) {
            val response = aqiApi.getAQI(lat = lat, long = lon)
            when (response.code()) {
                200 -> {
                    response.body()?.data.apply {
                        binding.tvAQI.text = this?.current?.pollution?.aqius.toString()
                    }
                }
            }
        }
    }

    //define the listener
    private val locationListener: LocationListener = LocationListener { location ->
        CoroutineScope(Main).launch {
            getWeather(location.latitude.toString() + "," + location.longitude.toString())
            getAQI(location.latitude.toString(), location.longitude.toString())
        }
    }
}