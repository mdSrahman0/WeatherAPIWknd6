package com.example.weatherapiwknd6.model.datasource.local


import com.google.gson.annotations.SerializedName

data class Main(
    @SerializedName("humidity")
    val humidity: Int,
    @SerializedName("pressure")
    val pressure: Float,
    @SerializedName("temp")
    val temp: Float,
    @SerializedName("temp_max")
    val tempMax: Float,
    @SerializedName("temp_min")
    val tempMin: Double
)