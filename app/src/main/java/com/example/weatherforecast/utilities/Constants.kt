package com.example.weatherforecast.utilities

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build

object Constants{

    var Location: String = ""

    //API KEY inserted in this part
    const val APP_ID: String = "6b304d8056c13a25309660d6a5521ae5"

    //Link to the Weather Map API
    const val BASE_URL: String = "http://api.openweathermap.org/data/"

    //Metric unit like Celsius and Fahrenheit
    const val METRIC_UNIT: String = "metric"

    const val PREFERENCE_NAME = "WeatherAppPreference"
    const val WEATHER_RESPONSE_DATA = "weather_response_data"

    //To check if there is an Internet Connection
    fun isNetworkAvailable(context: Context) : Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as
                ConnectivityManager

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork ?: return false
            val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false

            return when {
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
                else -> false
            }
        }
            else {
                val networkStat = connectivityManager.activeNetworkInfo
                return networkStat !=null && networkStat.isConnectedOrConnecting
            }
    }
}