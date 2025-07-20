package android.weather.data.remote.api

import android.weather.BuildConfig
import android.weather.data.remote.model.WeatherResponse
import retrofit2.http.*

interface ApiInterface {
    @GET("/data/2.5/weather")
    suspend fun getWeather(
        @Query("q") city: String,
        @Query("appid") apiKey: String = BuildConfig.API_KEY
    ): WeatherResponse

}
