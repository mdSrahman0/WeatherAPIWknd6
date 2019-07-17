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

                val cityName = weatherResponse!!.name
                tvCityName.text = cityName

                val currentTemp = weatherResponse.main.temp
                tvResult.text = currentTemp.toString()

                // set both radio buttons to Visible
                rbFahrenheit.visibility = VISIBLE
                rbCelsius.visibility = VISIBLE

                // by default, fahrenheit is displayed
                rbFahrenheit.isChecked = TRUE
                getFahrenheit(currentTemp)

                rbFahrenheit.setOnClickListener {
                    getFahrenheit(currentTemp)
                }

                rbCelsius.setOnClickListener {
                    getCelsius(currentTemp)
                }

                // once the current temp has been displayed, call the function to display the hourly weather
                getHourlyWeather(zip)
            }

            override fun onFailure(call: Call<WeatherResponse>, t: Throwable) {
                //tvResult.text = t.message
                tvResult.text = "NOT A VALID ZIP CODE"
            }
        })
    }

    fun getHourlyWeather(zip : Int) {
        Log.d("TAG", "inside")
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
                populateRecyclerView(hourlyWeatherList)
                Log.d("TAG", "inside onResponse")
            }

            override fun onFailure(call: Call<HourlyWeather>, t: Throwable) {
                Log.d("TAG", t.message)
            }
        })
    }

    fun populateRecyclerView(hourlyWeatherList: List<X>) {
        linearLayoutManager = LinearLayoutManager(this)
        recyclerView.layoutManager = linearLayoutManager
        recyclerView.addItemDecoration(DividerItemDecoration(recyclerView.context, DividerItemDecoration.VERTICAL))
        val recyclerViewAdapter = RecyclerViewAdapter(hourlyWeatherList)
        recyclerView.adapter = recyclerViewAdapter
    }

    fun getFahrenheit(currentTemp : Float) {
        var currentTemp = (9.0/5.0)*(currentTemp - 273.0) + 32.0
        val fahrTemp = currentTemp.toInt()
        if (fahrTemp > 60.00) {
            mainActivityLayout.setBackgroundResource(R.color.colorYellow)
        }
        if (fahrTemp <= 60.00) {
            mainActivityLayout.setBackgroundResource(R.color.colorPrimary)
        }
        tvResult.text = ("$fahrTemp \u2109")
    }

    fun getCelsius(currentTemp: Float) {
        var currentTemp = currentTemp - 273.0
        var celTemp = currentTemp.toInt()
        if (celTemp > 16.00) {
            mainActivityLayout.setBackgroundResource(R.color.colorYellow)
        }
        if (celTemp <= 16.00) {
            mainActivityLayout.setBackgroundResource(R.color.colorPrimary)
        }
        // u2103 is celcius
        tvResult.text =("$celTemp \u2103")
    }
}