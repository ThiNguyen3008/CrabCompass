package com.example.mdtravel.data

import android.content.Context
import com.example.mdtravel.model.Destination
import org.json.JSONArray
import java.io.BufferedReader

object DestinationRepository {

    fun getAll(context: Context): List<Destination> {
        val list = mutableListOf<Destination>()
        try {
            val inputStream = context.assets.open("destinations.json")
            val bufferedReader = BufferedReader(inputStream.reader())
            val jsonText = bufferedReader.use { it.readText() }
            val jsonArray = JSONArray(jsonText)

            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                val destination = Destination(
                    name = obj.getString("name"),
                    city = obj.getString("city"),
                    price = obj.getInt("price"),
                    season = obj.getString("season"),
                    interest = obj.getString("interest"),
                    description = obj.getString("description"),
                    link = obj.getString("link"),
                    lat = obj.getDouble("lat"),
                    lng = obj.getDouble("lng"),
                    rating = obj.getDouble("rating"),
                    ratingCount = obj.getInt("ratingCount")
                )
                list.add(destination)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list
    }
}
