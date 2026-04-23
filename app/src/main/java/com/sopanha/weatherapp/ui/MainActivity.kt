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
import com.sopanha.weatherapp.data.model.WeatherResponse
import com.sopanha.weatherapp.databinding.ActivityMainBinding
import com.sopanha.weatherapp.utils.ApiResult
import com.sopanha.weatherapp.viewmodel.WeatherViewModel
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: WeatherViewModel by viewModels()
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) fetchCurrentLocation()
        else Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        setupListeners()
        observeViewModel()

        // Load default city on start
        viewModel.fetchByCity("London")
    }

    private fun setupListeners() {
        binding.btnSearch.setOnClickListener { triggerSearch() }

        binding.etCity.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                triggerSearch(); true
            } else false
        }

        binding.btnLocation.setOnClickListener { requestLocation() }

        binding.btnToggleUnit.setOnClickListener { viewModel.toggleUnit() }
    }

    private fun triggerSearch() {
        val city = binding.etCity.text.toString()
        if (city.isBlank()) {
            binding.etCity.error = "Enter a city name"
            return
        }
        viewModel.fetchByCity(city)
    }

    private fun requestLocation() {
        val fineGranted = ContextCompat.checkSelfPermission(
            this, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val coarseGranted = ContextCompat.checkSelfPermission(
            this, Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (fineGranted || coarseGranted) {
            fetchCurrentLocation()
        } else {
            locationPermissionLauncher.launch(
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
                viewModel.fetchByCoords(location.latitude, location.longitude)
            } else {
                Toast.makeText(this, "Unable to get location. Try again.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.weatherState.collect { state ->
                when (state) {
                    is ApiResult.Loading -> showLoading(true)
                    is ApiResult.Success -> {
                        showLoading(false)
                        updateUI(state.data)
                    }
                    is ApiResult.Error -> {
                        showLoading(false)
                        showError(state.message)
                    }
                    null -> { /* initial state */ }
                }
            }
        }

        lifecycleScope.launch {
            viewModel.isCelsius.collect { isCelsius ->
                binding.btnToggleUnit.text = if (isCelsius) "°C / °F" else "°F / °C"
            }
        }
    }

    private fun updateUI(data: WeatherResponse) {
        binding.weatherCard.visibility = View.VISIBLE
        binding.tvError.visibility = View.GONE

        binding.tvCityName.text = "${data.cityName}, ${data.sys.country}"
        binding.tvTemperature.text = viewModel.formatTemp(data.main.temp)
        binding.tvDescription.text = data.weather.firstOrNull()?.description?.replaceFirstChar { it.uppercase() } ?: ""
        binding.tvFeelsLike.text = "Feels like ${viewModel.formatTemp(data.main.feelsLike)}"
        binding.tvMinMax.text = "↓ ${viewModel.formatTemp(data.main.tempMin)}  ↑ ${viewModel.formatTemp(data.main.tempMax)}"
        binding.tvHumidity.text = "${data.main.humidity}%"
        binding.tvWind.text = "${data.wind.speed} m/s"
        binding.tvPressure.text = "${data.main.pressure} hPa"

        val iconUrl = "https://openweathermap.org/img/wn/${data.weather.firstOrNull()?.icon}@2x.png"
        Glide.with(this)
            .load(iconUrl)
            .placeholder(R.drawable.ic_placeholder)
            .into(binding.ivWeatherIcon)
    }

    private fun showLoading(loading: Boolean) {
        binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        if (loading) {
            binding.weatherCard.visibility = View.GONE
            binding.tvError.visibility = View.GONE
        }
    }

    private fun showError(message: String) {
        binding.tvError.visibility = View.VISIBLE
        binding.tvError.text = "Error: $message"
        binding.weatherCard.visibility = View.GONE
    }
}
