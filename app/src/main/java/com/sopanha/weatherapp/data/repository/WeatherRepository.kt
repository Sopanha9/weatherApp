package com.sopanha.weatherapp.data.repository

import com.sopanha.weatherapp.BuildConfig
import com.sopanha.weatherapp.data.model.WeatherResponse
import com.sopanha.weatherapp.utils.ApiResult
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherApi {
    @GET("weather")
    suspend fun getWeatherByCity(
        @Query("q")     city:  String,
        @Query("appid") apiKey: String = BuildConfig.OWM_API_KEY,
        @Query("units") units:  String = "metric"
    ): WeatherResponse

    @GET("weather")
    suspend fun getWeatherByCoords(
        @Query("lat")   lat:   Double,
        @Query("lon")   lon:   Double,
        @Query("appid") apiKey: String = BuildConfig.OWM_API_KEY,
        @Query("units") units:  String = "metric"
    ): WeatherResponse
}

class WeatherRepository {

    private val api: WeatherApi by lazy {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }
        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()

        Retrofit.Builder()
            .baseUrl("https://api.openweathermap.org/data/2.5/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(WeatherApi::class.java)
    }

    suspend fun fetchByCity(city: String): ApiResult<WeatherResponse> {
        return try {
            ApiResult.Success(api.getWeatherByCity(city))
        } catch (e: retrofit2.HttpException) {
            when (e.code()) {
                404  -> ApiResult.Error("City \"$city\" not found. Please check spelling.")
                401  -> ApiResult.Error("Invalid API key. Replace with your OpenWeatherMap key.")
                else -> ApiResult.Error("Server error (${e.code()}). Try again.")
            }
        } catch (e: Exception) {
            ApiResult.Error("Network error. Check your internet connection.")
        }
    }

    suspend fun fetchByCoords(lat: Double, lon: Double): ApiResult<WeatherResponse> {
        return try {
            ApiResult.Success(api.getWeatherByCoords(lat, lon))
        } catch (e: Exception) {
            ApiResult.Error("Could not get location weather.")
        }
    }
}
