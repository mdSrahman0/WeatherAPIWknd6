package com.example.weatherapiwknd6

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.View.VISIBLE
import android.widget.RadioButton
import com.example.weatherapiwknd6.model.datasource.local.WeatherResponse
import kotlinx.android.synthetic.main.activity_main.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.lang.Boolean.FALSE
import java.lang.Boolean.TRUE

class MainActivity : AppCompatActivity() {

    val BASE_URL = "http://api.openweathermap.org/data/2.5/"
    val APPID = "c467f3cd5ea422c1324671a57e10f64a"
    private var weatherAPI: WeatherAPI? = null
    //lateinit var radiogroup : RadioGroup
    lateinit var rbFahrenheit : RadioButton
    lateinit var rbCelsius : RadioButton
    private var myPreferences = "myPrefs"
    private var ZIP = "zip" // key for shared pref
    private var EMPTY = 0   // default val for shared pref

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // these two radio buttons are invisible by default, so the user doesn't see them until they enter
        // their very first zip code.
        rbFahrenheit = findViewById(R.id.rbFahrenheit)
        rbCelsius = findViewById(R.id.rbCelsius)

        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        weatherAPI = retrofit.create(WeatherAPI::class.java)

        val sharedPreferences = getSharedPreferences(myPreferences, Context.MODE_PRIVATE)

        // check if shared preference exists. if so, fetch weather info
        if(sharedPreferences.getInt(ZIP, EMPTY) != EMPTY) {
            val zip = sharedPreferences.getInt(ZIP, 0)
            getCurrentWeather(zip)
        }
        else {
            btnSetZip.setOnClickListener {
                val editor = sharedPreferences.edit()
                val etZip = etZip.text.toString().toInt()
                editor.putInt(ZIP, etZip)
                editor.apply()
                Log.d("TAG", etZip.toString())
                getCurrentWeather(etZip)
            }
        }
    }

    // I will still allow the user to set a new zip code and store that info inside shared pref
    fun onClick(view: View) {
        val sharedPreferences = getSharedPreferences(myPreferences, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        val etZip = etZip.text.toString().toInt()
        editor.putInt(ZIP, etZip)
        editor.apply()
        Log.d("TAG", etZip.toString())
        rbFahrenheit.isChecked = FALSE
        rbCelsius.isChecked = FALSE
        getCurrentWeather(etZip)
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
            }

            override fun onFailure(call: Call<WeatherResponse>, t: Throwable) {
                //tvResult.text = t.message
                tvResult.text = "NOT A VALID ZIP CODE"
            }
        })
    }

    fun getFahrenheit(currentTemp : Float) {
        var fahrTemp = (9.0/5.0)*(currentTemp - 273.0) + 32.0
        fahrTemp = String.format("%.2f", fahrTemp).toDouble()
        if (fahrTemp > 60.00) {
            mainActivityLayout.setBackgroundResource(R.color.colorYellow)
        }
        if (fahrTemp <= 60.00) {
            mainActivityLayout.setBackgroundResource(R.color.colorPrimary)
        }
        tvResult.text = fahrTemp.toString()
    }

    fun getCelsius(currentTemp: Float) {
        var celTemp = currentTemp - 273.0
        celTemp = String.format("%.2f", celTemp).toDouble()
        if (celTemp > 16.00) {
            mainActivityLayout.setBackgroundResource(R.color.colorYellow)
        }
        if (celTemp <= 16.00) {
            mainActivityLayout.setBackgroundResource(R.color.colorPrimary)
        }
        tvResult.text = celTemp.toString()
    }
}