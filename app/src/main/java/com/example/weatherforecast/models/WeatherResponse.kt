package com.example.weatherforecast.models

import java.io.Serializable

data class WeatherResponse(

    //For the Location Coordinates
    val coord: Coord,

    //List of Weather Forecast
    val weather : List<Weather>,

    val base: String,
    val main: Main,
    val visibility: Int,
    val wind: Wind,
    val cloud: Cloud,
    val dt: Int,
    val sys: Sys,
    val id: Int,
    val name: String,
    val cod: Int
) : Serializable