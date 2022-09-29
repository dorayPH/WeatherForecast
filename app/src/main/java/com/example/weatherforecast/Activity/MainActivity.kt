package com.example.weatherforecast.Activity

import android.Manifest
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.*
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Switch
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import com.example.weatherforecast.utilities.Constants
import com.example.weatherforecast.R
import com.example.weatherforecast.databinding.ActivityMainBinding
import com.example.weatherforecast.CurrentForecastModel.WeatherResponse
import com.example.weatherforecast.network.WeatherService
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.gson.Gson
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import retrofit.*
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    //Getting the Location of the device
    private lateinit var mFusedLocationClient: FusedLocationProviderClient

    //Data Binding part
    private lateinit var bindind: ActivityMainBinding

    private lateinit var modeSwitch : Switch


        private var mProgressDialog: Dialog? = null
        private var mLatitude: Double = 0.0
        private var mLongitude: Double = 0.0
        private lateinit var mSharedPreferences : SharedPreferences

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)

        //setContentView(R.layout.activity_main)
        bindind = DataBindingUtil.setContentView(this, R.layout.activity_main)

        bindind.searchLocationButton.setOnClickListener{
            var searchLocation: String = bindind.searchLocation.text.toString()
            Constants.Location = searchLocation
            if (searchLocation == "") {
                getLocationWeatherDetails()
            } else {
                getCurrentLocation()
            }
        }

        bindind.fiveDays.setOnClickListener{
            val intent = Intent(this@MainActivity, fiveDaysForecast::class.java)
            startActivity(intent)
        }

//        bindind.manageCity.setOnClickListener{
//            val intent = Intent(this@MainActivity, ManageCities::class.java)
//            startActivity(intent)
//        }


        modeSwitch = bindind.darkTheme as Switch
        modeSwitch.setOnClickListener{
            if (modeSwitch.isChecked) {
                bindind.MainBackground.setBackgroundResource(R.drawable.night)
                Toast.makeText(this, "NIGHT MODE IS TURNED ON", Toast.LENGTH_LONG).show()
            } else {
                bindind.MainBackground.setBackgroundResource(R.drawable.morning)
                Toast.makeText(this, "NIGHT MODE IS TURNED OFF.", Toast.LENGTH_LONG).show()
            }
        }

        //Late initialization of getting the location of user
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        mSharedPreferences = getSharedPreferences(Constants.PREFERENCE_NAME, Context.MODE_PRIVATE)

        setupUI()

        //This part shows up when the GPS of the device is turned OFF.
        if (!isLocationEnabled()){
            Toast.makeText(
                this,
                "Your location is not detected, please turn ON your location.",
                Toast.LENGTH_SHORT
            ).show()

            //Allow to make changes if the GPS is Turned Off.
            val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            startActivity(intent)
        }
        //This part ask users to grant a Location Access.
        else{
            Dexter.withActivity(this)
                .withPermissions(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
                .withListener(object : MultiplePermissionsListener {
                    override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                        if (report!!.areAllPermissionsGranted()) {

                            requestLocationData()

                        }

                        if (report.isAnyPermissionPermanentlyDenied) {
                            Toast.makeText(
                                this@MainActivity,
                                "YOU HAVE DENIED PERMISSION. TRY AGAIN!",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }

                    override fun onPermissionRationaleShouldBeShown(
                        permissions: MutableList<PermissionRequest>?,
                        token: PermissionToken?
                    ) {
                        showRationalDialogForPermissions()
                    }
                }).onSameThread()
                .check()
        }
    }

    @Suppress("DEPRECATION")
    @SuppressLint ("MissingPermission")
    private fun requestLocationData() {

        val mLocationRequest = com.google.android.gms.location.LocationRequest()
        mLocationRequest.priority = com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY

        mFusedLocationClient.requestLocationUpdates(
            mLocationRequest, mLocationCallback,
            Looper.myLooper()
        )
    }
    //Determining Location based on the Latitude and Longitude
    private val mLocationCallback = object : LocationCallback() {

        override fun onLocationResult(locationResult: LocationResult) {
            val mLastLocation: Location? = locationResult.lastLocation

            mLatitude = mLastLocation!!.latitude
            Log.e("Current Latitude", "$mLatitude")

            mLongitude = mLastLocation.longitude
            Log.e("Current Longitude", "$mLongitude")

            getLocationWeatherDetails()
        }
    }

    //Internet Connection Status
    private fun getLocationWeatherDetails(){
        if(Constants.isNetworkAvailable(this)){

            val retrofit : Retrofit = Retrofit.Builder()
                .baseUrl(Constants.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            val service : WeatherService =
                retrofit.create<WeatherService>(WeatherService::class.java)

            val listCall : Call<WeatherResponse> = service.getWeather(
                mLatitude, mLongitude, Constants.METRIC_UNIT, Constants.APP_ID
            )

            //Part where the progress dialog shows in the screen
            showCustomProgressDialog()

            listCall.enqueue(object : Callback<WeatherResponse> {
                @SuppressLint("SetTextI18n")

                @RequiresApi(Build.VERSION_CODES.N)
                override fun onResponse(response: Response<WeatherResponse>,
                                        retrofit: Retrofit) {
                    if(response.isSuccess){

                        //Part where to hide the progress dialog in the screen
                        hideProgressDialog()

                        val weatherList: WeatherResponse = response.body()
                        Log.i("Response Result", "$weatherList")

                        val weatherResponseJsonString = Gson().toJson(weatherList)
                        val editor = mSharedPreferences.edit()
                        editor.putString(Constants.WEATHER_RESPONSE_DATA, weatherResponseJsonString)
                        editor.apply()

                        setupUI()

                    } else {
                        val rc = response.code()

                        hideProgressDialog()
                        when (rc) {

                            400 -> {
                                Log.e("Error 400", "Bad Connection")
                            }
                            404 -> {
                                Log.e("Error 404", "Not Found")
                            }else -> {
                                Log.e("Error", "Generic Error")
                            }
                        }
                    }
                }

                override fun onFailure(t: Throwable?) {
                    Log.e("SUPER ERROR", t!!.message.toString())
                }
            })

        } else {
            Toast.makeText(
                this@MainActivity,
                "You don't have Internet Connection.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun getCurrentLocation(){
        if(Constants.isNetworkAvailable(this)){

            val retrofit : Retrofit = Retrofit.Builder()
                .baseUrl(Constants.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            val service : WeatherService =
                retrofit.create<WeatherService>(WeatherService::class.java)

            val listCall : Call<WeatherResponse> = service.getLocationWeather(
                mLatitude, mLongitude, Constants.METRIC_UNIT, Constants.Location, Constants.APP_ID
            )

            //Part where the progress dialog shows in the screen
            showCustomProgressDialog()

            listCall.enqueue(object : Callback<WeatherResponse> {
                @SuppressLint("SetTextI18n")

                @RequiresApi(Build.VERSION_CODES.N)
                override fun onResponse(response: Response<WeatherResponse>,
                                        retrofit: Retrofit) {
                    if(response.isSuccess){

                        //Part where to hide the progress dialog in the screen
                        hideProgressDialog()

                        val weatherList: WeatherResponse = response.body()
                        Log.i("Response Result", "$weatherList")

                        val weatherResponseJsonString = Gson().toJson(weatherList)
                        val editor = mSharedPreferences.edit()
                        editor.putString(Constants.WEATHER_RESPONSE_DATA, weatherResponseJsonString)
                        editor.apply()

                        setupUI()

                    } else {
                        val rc = response.code()

                        hideProgressDialog()
                        when (rc) {
                            400 -> {
                                Log.e("Error 400", "Bad Connection")
                            }
                            404 -> {
                                Log.e("Error 404", "Not Found")
                                Toast.makeText(
                                    this@MainActivity,
                                    "Location Not Detected",
                                    Toast.LENGTH_LONG
                                ).show()

                                bindind.searchLocation.setText("")

                            }else -> {
                            Log.e("Error", "Generic Error")
                        }
                        }
                    }
                }

                override fun onFailure(t: Throwable?) {
                    Log.e("SUPER ERROR", t!!.message.toString())
                }
            })

        } else {
            Toast.makeText(
                this@MainActivity,
                "You don't have Internet Connection.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun showRationalDialogForPermissions() {
        AlertDialog.Builder(this)
            .setMessage("You don't have a location permission. Kindly go to SETTINGS to enable your location.")
            .setPositiveButton(
                "Settings"
            ) { _, _ ->
                try {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    val  uri = Uri.fromParts("package", packageName, null)
                    intent.data = uri
                    startActivity(intent)
                } catch (e: ActivityNotFoundException) {
                    e.printStackTrace()
                }
            }
            .setNegativeButton("Cancel") { dialog,
                                                _ ->
                dialog.dismiss()
            }.show()
    }

    private fun isLocationEnabled(): Boolean {

        //This part provides access to the LOCATION where you are.
        val locationManager: LocationManager =
            getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    //Start the screen with a LOADING dialog to wait for its progress
    private fun showCustomProgressDialog(){
        mProgressDialog = Dialog(this)
        mProgressDialog!!.setContentView(R.layout.dialog_custom_progress)
        mProgressDialog!!.show()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId){
           R.id.action_refresh ->{
               true
           }else -> super.onOptionsItemSelected(item)
        }
        return super.onOptionsItemSelected(item)
    }


    //Closes the Process Dialog in the screen
    private fun hideProgressDialog(){
        if(mProgressDialog != null) {
            mProgressDialog!!.dismiss()
        }
    }

    //function gathered in the OpenWeatherMap API
    @SuppressLint("SetTextI18n")
    @RequiresApi(Build.VERSION_CODES.N)
    private fun setupUI() {

        val weatherResponseJsonString =
            mSharedPreferences.getString(Constants.WEATHER_RESPONSE_DATA, "")

        if (!weatherResponseJsonString.isNullOrEmpty()) {

            val weatherList =
                Gson().fromJson(weatherResponseJsonString, WeatherResponse::class.java)

            for (i in weatherList.weather.indices) {
                Log.i("Weather Name", weatherList.weather[i].main)

                bindind.tvMain.text = weatherList.weather[i].main
                bindind.tvMainDescription.text = weatherList.weather[i].description
                bindind.tvTemp.text = weatherList.main.temp.toString() + getUnit(application.resources.configuration.locales.toString())
                bindind.tvTemp2.text = weatherList.main.temp.toString() + getUnit(application.resources.configuration.locales.toString())
                bindind.tvHumidity.text = weatherList.main.humidity.toString() + " per cent "
                bindind.tvMin.text = weatherList.main.tempMin.toString() + " min "
                bindind.tvMax.text = weatherList.main.tempMax.toString() + " max "
                bindind.tvSpeed.text = weatherList.wind.speed.toString()
                bindind.tvName.text = weatherList.name
                bindind.tvCountry.text = weatherList.sys.country
                bindind.tvSunriseTime.text = unixTime(weatherList.sys.sunrise)
                bindind.tvSunsetTime.text = unixTime(weatherList.sys.sunset)


                when (weatherList.weather[i].icon) {
                    "01d" -> bindind.ivMain.setImageResource(R.drawable.sunny)
                    "02d" -> bindind.ivMain.setImageResource(R.drawable.cloud)
                    "03d" -> bindind.ivMain.setImageResource(R.drawable.cloud)
                    "04d" -> bindind.ivMain.setImageResource(R.drawable.cloud)
                    "04n" -> bindind.ivMain.setImageResource(R.drawable.cloud)
                    "10d" -> bindind.ivMain.setImageResource(R.drawable.rain)
                    "11d" -> bindind.ivMain.setImageResource(R.drawable.storm)
                    "13d" -> bindind.ivMain.setImageResource(R.drawable.snowflake)
                    "01n" -> bindind.ivMain.setImageResource(R.drawable.cloud)
                    "02n" -> bindind.ivMain.setImageResource(R.drawable.cloud)
                    "03n" -> bindind.ivMain.setImageResource(R.drawable.cloud)
                    "10n" -> bindind.ivMain.setImageResource(R.drawable.cloud)
                    "11n" -> bindind.ivMain.setImageResource(R.drawable.rain)
                    "13n" -> bindind.ivMain.setImageResource(R.drawable.snowflake)
                }
            }
        }
    }
    private fun getUnit(value: String):String? {
        Log.i("unitSsSs", value)
        var value = "°C"
        if ("US" == value || "LR" == value || "MM" == value) {
            value = "°F"
        }
        return value
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun unixTime(timex: Long): String? {
        val date = Date(timex *1000L)
        @SuppressLint("SimpleDateFormat")
        val sdf = SimpleDateFormat("HH:mm", Locale.CHINA)
        sdf.timeZone = TimeZone.getDefault()
        return sdf.format(date)
    }

    fun getLocationWeather() {
        if (Constants.isNetworkAvailable(this@MainActivity)) {
            val retrofit: Retrofit = Retrofit.Builder()
                .baseUrl(Constants.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            val service: WeatherService =
                retrofit.create<WeatherService>(WeatherService::class.java)
            val listCall: Call<WeatherResponse> = service.getLocationWeather(
                mLatitude, mLongitude, Constants.METRIC_UNIT,Constants.Location,Constants.APP_ID
            )
            showCustomProgressDialog()
            listCall.enqueue(object : Callback<WeatherResponse> {
                @RequiresApi(Build.VERSION_CODES.N)
                @SuppressLint("SetTextI18n")
                override fun onResponse(
                    response: Response<WeatherResponse>,
                    retrofit: Retrofit
                ) {

                    if (response.isSuccess) {
                        hideProgressDialog()

                        val weatherList: WeatherResponse = response.body()
                        Log.i("Response Result", "$weatherList")

                        val weatherResponseJsonString = Gson().toJson(weatherList)
                        val editor = mSharedPreferences.edit()

                        editor.putString(Constants.WEATHER_RESPONSE_DATA, weatherResponseJsonString)
                        editor.apply()

                        var location : String = weatherList.sys.country
                        if (location == "PH") {
                            setupUI()
                        } else {
                            Toast.makeText(
                                this@MainActivity,
                                "Invalid City",
                                Toast.LENGTH_LONG
                            ).show()
                        }

                    } else {
                        val sc = response.code()
                        hideProgressDialog()
                        when (sc) {
                            400 -> {
                                Log.e("Error 400", "Bad Request")
                            }
                            404 -> {
                                Log.e("Error 404", "Not Found")
                                Toast.makeText(
                                    this@MainActivity,
                                    "Invalid City",
                                    Toast.LENGTH_LONG
                                ).show()
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
                this@MainActivity,
                "No internet connection available.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

} //End Bracket



