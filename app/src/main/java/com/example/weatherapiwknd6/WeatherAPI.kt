package com.example.weatherapiwknd6

import com.example.weatherapiwknd6.model.datasource.local.currentweather.WeatherResponse
import com.example.weatherapiwknd6.model.datasource.local.hourlyweather.HourlyWeather
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherAPI {
    @GET("weather")
    fun getWeather(@Query("zip") zip: Int, @Query("appid") appid: String): Call<WeatherResponse>

    @GET("forecast")
    fun getForecast(@Query ("zip") zip : Int, @Query("appid") appid: String): Call<HourlyWeather>
}