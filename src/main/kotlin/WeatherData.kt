package org.example

import java.time.LocalDate

data class WeatherData(
    val date: LocalDate,
    val location: String,
    val temperature: Double,
    val humidity: Double,
    val pressure: Double,
)
