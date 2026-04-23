package com.sopanha.weatherapp.data.repository

import com.sopanha.weatherapp.BuildConfig
import com.sopanha.weatherapp.data.model.WeatherResponse
import com.sopanha.weatherapp.utils.ApiResult
import com.google.gson.Gson
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherApiService {
    @GET("weather")
    suspend fun getWeatherByCity(
        @Query("q") city: String,
        @Query("appid") apiKey: String = BuildConfig.OWM_API_KEY,
        @Query("units") units: String = "metric"
    ): WeatherResponse

    @GET("weather")
    suspend fun getWeatherByCoords(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("appid") apiKey: String = BuildConfig.OWM_API_KEY,
        @Query("units") units: String = "metric"
    ): WeatherResponse
}

class WeatherRepository {

    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(logging)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://api.openweathermap.org/data/2.5/")
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val api = retrofit.create(WeatherApiService::class.java)

    suspend fun fetchByCity(city: String, units: String): ApiResult<WeatherResponse> {
        return try {
            val response = api.getWeatherByCity(city = city, units = units)
            ApiResult.Success(response)
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Unknown error")
        }
    }

    suspend fun fetchByCoords(lat: Double, lon: Double, units: String): ApiResult<WeatherResponse> {
        return try {
            val response = api.getWeatherByCoords(lat = lat, lon = lon, units = units)
            ApiResult.Success(response)
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Unknown error")
        }
    }
}
