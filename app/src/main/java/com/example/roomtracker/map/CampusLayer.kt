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

        val localFile = java.io.File(context.filesDir, "campus_updated.geojson")
        if (!localFile.exists()) return CampusData(emptyList(), emptyList(), emptyList())
        val jsonString = localFile.readText()

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

                        val name = properties.optString("name", "")
                            .ifBlank { feature.optString("id", "POI $i") }

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

    fun loadGraphCoordinates(context: Context): Map<String, LatLng> {

        val localFile = java.io.File(context.filesDir, "campus_updated.geojson")
        if (!localFile.exists()) return emptyMap()
        val json = localFile.readText()

        val jsonObject = JSONObject(json)
        val features = jsonObject.getJSONArray("features")

        val coordinates = mutableMapOf<String, LatLng>()

        for (i in 0 until features.length()) {

            val feature = features.getJSONObject(i)
            val properties = feature.getJSONObject("properties")

            if (properties.optString("type") == "node") {

                val id = feature.getString("id")

                val geometry = feature.getJSONObject("geometry")
                val coords = geometry.getJSONArray("coordinates")

                val lng = coords.getDouble(0)
                val lat = coords.getDouble(1)

                coordinates[id] = LatLng(lat, lng)
            }
        }

        return coordinates
    }

    fun loadEdgeGeometry(context: Context): Map<Pair<String, String>, List<LatLng>> {

        val localFile = java.io.File(context.filesDir, "edge_geometry.json")
        if (!localFile.exists()) return emptyMap()
        val json = localFile.readText()
        val jsonObject = JSONObject(json)
        val edgeGeometry = mutableMapOf<Pair<String, String>, List<LatLng>>()

        val keys = jsonObject.keys()
        while (keys.hasNext()) {
            val key = keys.next()
            val parts = key.split("|")
            if (parts.size != 2) continue
            val startId = parts[0]
            val endId = parts[1]
            val coordsArray = jsonObject.getJSONArray(key)
            val points = mutableListOf<LatLng>()
            for (i in 0 until coordsArray.length()) {
                val pt = coordsArray.getJSONArray(i)
                points.add(LatLng(pt.getDouble(1), pt.getDouble(0)))
            }
            edgeGeometry[startId to endId] = points
        }

        return edgeGeometry
    }
}