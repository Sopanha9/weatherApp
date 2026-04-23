package com.sopanha.weatherapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sopanha.weatherapp.data.model.WeatherResponse
import com.sopanha.weatherapp.data.repository.WeatherRepository
import com.sopanha.weatherapp.utils.ApiResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class WeatherUiState {
    object Idle : WeatherUiState()
    object Loading : WeatherUiState()
    data class Success(val data: WeatherResponse, val unit: String) : WeatherUiState()
    data class Error(val message: String) : WeatherUiState()
}

class WeatherViewModel : ViewModel() {

    private val repository = WeatherRepository()

    private val _uiState = MutableStateFlow<WeatherUiState>(WeatherUiState.Idle)
    val uiState: StateFlow<WeatherUiState> = _uiState.asStateFlow()

    // "metric" = Celsius, "imperial" = Fahrenheit
    private var currentUnit = "metric"
    private var lastCity: String? = null
    private var lastLat: Double? = null
    private var lastLon: Double? = null

    fun fetchByCity(city: String) {
        lastCity = city
        lastLat = null
        lastLon = null
        viewModelScope.launch {
            _uiState.value = WeatherUiState.Loading
            when (val result = repository.getWeatherByCity(city, currentUnit)) {
                is ApiResult.Success -> _uiState.value = WeatherUiState.Success(result.data, currentUnit)
                is ApiResult.Error -> _uiState.value = WeatherUiState.Error(result.message)
                is ApiResult.Loading -> { /* no-op */ }
            }
        }
    }

    fun fetchByLocation(lat: Double, lon: Double) {
        lastLat = lat
        lastLon = lon
        lastCity = null
        viewModelScope.launch {
            _uiState.value = WeatherUiState.Loading
            when (val result = repository.getWeatherByCoords(lat, lon, currentUnit)) {
                is ApiResult.Success -> _uiState.value = WeatherUiState.Success(result.data, currentUnit)
                is ApiResult.Error -> _uiState.value = WeatherUiState.Error(result.message)
                is ApiResult.Loading -> { /* no-op */ }
            }
        }
    }

    fun toggleUnit() {
        currentUnit = if (currentUnit == "metric") "imperial" else "metric"
        // Refresh with current unit
        lastCity?.let { fetchByCity(it) }
            ?: run {
                val lat = lastLat
                val lon = lastLon
                if (lat != null && lon != null) fetchByLocation(lat, lon)
            }
    }

    fun getCurrentUnit(): String = currentUnit
}
