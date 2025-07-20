package android.weather.data.remote.repository

import android.weather.data.remote.api.ApiInterface
import android.weather.data.remote.model.WeatherResponse
import jakarta.inject.Inject

class WeatherRepository @Inject constructor(
    private val api: ApiInterface
) {
    suspend fun fetchWeather(cityName: String): WeatherResponse {
        return api.getWeather(cityName)
    }
}
