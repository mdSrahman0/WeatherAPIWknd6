package com.example.weatherapiwknd6.model.datasource.local.hourlyweather


import com.google.gson.annotations.SerializedName

data class Sys(
    @SerializedName("pod")
    val pod: String
)