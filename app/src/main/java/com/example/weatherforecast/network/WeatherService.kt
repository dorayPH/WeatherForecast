package com.example.weatherforecast.network

import eu.tutorials.futureweatherforecast.futureForecastModel.FutureWeather
import com.example.weatherforecast.CurrentForecastModel.WeatherResponse
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

    @GET("2.5/forecast")
    fun getFutureWeather(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("units") units: String?,
        @Query("q") City: String?,
        @Query("appid") appid: String?

    ): Call<FutureWeather>


}
