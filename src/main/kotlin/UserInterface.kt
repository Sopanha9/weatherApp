package org.example

import java.time.LocalDate

class UserInterface(
    private val processor: DataProcessor,
    private val analyzer: DataAnalyzer,
    private val trendDetector: TrendDetector,
    private val anomalyDetector: AnomalyDetector
) {
    fun start(csvPath: String) {
        val all = processor.readFromFile(csvPath)
        var current = all

        while (true) {
            println("\n=== WeatherApp Menu ===")
            println("1) Show record count")
            println("2) Filter by date range")
            println("3) Show stats (avg/min/max)")
            println("4) Show trends (temp/humidity/pressure)")
            println("5) Show anomalies (temp/pressure)")
            println("0) Exit")
            print("Choose: ")

            when (readLine()?.trim()) {
                "1" -> {
                    println("Records: ${current.size}")
                }

                "2" -> {
                    print("Start date (YYYY-MM-DD): ")
                    val start = LocalDate.parse(readLine()?.trim())
                    print("End date (YYYY-MM-DD): ")
                    val end = LocalDate.parse(readLine()?.trim())

                    current = processor.filterByDateRange(current, start, end)
                    println("Filtered records: ${current.size}")
                }

                "3" -> {
                    val t = analyzer.temperatureStats(current)
                    val h = analyzer.humidityStats(current)
                    val p = analyzer.pressureStats(current)

                    println("Temp     avg=${t.avg} min=${t.min} max=${t.max}")
                    println("Humidity avg=${h.avg} min=${h.min} max=${h.max}")
                    println("Pressure avg=${p.avg} min=${p.min} max=${p.max}")
                }

                "4" -> {
                    val t = trendDetector.temperatureTrend(current)
                    val h = trendDetector.humidityTrend(current)
                    val p = trendDetector.pressureTrend(current)

                    println("Temp trend:     slope=${t.slope}")
                    println("Humidity trend: slope=${h.slope}")
                    println("Pressure trend: slope=${p.slope}")
                }

                "5" -> {
                    // for now, just show temp + pressure anomalies with easier threshold
                    val t = anomalyDetector.temperatureAnomalies(current, threshold = 1.9)
                    val p = anomalyDetector.pressureAnomalies(current, threshold = 1.9)

                    println("\nTemperature anomalies:")
                    if (t.isEmpty()) {
                        println("None")
                    } else {
                        t.forEach { a ->
                            val date = current[a.index].date
                            println("Date: $date | Value: ${a.value} | Z-score: ${"%.2f".format(a.zScore)}")
                        }
                    }

                    println("\nPressure anomalies:")
                    if (p.isEmpty()) {
                        println("None")
                    } else {
                        p.forEach { a ->
                            val date = current[a.index].date
                            println("Date: $date | Value: ${a.value} | Z-score: ${"%.2f".format(a.zScore)}")
                        }
                    }
                }

                "0" -> return
                else -> println("Invalid choice.")
            }
        }
    }
}