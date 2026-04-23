package com.sopanha.weatherapp.data.model

import com.google.gson.annotations.SerializedName

data class WeatherResponse(
    val name: String,
    val sys: Sys,
    val main: Main,
    val weather: List<Weather>,
    val wind: Wind,
    val visibility: Int,
    val dt: Long
)

data class Sys(
    val country: String,
    val sunrise: Long,
    val sunset: Long
)

data class Main(
    val temp: Double,
    @SerializedName("feels_like") val feelsLike: Double,
    @SerializedName("temp_min") val tempMin: Double,
    @SerializedName("temp_max") val tempMax: Double,
    val humidity: Int,
    val pressure: Int
)

data class Weather(
    val id: Int,
    val main: String,
    val description: String,
    val icon: String
)

data class Wind(
    val speed: Double,
    val deg: Int
)
