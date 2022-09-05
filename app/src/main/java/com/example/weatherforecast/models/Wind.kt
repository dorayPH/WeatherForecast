package com.example.weatherforecast.models

import java.io.Serializable

data class Wind (
    val speed: Double,
    val degree: Int
        ) : Serializable