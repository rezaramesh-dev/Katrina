package com.onestackdev.katrina.utils

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.graphics.drawable.Drawable
import android.location.LocationManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsResponse
import com.google.android.gms.location.LocationSettingsStatusCodes
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.Task
import com.onestackdev.katrina.R
import kotlinx.coroutines.CoroutineExceptionHandler

fun isOnline(context: Context): Boolean {
    val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val capabilities = connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
    if (capabilities != null) if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) return true
    else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) return true
    else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) return true
    showMessageToast(context, "")
    return false
}

val handler = CoroutineExceptionHandler { _, exception ->
    Log.e("Network", "Caught $exception")
}

fun showMessageToast(context: Context, message: String) {
    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
}

fun shareApp(activity: AppCompatActivity) {
    val intent = Intent(Intent.ACTION_SEND)
    val shareBody = ""
    intent.type = "text/plain"
    intent.putExtra(Intent.EXTRA_SUBJECT, "Invite Friends")
    intent.putExtra(Intent.EXTRA_TEXT, shareBody)
    activity.startActivity(Intent.createChooser(intent, "Invite Friends"))
}

fun setUpRecyclerVERTICAL(activity: AppCompatActivity?, rv: RecyclerView) {
    rv.layoutManager = LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)
    rv.setHasFixedSize(true)
    rv.itemAnimator = DefaultItemAnimator()
}

fun turnOnLocation(activity: AppCompatActivity, locationRequest: LocationRequest) {
    val requestCode = 1000
    val builder: LocationSettingsRequest.Builder = LocationSettingsRequest.Builder()
        .addLocationRequest(locationRequest)
    val result =
        LocationServices.getSettingsClient(activity).checkLocationSettings(builder.build())
    result.addOnCompleteListener { task: Task<LocationSettingsResponse?> ->
        try {
            task.getResult(ApiException::class.java)
        } catch (e: ApiException) {
            when (e.statusCode) {
                LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> try {
                    val resolvableApiException = e as ResolvableApiException
                    resolvableApiException.startResolutionForResult(activity, requestCode)
                } catch (sendIntentException: IntentSender.SendIntentException) {
                    sendIntentException.printStackTrace()
                } catch (ex: ClassCastException) {
                }

                LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> {}
            }
        }
    }
}

fun checkLocationStatus(): Boolean {
    //GPS
    val lm: LocationManager? = null
    var gpsEnabled = false
    var networkEnabled = false
    try {
        gpsEnabled = lm!!.isProviderEnabled(LocationManager.GPS_PROVIDER)
        networkEnabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    } catch (ex: Exception) {
    }
    return !(!gpsEnabled && !networkEnabled)
}

fun buildLocationRequest(): LocationRequest {
    val lo = LocationRequest.Builder(80000)
    lo.setIntervalMillis(80000)
    lo.setMinUpdateDistanceMeters(20f)
    lo.setMinUpdateDistanceMeters(60000f)
    lo.setPriority(Priority.PRIORITY_HIGH_ACCURACY)
    lo.setMaxUpdateDelayMillis(100)
    return lo.build()
}


@SuppressLint("UseCompatLoadingForDrawables")
fun returnImageWeather(code: Int, day: Int, context: Context): Drawable? {
    var image: Drawable? = null
    when (code) {
        1000 -> image =
            if (day == 1) context.getDrawable(R.drawable.sunny) else context.getDrawable(R.drawable.moon_clear)

        1003 -> image =
            if (day == 1) context.getDrawable(R.drawable.sunny) else context.getDrawable(R.drawable.moon_partly_cloudy)

        1006 -> image =
            if (day == 1) context.getDrawable(R.drawable.cloudy) else context.getDrawable(R.drawable.cloudy)

        1009 -> image = context.getDrawable(R.drawable.cloudy)

        1030 -> image = context.getDrawable(R.drawable.cloudy)

        1063 -> image =
            if (day == 1) context.getDrawable(R.drawable.sunny) else context.getDrawable(R.drawable.moon_cloudy)

        1066 -> image =
            if (day == 1) context.getDrawable(R.drawable.sunny) else context.getDrawable(R.drawable.nigh_train)

        1069 -> image = context.getDrawable(R.drawable.rain)

        1072 -> image = context.getDrawable(R.drawable.rain)

        1087 -> image =
            if (day == 1) context.getDrawable(R.drawable.thunder_suny) else context.getDrawable(R.drawable.thunder_moon)

        1114 -> image = context.getDrawable(R.drawable.snow_rain)

        1117 -> image = context.getDrawable(R.drawable.snow_rain)

        1135 -> image = context.getDrawable(R.drawable.cloudy)

        1147 -> image = context.getDrawable(R.drawable.cloudy)

        1150 -> image = context.getDrawable(R.drawable.rain)

        1153 -> image = context.getDrawable(R.drawable.rain)

        1168 -> image = context.getDrawable(R.drawable.rain)

        1171 -> image = context.getDrawable(R.drawable.rain)

        1180 -> image =
            if (day == 1) context.getDrawable(R.drawable.sunny) else context.getDrawable(R.drawable.moon_cloudy)

        1183 -> image = context.getDrawable(R.drawable.rain)

        1186 -> image =
            if (day == 1) context.getDrawable(R.drawable.sunny) else context.getDrawable(R.drawable.moon_cloudy)

        1189 -> image = context.getDrawable(R.drawable.rain)

        1192 -> image =
            if (day == 1) context.getDrawable(R.drawable.sunny) else context.getDrawable(R.drawable.moon_cloudy)

        1195 -> image = context.getDrawable(R.drawable.rain)

        1198 -> image = context.getDrawable(R.drawable.rain)

        1201 -> image = context.getDrawable(R.drawable.rain)

        1204 -> image = context.getDrawable(R.drawable.rain)

        1207 -> image = context.getDrawable(R.drawable.rain)

        1210 -> image =
            if (day == 1) context.getDrawable(R.drawable.sunny) else context.getDrawable(R.drawable.moon_cloudy)

        1213 -> image = context.getDrawable(R.drawable.snow)

        1216 -> image =
            if (day == 1) context.getDrawable(R.drawable.sunny) else context.getDrawable(R.drawable.moon_cloudy)

        1219 -> image = context.getDrawable(R.drawable.sunny)

        1222 -> image =
            if (day == 1) context.getDrawable(R.drawable.sunny) else context.getDrawable(R.drawable.moon_cloudy)

        1225 -> image = context.getDrawable(R.drawable.snow)

        1237 -> image = context.getDrawable(R.drawable.snow)

        1240 -> image =
            if (day == 1) context.getDrawable(R.drawable.sunny) else context.getDrawable(R.drawable.moon_cloudy)

        1243 -> image =
            if (day == 1) context.getDrawable(R.drawable.sunny) else context.getDrawable(R.drawable.moon_cloudy)

        1246 -> image =
            if (day == 1) context.getDrawable(R.drawable.sunny) else context.getDrawable(R.drawable.moon_cloudy)

        1249 -> image =
            if (day == 1) context.getDrawable(R.drawable.sunny) else context.getDrawable(R.drawable.moon_cloudy)

        1252 -> image =
            if (day == 1) context.getDrawable(R.drawable.sunny) else context.getDrawable(R.drawable.moon_cloudy)

        1255 -> image =
            if (day == 1) context.getDrawable(R.drawable.sunny) else context.getDrawable(R.drawable.moon_cloudy)

        1258 -> image =
            if (day == 1) context.getDrawable(R.drawable.sunny) else context.getDrawable(R.drawable.moon_cloudy)

        1261 -> image =
            if (day == 1) context.getDrawable(R.drawable.sunny) else context.getDrawable(R.drawable.moon_cloudy)

        1264 -> image =
            if (day == 1) context.getDrawable(R.drawable.sunny) else context.getDrawable(R.drawable.moon_cloudy)

        1273 -> image =
            if (day == 1) context.getDrawable(R.drawable.thunder_suny) else context.getDrawable(R.drawable.thunder_moon)

        1276 -> image = context.getDrawable(R.drawable.thunder)

        1279 -> image = context.getDrawable(R.drawable.heavyrain_snow)

        1282 -> image = context.getDrawable(R.drawable.heavyrain_snow)
    }
    return image
}

fun tempFormat(temp: String): String {
    return temp.substring(0, temp.indexOf("."))
}
