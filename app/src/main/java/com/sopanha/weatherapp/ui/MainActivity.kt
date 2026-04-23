package com.sopanha.weatherapp.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.sopanha.weatherapp.R
import com.sopanha.weatherapp.databinding.ActivityMainBinding
import com.sopanha.weatherapp.viewmodel.WeatherUiState
import com.sopanha.weatherapp.viewmodel.WeatherViewModel
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: WeatherViewModel by viewModels()
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) fetchCurrentLocation()
        else Toast.makeText(this, getString(R.string.location_permission_denied), Toast.LENGTH_SHORT).show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        setupUI()
        observeViewModel()
    }

    private fun setupUI() {
        // Search button click
        binding.btnSearch.setOnClickListener {
            val city = binding.etCity.text.toString().trim()
            if (city.isNotEmpty()) {
                hideKeyboard()
                viewModel.fetchByCity(city)
            } else {
                binding.etCity.error = getString(R.string.error_empty_city)
            }
        }

        // IME search action
        binding.etCity.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                binding.btnSearch.performClick()
                true
            } else false
        }

        // GPS button click
        binding.btnLocation.setOnClickListener {
            requestLocationPermission()
        }

        // Unit toggle
        binding.tvUnitToggle.setOnClickListener {
            viewModel.toggleUnit()
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                when (state) {
                    is WeatherUiState.Idle -> showIdle()
                    is WeatherUiState.Loading -> showLoading()
                    is WeatherUiState.Success -> showWeather(state)
                    is WeatherUiState.Error -> showError(state.message)
                }
            }
        }
    }

    private fun showIdle() {
        binding.progressBar.visibility = View.GONE
        binding.cardWeather.visibility = View.GONE
        binding.tvError.visibility = View.GONE
    }

    private fun showLoading() {
        binding.progressBar.visibility = View.VISIBLE
        binding.cardWeather.visibility = View.GONE
        binding.tvError.visibility = View.GONE
    }

    private fun showWeather(state: WeatherUiState.Success) {
        binding.progressBar.visibility = View.GONE
        binding.tvError.visibility = View.GONE
        binding.cardWeather.visibility = View.VISIBLE

        val data = state.data
        val unitSymbol = if (state.unit == "metric") "°C" else "°F"
        val windUnit = if (state.unit == "metric") "m/s" else "mph"

        // City & country
        binding.tvCityName.text = "${data.name}, ${data.sys.country}"

        // Temperature
        val temp = data.main.temp.roundToInt()
        binding.tvTemperature.text = "$temp$unitSymbol"

        // Weather icon
        val iconCode = data.weather.firstOrNull()?.icon ?: "01d"
        Glide.with(this)
            .load("https://openweathermap.org/img/wn/${iconCode}@2x.png")
            .into(binding.ivWeatherIcon)

        // Description
        binding.tvDescription.text = data.weather.firstOrNull()?.description?.replaceFirstChar { it.uppercase() } ?: ""

        // Feels like
        val feelsLike = data.main.feelsLike.roundToInt()
        binding.tvFeelsLike.text = getString(R.string.feels_like, "$feelsLike$unitSymbol")

        // Min / Max
        val tempMin = data.main.tempMin.roundToInt()
        val tempMax = data.main.tempMax.roundToInt()
        binding.tvMinMax.text = getString(R.string.min_max, "$tempMin$unitSymbol", "$tempMax$unitSymbol")

        // Humidity
        binding.tvHumidity.text = getString(R.string.humidity_value, data.main.humidity)

        // Wind
        binding.tvWind.text = getString(R.string.wind_value, data.wind.speed, windUnit)

        // Pressure
        binding.tvPressure.text = getString(R.string.pressure_value, data.main.pressure)

        // Unit toggle label
        binding.tvUnitToggle.text = if (state.unit == "metric") "Switch to °F" else "Switch to °C"
    }

    private fun showError(message: String) {
        binding.progressBar.visibility = View.GONE
        binding.cardWeather.visibility = View.GONE
        binding.tvError.visibility = View.VISIBLE
        binding.tvError.text = message
    }

    private fun requestLocationPermission() {
        when {
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED -> fetchCurrentLocation()
            else -> locationPermissionRequest.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    @SuppressLint("MissingPermission")
    private fun fetchCurrentLocation() {
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                viewModel.fetchByLocation(location.latitude, location.longitude)
            } else {
                Toast.makeText(this, getString(R.string.location_unavailable), Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener {
            Toast.makeText(this, getString(R.string.location_error), Toast.LENGTH_SHORT).show()
        }
    }

    private fun hideKeyboard() {
        val imm = getSystemService(android.view.inputmethod.InputMethodManager::class.java)
        imm.hideSoftInputFromWindow(binding.root.windowToken, 0)
    }
}
