package org.example

data class Trend(val slope: Double, val intercept: Double)

class TrendDetector {

    // We use x = 0,1,2,... (time index) for simplicity
    fun temperatureTrend(data: List<WeatherData>): Trend {
       return calculateTrend(data.map { it.temperature })
    }
    fun humidityTrend(data: List<WeatherData>): Trend {
        return calculateTrend(data.map { it.humidity })
    }

    fun pressureTrend(data: List<WeatherData>): Trend {
        return calculateTrend(data.map { it.pressure })
    }

    // Core math
    private fun calculateTrend(y: List<Double>): Trend {
        val n = y.size
        if (n == 0) return Trend(0.0, 0.0)
        if (n == 1) return Trend(0.0, y[0])

        val x = (0 until n).map { it.toDouble() }

        val sumX = x.sum()
        val sumY = y.sum()
        val sumXY = x.zip(y).sumOf { (xi, yi) -> xi * yi }
        val sumXX = x.sumOf { xi -> xi * xi }

        val denom = (n * sumXX - sumX * sumX)
        if (denom == 0.0) return Trend(0.0, y.average())

        val slope = (n * sumXY - sumX * sumY) / denom
        val intercept = (sumY - slope * sumX) / n

        return Trend(slope, intercept)
    }
}
