package org.example

import kotlin.math.abs
import kotlin.math.sqrt

data class Anomaly(val index: Int, val value: Double, val zScore: Double)

class AnomalyDetector {

    // reusable engine
    private fun detect(values: List<Double>, threshold: Double = 2.0): List<Anomaly> {
        if (values.isEmpty()) return emptyList()

        val mean = values.average()
        val variance = values.map { (it - mean) * (it - mean) }.average()
        val stdDev = sqrt(variance)

        if (stdDev == 0.0) return emptyList()

        return values.mapIndexedNotNull { idx, v ->
            val z = abs(v - mean) / stdDev
            if (z > threshold) Anomaly(idx, v, z) else null
        }
    }

    fun temperatureAnomalies(data: List<WeatherData>, threshold: Double = 2.0): List<Anomaly> =
        detect(data.map { it.temperature }, threshold)

    fun humidityAnomalies(data: List<WeatherData>, threshold: Double = 2.0): List<Anomaly> =
        detect(data.map { it.humidity }, threshold)

    fun pressureAnomalies(data: List<WeatherData>, threshold: Double = 2.0): List<Anomaly> =
        detect(data.map { it.pressure }, threshold)
}