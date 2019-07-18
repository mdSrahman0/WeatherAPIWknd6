package com.example.weatherapiwknd6

import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.View
import android.view.View.VISIBLE
import android.widget.RadioButton
import android.widget.Toast
import com.example.weatherapiwknd6.model.datasource.local.currentweather.WeatherResponse
import com.example.weatherapiwknd6.model.datasource.local.hourlyweather.HourlyWeather
import com.example.weatherapiwknd6.model.datasource.local.hourlyweather.X
import kotlinx.android.synthetic.main.activity_main.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.lang.Boolean.FALSE
import java.lang.Boolean.TRUE
import java.util.regex.Pattern

class MainActivity : AppCompatActivity() {

    val CURRENT_TEMP_URL = "http://api.openweathermap.org/data/2.5/"
    val HOURLY_TEMP_URL = "http://api.openweathermap.org/data/2.5/"
    val APPID = "c467f3cd5ea422c1324671a57e10f64a"

    private var weatherAPI: WeatherAPI? = null

    lateinit var rbFahrenheit : RadioButton
    lateinit var rbCelsius : RadioButton

    private var myPreferences = "myPrefs"
    private var ZIP = "zip" // key for shared pref
    private var EMPTY = 0   // default val for shared pref

    private lateinit var linearLayoutManager: LinearLayoutManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // these two radio buttons are invisible by default, so the user doesn't see them until they enter first zip
        rbFahrenheit = findViewById(R.id.rbFahrenheit)
        rbCelsius = findViewById(R.id.rbCelsius)

        val retrofit = Retrofit.Builder()
            .baseUrl(CURRENT_TEMP_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        weatherAPI = retrofit.create(WeatherAPI::class.java)

        val sharedPreferences = getSharedPreferences(myPreferences, Context.MODE_PRIVATE)

        // check if shared preference exists. if so, fetch weather info
        if(sharedPreferences.getInt(ZIP, EMPTY) != EMPTY) {
            val zip = sharedPreferences.getInt(ZIP, 0)
            getCurrentWeather(zip)
        }
    }

    fun onClick(view: View) {
        //val etMyZip = etZip.text.toString().toInt()
        if (!etZip.text.isNullOrBlank()) {
            val regex = "^[0-9]{5}(?:-[0-9]{4})?$"
            val pattern = Pattern.compile(regex)
            val matcher = pattern.matcher(etZip.text.toString())
            if(matcher.matches()) {
                val sharedPreferences = getSharedPreferences(myPreferences, Context.MODE_PRIVATE)
                val editor = sharedPreferences.edit()
                val etZip = etZip.text.toString().toInt()
                editor.putInt(ZIP, etZip)
                editor.apply()
                rbFahrenheit.isChecked = FALSE
                rbCelsius.isChecked = FALSE
                getCurrentWeather(etZip)
            }
            else {
                Toast.makeText(applicationContext, "ENTER A VALID ZIP", Toast.LENGTH_LONG).show()
            }
        }
        else {
            Toast.makeText(applicationContext, "ENTER A VALID ZIP", Toast.LENGTH_LONG).show()
        }
    }

    fun getCurrentWeather(zip : Int) {
        val call = weatherAPI?.getWeather(zip, APPID)

        call?.enqueue(object: Callback<WeatherResponse> {
            override fun onResponse(call: Call<WeatherResponse>, response: Response<WeatherResponse>) {
                val weatherResponse = response.body()

                // remember, onResponse means we successfully got a response, but information in that response might be null
                // even if we get a response (meaning the zip code is a 5 digit number), it might be a null. so check it
                if(weatherResponse == null) {
                    Toast.makeText(applicationContext, "ZIP CODE DOESN'T EXIST", Toast.LENGTH_LONG).show()
                }
                else {
                    val cityName = weatherResponse!!.name
                    val currentTemp = weatherResponse.main.temp
                    tvResult.text = currentTemp.toString()
                    tvCityName.text = cityName

                    // set both radio buttons to Visible
                    rbFahrenheit.visibility = VISIBLE
                    rbCelsius.visibility = VISIBLE

                    // by default, fahrenheit is displayed
                    rbFahrenheit.isChecked = TRUE
                    var fahrTemp = getFahrenheit(currentTemp)
                    if (fahrTemp > 60.00) {
                        mainActivityLayout.setBackgroundResource(R.color.colorYellow)
                    }
                    if (fahrTemp <= 60.00) {
                        mainActivityLayout.setBackgroundResource(R.color.colorPrimary)
                    }
                    tvResult.text = ("$fahrTemp \u2109")

                    rbFahrenheit.setOnClickListener {
                        var fahrTemp = getFahrenheit(currentTemp)
                        if (fahrTemp > 60.00) {
                            mainActivityLayout.setBackgroundResource(R.color.colorYellow)
                        }
                        if (fahrTemp <= 60.00) {
                            mainActivityLayout.setBackgroundResource(R.color.colorPrimary)
                        }
                        tvResult.text = ("$fahrTemp \u2109")
                        getHourlyWeather(zip, "F")
                    }

                    rbCelsius.setOnClickListener {
                        var celTemp = getCelsius(currentTemp)
                        if (celTemp > 16.00) {
                            mainActivityLayout.setBackgroundResource(R.color.colorYellow)
                        }
                        if (celTemp <= 16.00) {
                            mainActivityLayout.setBackgroundResource(R.color.colorPrimary)
                        }
                        // u2103 is celcius
                        tvResult.text = ("$celTemp \u2103")
                        getHourlyWeather(zip, "C")
                    }

                    // once the current temp has been displayed, call the function to display the hourly weather
                    getHourlyWeather(zip, "F")
                }
            }

            override fun onFailure(call: Call<WeatherResponse>, t: Throwable) {
                tvResult.text = "NOT A VALID ZIP CODE"
            }
        })
    }

    fun getHourlyWeather(zip : Int, degreeType : String) {
        val retrofit = Retrofit.Builder()
            .baseUrl(HOURLY_TEMP_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        weatherAPI = retrofit.create(WeatherAPI::class.java)

        val call = weatherAPI?.getForecast(zip, APPID)
        
        call?.enqueue(object : Callback<HourlyWeather> {
            override fun onResponse(call: Call<HourlyWeather>, response: Response<HourlyWeather>) {
                val hourlyWeather = response.body()
                val hourlyWeatherList = hourlyWeather!!.list
                populateRecyclerView(hourlyWeatherList, degreeType)
            }

            override fun onFailure(call: Call<HourlyWeather>, t: Throwable) {
                Log.d("TAG", t.message)
            }
        })
    }

    fun populateRecyclerView(hourlyWeatherList: List<X>, degreeType: String) {
        linearLayoutManager = LinearLayoutManager(this)
        recyclerView.layoutManager = linearLayoutManager
        recyclerView.addItemDecoration(DividerItemDecoration(recyclerView.context, DividerItemDecoration.VERTICAL))
        val recyclerViewAdapter = RecyclerViewAdapter(hourlyWeatherList, degreeType)
        recyclerView.adapter = recyclerViewAdapter
    }
}