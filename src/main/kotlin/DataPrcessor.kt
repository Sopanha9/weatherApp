package org.example

import java.time.LocalDate
import java.io.File


class DataProcessor {
    fun parseLines(lines: List<String>): List<WeatherData> {
        val result = mutableListOf<WeatherData>()

        for (line in lines) {
            val trimmed = line.trim()
            if (trimmed.isBlank()) continue
            if (trimmed.startsWith("date", ignoreCase = true)) continue // header

            val parts = trimmed.split(",")
            if (parts.size < 3) continue

            val date = LocalDate.parse(parts[0].trim())
            val location = parts[1].trim()
            val temp = parts[2].trim().toDouble()
            val humidity = parts[3].trim().toDouble()
            val pressure = parts[4].trim().toDouble()

            result.add(WeatherData(date, location, temp, humidity, pressure))
        }

        return result
    }

    fun readFromFile(path: String): List<WeatherData> {
        val lines = java.io.File(path).readLines()
        return parseLines(lines)
    }

    fun filterByDateRange(
        data: List<WeatherData>,
        start: LocalDate,
        end: LocalDate): List<WeatherData> {

        require(!end.isBefore(start)) { "End date must be >= start date" }

        return data.filter {wd ->
            (!wd.date.isBefore(start)) && (!wd.date.isAfter(end))
        }
    }
}
