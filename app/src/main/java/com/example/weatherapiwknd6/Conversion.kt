package com.example.weatherapiwknd6

fun getFahrenheit(currentTemp: Float): Int {
    val currentTemp = (9.0 / 5.0) * (currentTemp - 273.0) + 32.0
    return currentTemp.toInt()
}

fun getCelsius(currentTemp: Float): Int {
    var currentTemp = currentTemp - 273.0
    return currentTemp.toInt()
}
