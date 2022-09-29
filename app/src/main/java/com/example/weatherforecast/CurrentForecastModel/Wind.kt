package com.example.weatherforecast.CurrentForecastModel

import java.io.Serializable

data class Wind (
    val speed: Double,
    val degree: Int
        ) : Serializable