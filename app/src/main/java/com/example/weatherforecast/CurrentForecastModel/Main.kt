package com.example.weatherforecast.CurrentForecastModel

import java.io.Serializable

data class Main (
    val temp: Double,
    val pressure: Double,
    val humidity: Int,
    val tempMin: Double,
    val tempMax: Double,
    val seaLevel: Double,
    val groundLevel: Double
        ) : Serializable