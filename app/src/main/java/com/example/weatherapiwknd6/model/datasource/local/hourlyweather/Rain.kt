package com.example.weatherapiwknd6.model.datasource.local.hourlyweather


import com.google.gson.annotations.SerializedName

data class Rain(
    @SerializedName("3h")
    val h: Double
)