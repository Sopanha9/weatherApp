package org.example

data class Stats(val avg: Double, val min: Double, val max: Double)

class DataAnalyzer {

    private fun statsOf(values: List<Double>): Stats {
        if (values.isEmpty()) return Stats(0.0, 0.0, 0.0)

        return Stats(
            avg = values.average(),
            min = values.minOrNull() ?: 0.0,
            max = values.maxOrNull() ?: 0.0
        )
    }

    fun temperatureStats(data: List<WeatherData>): Stats =
        statsOf(data.map { it.temperature })

    fun humidityStats(data: List<WeatherData>): Stats =
        statsOf(data.map { it.humidity })

    fun pressureStats(data: List<WeatherData>): Stats =
        statsOf(data.map { it.pressure })
}