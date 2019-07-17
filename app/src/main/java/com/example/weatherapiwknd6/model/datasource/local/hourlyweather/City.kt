package com.example.weatherapiwknd6.model.datasource.local.hourlyweather


import com.google.gson.annotations.SerializedName

data class City(
    @SerializedName("coord")
    val coord: Coord,
    @SerializedName("country")
    val country: String,
    @SerializedName("name")
    val name: String,
    @SerializedName("timezone")
    val timezone: Int
)