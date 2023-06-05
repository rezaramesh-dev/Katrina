package com.onestackdev.katrina.screens.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.bumptech.glide.Glide
import com.google.android.gms.location.LocationRequest
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import com.onestackdev.katrina.R
import com.onestackdev.katrina.data.AqiApiRepository
import com.onestackdev.katrina.data.WeatherApiRepository
import com.onestackdev.katrina.databinding.FragmentHomeBinding
import com.onestackdev.katrina.model.Hour
import com.onestackdev.katrina.model.WeatherAPIModel
import com.onestackdev.katrina.screens.activities.MainActivity
import com.onestackdev.katrina.utils.buildLocationRequest
import com.onestackdev.katrina.utils.checkLocationStatus
import com.onestackdev.katrina.utils.handler
import com.onestackdev.katrina.utils.returnImageWeather
import com.onestackdev.katrina.utils.setUpRecyclerHorizontal
import com.onestackdev.katrina.utils.setUpRecyclerVERTICAL
import com.onestackdev.katrina.utils.tempFormat
import com.onestackdev.katrina.utils.turnOnLocation
import com.onestackdev.katrina.viewmodel.TodayWeatherAdapter
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

    @Inject
    lateinit var todayAdapter: TodayWeatherAdapter

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
            //73.036479, -85.152025
            val response = weatherApi.getWeather(latLong)
            when (response.code()) {
                200 -> {
                    response.body().apply {

                        this?.forecast?.let {
                            setupRvWeatherToday(
                                it.forecastday[0].hour,
                                response.body()
                            )
                        }

                        binding.tvCity.text = "${this!!.location.name}, ${location.country}"
                        binding.tvDate.text = location.localtime
                        binding.tvTemperature.text = tempFormat(this.current.temp_c.toString())

                        binding.tvWind.text = current.wind_kph.toString() + " Km"
                        binding.tvHumidity.text = "%" + current.humidity.toString()
                        binding.status.text = current.condition.text

                        binding.tvMinTemp.text =
                            tempFormat(forecast.forecastday[0].day.maxtemp_c.toString())

                        Glide.with(MainActivity.activity)
                            .load(
                                returnImageWeather(
                                    current.condition.code,
                                    current.is_day,
                                    MainActivity.activity
                                )
                            )
                            .into(binding.imageWeatherStatus)
                    }
                }
            }
        }
    }

    private fun setupRvWeatherToday(hour: List<Hour>, weather: WeatherAPIModel?) {

        val selectHour = ArrayList<Hour>()

        for (items in hour) {
            if (items.time_epoch > weather?.location?.localtime_epoch!!) {
                selectHour.add(items)
            }
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

    //define the listener
    private val locationListener: LocationListener = LocationListener { location ->
        CoroutineScope(Main).launch {
            getWeather(location.latitude.toString() + "," + location.longitude.toString())
            getAQI(location.latitude.toString(), location.longitude.toString())
        }
    }
}