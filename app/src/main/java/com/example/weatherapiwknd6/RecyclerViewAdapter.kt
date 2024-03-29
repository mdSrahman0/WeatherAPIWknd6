package com.example.weatherapiwknd6

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.example.weatherapiwknd6.model.datasource.local.hourlyweather.X

class RecyclerViewAdapter (val hourList : List<X>, val degreeType : String): RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder>(){
    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): ViewHolder {
        val view = LayoutInflater.from(p0.context).inflate(R.layout.item_view, p0, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return hourList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val x: X = hourList[position]
        holder.tvDay.text = x.dtTxt
        if(degreeType == "F") {
            val temp = getFahrenheit(x.main.temp.toFloat())
            holder.tvTemp.text = ("$temp \u2109")
        }
        if(degreeType == "C") {
            val temp = getCelsius(x.main.temp.toFloat())
            holder.tvTemp.text = ("$temp \u2103")
        }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvDay : TextView = itemView.findViewById(R.id.tvDay)
        val tvTemp : TextView = itemView.findViewById(R.id.tvHourlyTemp)
    }
}