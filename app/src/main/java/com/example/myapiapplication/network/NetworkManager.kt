package com.example.myapiapplication.network

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import com.example.myapiapplication.R
import com.example.myapiapplication.data.Restaurant
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.lang.StringBuilder
import java.net.HttpURLConnection
import java.net.URL
import kotlin.jvm.Throws

class NetworkManager(val context : Context){

    private val TAG = "NetworkManager"

    val openApiUrl by lazy {

        context.resources.getString(R.string.naver_url)
    }
    @Throws(IOException::class)
    fun downloadXml(keyword: String) : ArrayList<Restaurant> {
        var restaurants : ArrayList<Restaurant>? = null

        val inputStream = downloadUrl( openApiUrl + keyword + "&display=5")

        /*Parser 생성 및 parsing 수행*/
        val parser = SearchParser()
        restaurants = parser.parse(inputStream)
        Log.d(TAG, "input is here")

        return restaurants
    }

    @Throws(IOException::class)
    private fun downloadUrl(urlString: String) : InputStream? {
        val url = URL(urlString)


        val cliedId = context.resources.getString(R.string.client_id)
        val clientSecret = context.resources.getString(R.string.client_secret)

        return (url.openConnection() as? HttpURLConnection)?.run {
            readTimeout = 5000
            connectTimeout = 5000
            requestMethod = "GET"
            doInput = true

            /*Naver ClientID/Secret 을 HTTP Header Property에 저장*/
            setRequestProperty("X-Naver-Client-Id", cliedId)
            setRequestProperty("X-Naver-Client-Secret", clientSecret)

            connect()
            inputStream
        }
    }

    // InputStream 을 String 으로 변환
    private fun readStreamToString(iStream : InputStream?) : String {
        val resultBuilder = StringBuilder()

        val inputStreamReader = InputStreamReader(iStream)
        val bufferedReader = BufferedReader(inputStreamReader)

        var readLine : String? = bufferedReader.readLine()
        while (readLine != null) {
            resultBuilder.append(readLine + System.lineSeparator())
            readLine = bufferedReader.readLine()
        }

        bufferedReader.close()
        return resultBuilder.toString()
    }


    // InputStream 을 Bitmap 으로 변환
    private fun readStreamToImage(iStream: InputStream?) : Bitmap {
        val bitmap = BitmapFactory.decodeStream(iStream)
        return bitmap
    }
}