package com.example.weatherapiwknd6.model.datasource.local.currentweather


import com.google.gson.annotations.SerializedName

data class Clouds(
    @SerializedName("all")
    val all: Int
)