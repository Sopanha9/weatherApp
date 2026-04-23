package com.sopanha.weatherapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sopanha.weatherapp.data.model.WeatherResponse
import com.sopanha.weatherapp.data.repository.WeatherRepository
import com.sopanha.weatherapp.utils.ApiResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

class WeatherViewModel : ViewModel() {

    private val repository = WeatherRepository()

    private val _weatherState = MutableStateFlow<ApiResult<WeatherResponse>?>(null)
    val weatherState: StateFlow<ApiResult<WeatherResponse>?> = _weatherState

    private val _isCelsius = MutableStateFlow(true)
    val isCelsius: StateFlow<Boolean> = _isCelsius

    // Cached raw metric data (always fetch metric, convert locally)
    private var cachedResponse: WeatherResponse? = null

    fun fetchByCity(city: String) {
        if (city.isBlank()) return
        viewModelScope.launch {
            _weatherState.value = ApiResult.Loading
            val result = repository.fetchByCity(city.trim(), "metric")
            if (result is ApiResult.Success) {
                cachedResponse = result.data
                _weatherState.value = ApiResult.Success(applyUnit(result.data))
            } else {
                _weatherState.value = result
            }
        }
    }

    fun fetchByCoords(lat: Double, lon: Double) {
        viewModelScope.launch {
            _weatherState.value = ApiResult.Loading
            val result = repository.fetchByCoords(lat, lon, "metric")
            if (result is ApiResult.Success) {
                cachedResponse = result.data
                _weatherState.value = ApiResult.Success(applyUnit(result.data))
            } else {
                _weatherState.value = result
            }
        }
    }

    fun toggleUnit() {
        _isCelsius.value = !_isCelsius.value
        cachedResponse?.let {
            _weatherState.value = ApiResult.Success(applyUnit(it))
        }
    }

    private fun applyUnit(data: WeatherResponse): WeatherResponse {
        return if (_isCelsius.value) {
            data
        } else {
            // Convert °C → °F
            data.copy(
                main = data.main.copy(
                    temp = cToF(data.main.temp),
                    feelsLike = cToF(data.main.feelsLike),
                    tempMin = cToF(data.main.tempMin),
                    tempMax = cToF(data.main.tempMax)
                )
            )
        }
    }

    private fun cToF(c: Double): Double = (c * 9.0 / 5.0) + 32.0

    fun formatTemp(temp: Double): String {
        val unit = if (_isCelsius.value) "°C" else "°F"
        return "${temp.roundToInt()}$unit"
    }
}
