package com.example.weatherapiwknd6.model.datasource.local.hourlyweather


import com.google.gson.annotations.SerializedName

data class Clouds(
    @SerializedName("all")
    val all: Int
)