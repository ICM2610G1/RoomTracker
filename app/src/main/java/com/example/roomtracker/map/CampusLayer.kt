package com.example.roomtracker.map

import android.content.Context
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PolygonOptions
import com.google.android.gms.maps.model.PolylineOptions
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader

data class CampusData(
    val polygons: List<PolygonOptions>,
    val paths: List<PolylineOptions>,
    val pois: List<Pair<String, LatLng>>
)

object CampusLayer {

    fun loadCampus(context: Context): CampusData {

        val inputStream = context.assets.open("campus_updated.geojson")
        val reader = BufferedReader(InputStreamReader(inputStream))
        val jsonString = reader.readText()
        reader.close()

        val json = JSONObject(jsonString)
        val features = json.getJSONArray("features")

        val polygons = mutableListOf<PolygonOptions>()
        val paths = mutableListOf<PolylineOptions>()
        val pois = mutableListOf<Pair<String, LatLng>>()

        for (i in 0 until features.length()) {

            val feature = features.getJSONObject(i)
            val geometry = feature.getJSONObject("geometry")
            val properties = feature.optJSONObject("properties")
            val type = geometry.getString("type")

            when (type) {

                "Polygon" -> {
                    val coordinates = geometry.getJSONArray("coordinates")
                    val polygonOptions = PolygonOptions()

                    val outerRing = coordinates.getJSONArray(0)

                    for (j in 0 until outerRing.length()) {
                        val point = outerRing.getJSONArray(j)
                        val lng = point.getDouble(0)
                        val lat = point.getDouble(1)
                        polygonOptions.add(LatLng(lat, lng))
                    }

                    polygonOptions
                        .strokeWidth(5f)
                        .strokeColor(0xFF1976D2.toInt())
                        .fillColor(0x332196F3)

                    polygons.add(polygonOptions)
                }

                "LineString" -> {
                    val coordinates = geometry.getJSONArray("coordinates")
                    val polylineOptions = PolylineOptions()

                    for (j in 0 until coordinates.length()) {
                        val point = coordinates.getJSONArray(j)
                        val lng = point.getDouble(0)
                        val lat = point.getDouble(1)
                        polylineOptions.add(LatLng(lat, lng))
                    }

                    polylineOptions
                        .width(6f)
                        .color(0xFF4CAF50.toInt())

                    paths.add(polylineOptions)
                }

                "Point" -> {
                    if (properties != null &&
                        properties.optString("nodeKind") == "poi"
                    ) {
                        val coordinates = geometry.getJSONArray("coordinates")
                        val lng = coordinates.getDouble(0)
                        val lat = coordinates.getDouble(1)

                        val name = properties.optString("name", "POI")

                        pois.add(name to LatLng(lat, lng))
                    }
                }
            }
        }

        return CampusData(
            polygons = polygons,
            paths = paths,
            pois = pois
        )
    }
}