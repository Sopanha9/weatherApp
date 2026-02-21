package org.example

fun main() {
    val ui = UserInterface(
        processor = DataProcessor(),
        analyzer = DataAnalyzer(),
        trendDetector = TrendDetector(),
        anomalyDetector = AnomalyDetector()
    )

    ui.start("src/main/resources/sample.csv")
}