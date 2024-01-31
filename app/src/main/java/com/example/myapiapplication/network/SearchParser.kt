package com.example.myapiapplication.network

import android.util.Log
import android.util.Xml
import com.example.myapiapplication.data.Restaurant
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException
import java.io.InputStream

class SearchParser {
    private val ns: String? = null
    private val TAG = "MainActivity"

    companion object{
        val ITEM_TAG = "item"
        val TITLE_TAG = "title"
        val CATEGORY_TAG = "category"
        val ADDR_TAG = "address"
    }
    @Throws(XmlPullParserException::class, IOException::class)
    fun parse(inputStream: InputStream?) : ArrayList<Restaurant> {

        inputStream.use { inputStream ->
            val parser : XmlPullParser = Xml.newPullParser()

            /*Parser 의 동작 정의, next() 호출 전 반드시 호출 필요*/
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)

            /* Paring 대상이 되는 inputStream 설정 */
            parser.setInput(inputStream, null)

            /*Parsing 대상 태그의 상위 태그까지 이동*/
            while(parser.name != "channel") {
                parser.next()
            }
            return readResultList(parser)
        }
    }


    @Throws(XmlPullParserException::class, IOException::class)
    private fun readResultList(parser: XmlPullParser) : ArrayList<Restaurant> {
        val restaurants = mutableListOf<Restaurant>() //리스트

        parser.require(XmlPullParser.START_TAG, ns, "channel")
        while(parser.next() != XmlPullParser.END_TAG){
            if(parser.eventType != XmlPullParser.START_TAG){
                continue
            }
            if(parser.name == ITEM_TAG){
                restaurants.add(readRestaurantItem(parser))
            }else{
                skip(parser)
            }
        }
        return restaurants as ArrayList<Restaurant>
    }


    @Throws(XmlPullParserException::class, IOException::class)
    private fun readRestaurantItem(parser: XmlPullParser) : Restaurant {
        parser.require(XmlPullParser.START_TAG, ns, ITEM_TAG)
        var title : String? = null
        var category : String? = null
        var address : String? = null

        while(parser.next() != XmlPullParser.END_TAG){
            if(parser.eventType != XmlPullParser.START_TAG){
                continue
            }
            when(parser.name){
                TITLE_TAG -> title = readTextInTag(parser, TITLE_TAG)
                CATEGORY_TAG -> category = readTextInTag(parser, CATEGORY_TAG)
                ADDR_TAG -> address = readTextInTag(parser, ADDR_TAG)
                else -> skip(parser)
            }
        }
        Log.d(TAG, "${title}")
        return Restaurant(title, category, address)
    }



    @Throws(IOException::class, XmlPullParserException::class)
    private fun readTextInTag (parser: XmlPullParser, tag: String): String {
        parser.require(XmlPullParser.START_TAG, ns, tag)
        var text = ""
        if (parser.next() == XmlPullParser.TEXT) {
            text = parser.text
            parser.nextTag()
        }
        parser.require(XmlPullParser.END_TAG, ns, tag)
        return text
    }


    @Throws(XmlPullParserException::class, IOException::class)
    private fun skip(parser: XmlPullParser) {
        if (parser.eventType != XmlPullParser.START_TAG) {
            throw IllegalStateException()
        }
        var depth = 1
        while (depth != 0) {
            when (parser.next()) {
                XmlPullParser.END_TAG -> depth--
                XmlPullParser.START_TAG -> depth++
            }
        }
    }
}