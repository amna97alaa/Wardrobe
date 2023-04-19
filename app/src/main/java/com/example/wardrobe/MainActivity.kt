package com.example.wardrobe

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import com.example.wardrobe.data.MainResponse
import com.example.wardrobe.databinding.WardropeScreenBinding
import com.google.gson.Gson
import okhttp3.*
import java.io.IOException

class MainActivity : AppCompatActivity() {
    lateinit var binding: WardropeScreenBinding
    private lateinit var sharedPreferences: SharedPreferences
    val client = OkHttpClient()
    private val winterClothes = mutableListOf<Int>()
    private val summerClothes = mutableListOf<Int>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = WardropeScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)
        sharedPreferences =
            applicationContext.getSharedPreferences("MY_SHARED", Context.MODE_PRIVATE)
        makeRequestUsingOKHTTP()
    }

    init {
        winterClothes.apply {
            add(0, R.drawable.coat)
            add(1, R.drawable.coat2)
            add(2, R.drawable.coat5)
            add(3, R.drawable.purplecoat)
            add(4, R.drawable.redcoat)
            add(5, R.drawable.wintercoat)
            add(6, R.drawable.bluecoat2)

        }
        summerClothes.apply {
            add(0, R.drawable.dress10summer)
            add(1, R.drawable.dress3)
            add(2, R.drawable.dress5)
            add(3, R.drawable.outfit)
            add(4,R.drawable.dress_summer)
            add(5, R.drawable.dress7)
            add(6, R.drawable.pinkdress)
        }
    }

    fun View.hide() {
        this.visibility = View.GONE
    }

    fun View.show() {
        this.visibility = View.VISIBLE
    }

    private fun showNoNetworkError() {
        binding.lottieNoNetwork.show()
        binding.groupNoNetwork.hide()

    }

    private fun getImageId(temperature: Int): Int {
        return when (temperature) {
            in Int.MIN_VALUE..WINTER_MAX_TEMP -> winterClothes.random()
            else -> summerClothes.random()
        }
    }

    fun checkIsImageSaved(): Boolean {
        return sharedPreferences.contains("KEY_IMAGE")
    }

    fun checkIsDateSaved(date: String?): Boolean {
        return (date == getSavedDate())
    }

    fun setWeatherIcon(isDay: String) {
        if (isDay == "no") {
            binding.imageViewWeatherStatus.setImageResource(R.drawable.cloudy_moon)
        } else {
            binding.imageViewWeatherStatus.setImageResource(R.drawable.cloudy_sun)
        }
    }

    fun saveDate(date: String) {
        val editor = sharedPreferences.edit()
        editor.putString("KEY_DATE", date)
        editor.apply()
    }

    fun saveImage(drawableId: Int) {
        val editor = sharedPreferences.edit()
        editor.putInt("KEY_IMAGE", drawableId)
        editor.apply()
    }

    fun getSavedImage(): Int {
        return sharedPreferences.getInt("KEY_IMAGE", 0)
    }

    fun getSavedDate(): String? {
        return sharedPreferences.getString("KEY_DATE", "")
    }

    private fun makeRequestUsingOKHTTP() {
        val url =
            "http://api.weatherstack.com/current?access_key=${BuildConfig.API_KEY}&query=Baghdad"
        val request = Request.Builder()
            .url(url)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.v("ActivityMain", "$e.message")
                showNoNetworkError()
            }

            override fun onResponse(call: Call, response: Response) {
                response.body?.string().let { jsonString ->
                    val weatherResult = Gson().fromJson(jsonString, MainResponse::class.java)
                    Log.i("Amna", weatherResult.toString())
                    runOnUiThread {
                        val isDay = weatherResult.current.is_day
                        setWeatherIcon(isDay)
                        val temperature = weatherResult.current.temperature
                        binding.textViewWeatherDegree.text = temperature.toString()
                        val status = weatherResult.current.weather_descriptions[0]
                        binding.textViewWeatherStatus.text = status
                        val city = weatherResult.location.region
                        binding.textViewCity.text = city
                        val currentDate = weatherResult.location.localtime.take(10)
                        saveDate(currentDate)
                        if (checkIsDateSaved(currentDate)) {
                            Log.i("Amna",checkIsImageSaved().toString())
                            if (checkIsImageSaved()) {
                                binding.imageViewSuggestedOutfit.setImageResource(getSavedImage())
                            } else {
                                binding.imageViewSuggestedOutfit.setImageResource(
                                    getImageId(
                                        temperature
                                    )
                                )
                                saveImage(getImageId(temperature))
                            }
                        } else {
                            binding.imageViewSuggestedOutfit.setImageResource(getImageId(temperature))
                            saveImage(getImageId(temperature))
                        }
                    }
                }
            }
        })
    }

    companion object {
        const val WINTER_MAX_TEMP = 15
    }
}