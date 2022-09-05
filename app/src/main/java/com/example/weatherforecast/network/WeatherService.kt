package com.example.weatherforecast.network

import com.example.weatherforecast.models.WeatherResponse
import retrofit.Call
import retrofit.http.GET
import retrofit.http.Query

interface WeatherService {

    @GET("2.5/weather")
    fun getWeather(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("units") unit: String?,
        @Query("appID") appID: String?,
    ) : Call<WeatherResponse>

    @GET("2.5/weather")
    fun getLocationWeather(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("units") unit: String?,
        @Query("q") City: String?,
        @Query("appID") appID: String?,
    ) : Call<WeatherResponse>


}