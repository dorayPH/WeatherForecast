package com.example.weatherforecast.Activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.weatherforecast.R

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.SharedPreferences
import android.icu.text.SimpleDateFormat
import android.icu.util.TimeZone
import android.location.Location
import android.os.Build
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.databinding.DataBindingUtil
import com.example.weatherforecast.databinding.ActivityFiveDaysForecastBinding
import com.example.weatherforecast.network.WeatherService
import com.example.weatherforecast.utilities.Constants
import com.google.android.gms.location.*
import com.google.gson.Gson
import eu.tutorials.futureweatherforecast.futureForecastModel.FutureWeather
import retrofit.*
import java.util.*




class fiveDaysForecast : AppCompatActivity() {
    private lateinit var mFusedLocationClient: FusedLocationProviderClient
    private lateinit var mSharedPreferences: SharedPreferences
    private var mProgressDialog: Dialog? = null
    private var mLatitude: Double = 0.0
    private var mLongitude: Double = 0.0

    private lateinit var bindind: ActivityFiveDaysForecastBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //setContentView(R.layout.activity_five_days_forecast)
        bindind = DataBindingUtil.setContentView (this, R.layout.activity_five_days_forecast)

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        mSharedPreferences = getSharedPreferences(Constants.PREFERENCE_NAME, Context.MODE_PRIVATE)

        requestLocationData()
    }

    private fun getFutureWeatherDetails() {
        if (Constants.isNetworkAvailable(this@fiveDaysForecast )) {
            val retrofit: Retrofit = Retrofit.Builder()
                .baseUrl(Constants.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
            val service: WeatherService =
                retrofit.create<WeatherService>(WeatherService::class.java)
            val listCall: Call<FutureWeather> = service.getFutureWeather(
                mLatitude, mLongitude,  Constants.METRIC_UNIT,Constants.Location,Constants.APP_ID
            )

            showCustomProgressDialog()

            listCall.enqueue(object : Callback<FutureWeather> {
                @RequiresApi(Build.VERSION_CODES.N)
                @SuppressLint("SetTextI18n")
                override fun onResponse(
                    response: Response<FutureWeather>,
                    retrofit: Retrofit
                ) {

                    if (response.isSuccess) {
                        hideProgressDialog()
                        /** The de-serialized response body of a successful response. */
                        val weatherList: FutureWeather = response.body()
                        Log.i("Response Result", "$weatherList")
                        val weatherResponseJsonString = Gson().toJson(weatherList)
                        val editor = mSharedPreferences.edit()
                        editor.putString(Constants.WEATHER_RESPONSE_DATA, weatherResponseJsonString)
                        editor.apply()
                        setupUI()
                    } else {
                        val sc = response.code()
                        hideProgressDialog()
                        when (sc) {
                            400 -> {
                                Log.e("Error 400", "Bad Request")
                            }
                            404 -> {
                                Log.e("Error 404", "Not Found")
                            }
                            else -> {
                                Log.e("Error", "Generic Error")
                            }
                        }
                    }
                }

                override fun onFailure(t: Throwable) {
                    Log.e("Errorrrrr", t.message.toString())
                    hideProgressDialog()
                }
            })
        } else {
            Toast.makeText(
                this@fiveDaysForecast,
                "No internet connection available.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
    @RequiresApi(Build.VERSION_CODES.N)
    @SuppressLint("SetTextI18n")
    private fun setupUI() {
        val weatherResponseJsonString =
            mSharedPreferences.getString(Constants.WEATHER_RESPONSE_DATA, "")

        if (!weatherResponseJsonString.isNullOrEmpty()) {

            val weatherList =
                Gson().fromJson(
                    weatherResponseJsonString,
                    eu.tutorials.futureweatherforecast.futureForecastModel.FutureWeather::class.java
                )
            // For loop to get the required data. And all are populated in the UI.
            for (z in weatherList.list.indices) {

                bindind.fiveDaysCityname.text = weatherList.city.name
                bindind.dayOneTemp.text =
                    weatherList.list[0].main.temp.toString() + getUnit(application.resources.configuration.locales.toString())
                bindind.dayTwiTemp.text =
                    weatherList.list[8].main.temp.toString() + getUnit(application.resources.configuration.locales.toString())
                bindind.dayThreeTemp.text =
                    weatherList.list[16].main.temp.toString() + getUnit(application.resources.configuration.locales.toString())
                bindind.dayFourTemp.text =
                    weatherList.list[24].main.temp.toString() + getUnit(application.resources.configuration.locales.toString())
                bindind.dayFiveTemp.text =
                    weatherList.list[32].main.temp.toString() + getUnit(application.resources.configuration.locales.toString())

                bindind.dayOneDate.text = weatherList.list[0].dt_txt
                bindind.dayTwoDate.text = weatherList.list[8].dt_txt
                bindind.dayThreeDate.text = weatherList.list[16].dt_txt
                bindind.dayFourDate.text = weatherList.list[24].dt_txt
                bindind.dayFiveDate.text = weatherList.list[32].dt_txt

                bindind.dayOneForecast.text = weatherList.list[0].weather[0].description
                bindind.dayTwoForecast.text = weatherList.list[8].weather[0].description
                bindind.dayThreeForecast.text = weatherList.list[16].weather[0].description
                bindind.dayFourForecast.text = weatherList.list[24].weather[0].description
                bindind.dayFiveForecast.text = weatherList.list[32].weather[0].description


                when (weatherList.list[0].weather[0].icon) {
                    "01d" -> bindind.dayOneIcon.setImageResource(R.drawable.sunny)
                    "02d" -> bindind.dayOneIcon.setImageResource(R.drawable.cloud)
                    "03d" -> bindind.dayOneIcon.setImageResource(R.drawable.cloud)
                    "04d" -> bindind.dayOneIcon.setImageResource(R.drawable.cloud)
                    "04n" -> bindind.dayOneIcon.setImageResource(R.drawable.cloud)
                    "10d" -> bindind.dayOneIcon.setImageResource(R.drawable.rain)
                    "11d" -> bindind.dayOneIcon.setImageResource(R.drawable.storm)
                    "13d" -> bindind.dayOneIcon.setImageResource(R.drawable.snowflake)
                    "01n" -> bindind.dayOneIcon.setImageResource(R.drawable.cloud)
                    "02n" -> bindind.dayOneIcon.setImageResource(R.drawable.cloud)
                    "03n" -> bindind.dayOneIcon.setImageResource(R.drawable.cloud)
                    "10n" -> bindind.dayOneIcon.setImageResource(R.drawable.cloud)
                    "11n" -> bindind.dayOneIcon.setImageResource(R.drawable.rain)
                    "13n" -> bindind.dayOneIcon.setImageResource(R.drawable.snowflake)
                }
                when (weatherList.list[8].weather[0].icon) {
                    "01d" -> bindind.dayTwoIcon.setImageResource(R.drawable.sunny)
                    "02d" -> bindind.dayTwoIcon.setImageResource(R.drawable.cloud)
                    "03d" -> bindind.dayTwoIcon.setImageResource(R.drawable.cloud)
                    "04d" -> bindind.dayTwoIcon.setImageResource(R.drawable.cloud)
                    "04n" -> bindind.dayTwoIcon.setImageResource(R.drawable.cloud)
                    "10d" -> bindind.dayTwoIcon.setImageResource(R.drawable.rain)
                    "11d" -> bindind.dayTwoIcon.setImageResource(R.drawable.storm)
                    "13d" -> bindind.dayTwoIcon.setImageResource(R.drawable.snowflake)
                    "01n" -> bindind.dayTwoIcon.setImageResource(R.drawable.cloud)
                    "02n" -> bindind.dayTwoIcon.setImageResource(R.drawable.cloud)
                    "03n" -> bindind.dayTwoIcon.setImageResource(R.drawable.cloud)
                    "10n" -> bindind.dayTwoIcon.setImageResource(R.drawable.cloud)
                    "11n" -> bindind.dayTwoIcon.setImageResource(R.drawable.rain)
                    "13n" -> bindind.dayTwoIcon.setImageResource(R.drawable.snowflake)
                }
                when (weatherList.list[16].weather[0].icon) {
                    "01d" -> bindind.dayThreeIcon.setImageResource(R.drawable.sunny)
                    "02d" -> bindind.dayThreeIcon.setImageResource(R.drawable.cloud)
                    "03d" -> bindind.dayThreeIcon.setImageResource(R.drawable.cloud)
                    "04d" -> bindind.dayThreeIcon.setImageResource(R.drawable.cloud)
                    "04n" -> bindind.dayThreeIcon.setImageResource(R.drawable.cloud)
                    "10d" -> bindind.dayThreeIcon.setImageResource(R.drawable.rain)
                    "11d" -> bindind.dayThreeIcon.setImageResource(R.drawable.storm)
                    "13d" -> bindind.dayThreeIcon.setImageResource(R.drawable.snowflake)
                    "01n" -> bindind.dayThreeIcon.setImageResource(R.drawable.cloud)
                    "02n" -> bindind.dayThreeIcon.setImageResource(R.drawable.cloud)
                    "03n" -> bindind.dayThreeIcon.setImageResource(R.drawable.cloud)
                    "10n" -> bindind.dayThreeIcon.setImageResource(R.drawable.cloud)
                    "11n" -> bindind.dayThreeIcon.setImageResource(R.drawable.rain)
                    "13n" -> bindind.dayThreeIcon.setImageResource(R.drawable.snowflake)
                }
                when (weatherList.list[24].weather[0].icon) {
                    "01d" -> bindind.dayFourIcon.setImageResource(R.drawable.sunny)
                    "02d" -> bindind.dayFourIcon.setImageResource(R.drawable.cloud)
                    "03d" -> bindind.dayFourIcon.setImageResource(R.drawable.cloud)
                    "04d" -> bindind.dayFourIcon.setImageResource(R.drawable.cloud)
                    "04n" -> bindind.dayFourIcon.setImageResource(R.drawable.cloud)
                    "10d" -> bindind.dayFourIcon.setImageResource(R.drawable.rain)
                    "11d" -> bindind.dayFourIcon.setImageResource(R.drawable.storm)
                    "13d" -> bindind.dayFourIcon.setImageResource(R.drawable.snowflake)
                    "01n" -> bindind.dayFourIcon.setImageResource(R.drawable.cloud)
                    "02n" -> bindind.dayFourIcon.setImageResource(R.drawable.cloud)
                    "03n" -> bindind.dayFourIcon.setImageResource(R.drawable.cloud)
                    "10n" -> bindind.dayFourIcon.setImageResource(R.drawable.cloud)
                    "11n" -> bindind.dayFourIcon.setImageResource(R.drawable.rain)
                    "13n" -> bindind.dayFourIcon.setImageResource(R.drawable.snowflake)
                }
                when (weatherList.list[32].weather[0].icon) {
                    "01d" -> bindind.dayFiveIcon.setImageResource(R.drawable.sunny)
                    "02d" -> bindind.dayFiveIcon.setImageResource(R.drawable.cloud)
                    "03d" -> bindind.dayFiveIcon.setImageResource(R.drawable.cloud)
                    "04d" -> bindind.dayFiveIcon.setImageResource(R.drawable.cloud)
                    "04n" -> bindind.dayFiveIcon.setImageResource(R.drawable.cloud)
                    "10d" -> bindind.dayFiveIcon.setImageResource(R.drawable.rain)
                    "11d" -> bindind.dayFiveIcon.setImageResource(R.drawable.storm)
                    "13d" -> bindind.dayFiveIcon.setImageResource(R.drawable.snowflake)
                    "01n" -> bindind.dayFiveIcon.setImageResource(R.drawable.cloud)
                    "02n" -> bindind.dayFiveIcon.setImageResource(R.drawable.cloud)
                    "03n" -> bindind.dayFiveIcon.setImageResource(R.drawable.cloud)
                    "10n" -> bindind.dayFiveIcon.setImageResource(R.drawable.cloud)
                    "11n" -> bindind.dayFiveIcon.setImageResource(R.drawable.rain)
                    "13n" -> bindind.dayFiveIcon.setImageResource(R.drawable.snowflake)
                }
            }
        }

    }

    //Function is used to get the temperature unit value.

    private fun getUnit(value: String): String? {
        Log.i("unitttttt", value)
        var value = "°C"
        if ("US" == value || "LR" == value || "MM" == value) {
            value = "°F"
        }
        return value
    }


    //The function is used to get the formatted time based on the Format and the LOCALE we pass to it.

    @RequiresApi(Build.VERSION_CODES.N)
    private fun unixTime(timex: Long): String? {
        val date = Date(timex * 1000L)
        @SuppressLint("SimpleDateFormat") val sdf =
            SimpleDateFormat("HH:mm", Locale.CHINA)
        sdf.timeZone = TimeZone.getDefault()
        return sdf.format(date)
    }

    private fun showCustomProgressDialog() {
        mProgressDialog = Dialog(this)
        mProgressDialog!!.show()
    }

    private fun hideProgressDialog() {
        if (mProgressDialog != null) {
            mProgressDialog!!.dismiss()
        }
    }

    private val mLocationCallback = object : LocationCallback() {

        override fun onLocationResult(locationResult: LocationResult) {
            val mLastLocation: Location? = locationResult.lastLocation

            mLatitude = mLastLocation!!.latitude
            Log.e("Current Latitude", "$mLatitude")
            mLongitude = mLastLocation.longitude
            Log.e("Current Longitude", "$mLongitude")

            getFutureWeatherDetails()
        }
    }

    @SuppressLint("MissingPermission")
    private fun requestLocationData() {

        val mLocationRequest = LocationRequest()
        mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        mFusedLocationClient.requestLocationUpdates(
            mLocationRequest, mLocationCallback,
            Looper.myLooper()
        )
    }

}
