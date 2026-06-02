package com.example.roomtracker.map

import android.content.Context
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PolygonOptions
import com.google.android.gms.maps.model.PolylineOptions
import org.json.JSONObject
import java.io.File

// ─── Modelos de POI ───────────────────────────────────────────────────────────

data class PoiInfo(
    val nombre: String,
    val tipo: String?        = null,   // puede ser null si aún no se asignó
    val descripcion: String? = null
)

data class MapPoi(
    val nodeId: String,
    val displayName: String,   // nombre real si existe, sino el nodeId crudo
    val location: LatLng,
    val info: PoiInfo? = null
)

// ─── CampusData ───────────────────────────────────────────────────────────────

data class CampusData(
    val polygons: List<PolygonOptions>,
    val paths: List<PolylineOptions>,
    val pois: List<MapPoi>
)

// ─── CampusLayer ──────────────────────────────────────────────────────────────

object CampusLayer {

    /**
     * Carga el diccionario nodo → PoiInfo desde pois.json (guardado en filesDir).
     * Retorna vacío si el archivo no existe o tiene error.
     */
    fun loadPois(context: Context): Map<String, PoiInfo> {
        val file = File(context.filesDir, "pois.json")
        if (!file.exists()) return emptyMap()
        return try {
            val json = JSONObject(file.readText())
            val result = mutableMapOf<String, PoiInfo>()
            val keys = json.keys()
            while (keys.hasNext()) {
                val nodeId = keys.next()
                val obj = json.getJSONObject(nodeId)
                result[nodeId] = PoiInfo(
                    nombre      = obj.getString("nombre"),
                    tipo        = obj.optString("tipo").ifBlank { null },
                    descripcion = obj.optString("descripcion").ifBlank { null }
                )
            }
            result
        } catch (e: Exception) {
            android.util.Log.e("CAMPUS", "Error cargando pois.json: ${e.message}")
            emptyMap()
        }
    }

    /**
     * Carga campus_updated.geojson y enriquece los POIs con la info de poiInfoMap.
     * Los nodos sin entrada en poiInfoMap conservan su ID como displayName.
     */
    fun loadCampus(
        context: Context,
        poiInfoMap: Map<String, PoiInfo> = emptyMap()
    ): CampusData {

        val localFile = File(context.filesDir, "campus_updated.geojson")
        if (!localFile.exists()) return CampusData(emptyList(), emptyList(), emptyList())
        val jsonString = localFile.readText()

        val json = JSONObject(jsonString)
        val features = json.getJSONArray("features")

        val polygons = mutableListOf<PolygonOptions>()
        val paths    = mutableListOf<PolylineOptions>()
        val pois     = mutableListOf<MapPoi>()

        for (i in 0 until features.length()) {

            val feature    = features.getJSONObject(i)
            val geometry   = feature.getJSONObject("geometry")
            val properties = feature.optJSONObject("properties")
            val type       = geometry.getString("type")

            when (type) {

                "Polygon" -> {
                    val coordinates  = geometry.getJSONArray("coordinates")
                    val polygonOptions = PolygonOptions()
                    val outerRing    = coordinates.getJSONArray(0)
                    for (j in 0 until outerRing.length()) {
                        val point = outerRing.getJSONArray(j)
                        polygonOptions.add(LatLng(point.getDouble(1), point.getDouble(0)))
                    }
                    polygonOptions
                        .strokeWidth(5f)
                        .strokeColor(0xFF1976D2.toInt())
                        .fillColor(0x332196F3)
                    polygons.add(polygonOptions)
                }

                "LineString" -> {
                    val coordinates    = geometry.getJSONArray("coordinates")
                    val polylineOptions = PolylineOptions()
                    for (j in 0 until coordinates.length()) {
                        val point = coordinates.getJSONArray(j)
                        polylineOptions.add(LatLng(point.getDouble(1), point.getDouble(0)))
                    }
                    polylineOptions.width(6f).color(0xFF4CAF50.toInt())
                    paths.add(polylineOptions)
                }

                "Point" -> {
                    if (properties != null &&
                        properties.optString("nodeKind") == "poi"
                    ) {
                        val coordinates = geometry.getJSONArray("coordinates")
                        val lat = coordinates.getDouble(1)
                        val lng = coordinates.getDouble(0)

                        // El nodeId es el campo "id" del feature GeoJSON
                        val nodeId = feature.optString("id", "POI_$i")
                        val info   = poiInfoMap[nodeId]

                        // displayName: nombre real si existe, sino el nodeId crudo
                        val displayName = info?.nombre ?: nodeId

                        pois.add(MapPoi(nodeId, displayName, LatLng(lat, lng), info))
                    }
                }
            }
        }

        return CampusData(polygons = polygons, paths = paths, pois = pois)
    }

    fun loadGraphCoordinates(context: Context): Map<String, LatLng> {

        val localFile = File(context.filesDir, "campus_updated.geojson")
        if (!localFile.exists()) return emptyMap()
        val json = localFile.readText()

        val jsonObject = JSONObject(json)
        val features   = jsonObject.getJSONArray("features")
        val coordinates = mutableMapOf<String, LatLng>()

        for (i in 0 until features.length()) {
            val feature    = features.getJSONObject(i)
            val properties = feature.getJSONObject("properties")
            if (properties.optString("type") == "node") {
                val id       = feature.getString("id")
                val geometry = feature.getJSONObject("geometry")
                val coords   = geometry.getJSONArray("coordinates")
                coordinates[id] = LatLng(coords.getDouble(1), coords.getDouble(0))
            }
        }

        return coordinates
    }

    /**
     * Carga edificios.json desde filesDir y retorna un mapa nodeId → EdificioDetalle.
     * El JSON es un array de objetos; cada objeto tiene "nodos": [id1, id2, ...] para
     * que varios puntos de entrada del mismo edificio compartan el mismo detalle.
     */
    fun loadEdificios(context: Context): Map<String, EdificioDetalle> {
        val file = File(context.filesDir, "edificios.json")
        if (!file.exists()) return emptyMap()
        return try {
            val array = org.json.JSONArray(file.readText())
            val result = mutableMapOf<String, EdificioDetalle>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)

                // Parsear pisos
                val pisoArray = obj.getJSONArray("pisos")
                val pisos = mutableListOf<PisoDetalle>()
                for (p in 0 until pisoArray.length()) {
                    val pisoObj = pisoArray.getJSONObject(p)
                    val espacioArray = pisoObj.getJSONArray("espacios")
                    val espacios = (0 until espacioArray.length()).map { j ->
                        val e = espacioArray.getJSONObject(j)
                        EspacioDetalle(
                            codigo = e.getString("codigo"),
                            nombre = e.getString("nombre"),
                            tipo   = e.optString("tipo", "otro")
                        )
                    }
                    val serviciosArray = pisoObj.optJSONArray("servicios")
                    val servicios = if (serviciosArray != null)
                        (0 until serviciosArray.length()).map { j -> serviciosArray.getString(j) }
                    else emptyList()

                    pisos.add(PisoDetalle(
                        numero   = pisoObj.getString("numero"),
                        nombre   = pisoObj.getString("nombre"),
                        espacios = espacios,
                        servicios = servicios
                    ))
                }

                val detalle = EdificioDetalle(
                    nombre      = obj.getString("nombre"),
                    descripcion = obj.optString("descripcion", ""),
                    imagenUrl   = obj.optString("imagen_url").ifBlank { null },
                    pisos       = pisos
                )

                // Asociar el mismo detalle a todos los nodos de entrada del edificio
                val nodosArray = obj.optJSONArray("nodos")
                if (nodosArray != null) {
                    for (n in 0 until nodosArray.length()) result[nodosArray.getString(n)] = detalle
                }
            }
            result
        } catch (e: Exception) {
            android.util.Log.e("CAMPUS", "Error cargando edificios.json: ${e.message}")
            emptyMap()
        }
    }

    fun loadEdgeGeometry(context: Context): Map<Pair<String, String>, List<LatLng>> {

        val localFile = File(context.filesDir, "edge_geometry.json")
        if (!localFile.exists()) return emptyMap()
        val json       = localFile.readText()
        val jsonObject = JSONObject(json)
        val edgeGeometry = mutableMapOf<Pair<String, String>, List<LatLng>>()

        val keys = jsonObject.keys()
        while (keys.hasNext()) {
            val key   = keys.next()
            val parts = key.split("|")
            if (parts.size != 2) continue
            val coordsArray = jsonObject.getJSONArray(key)
            val points = mutableListOf<LatLng>()
            for (i in 0 until coordsArray.length()) {
                val pt = coordsArray.getJSONArray(i)
                points.add(LatLng(pt.getDouble(1), pt.getDouble(0)))
            }
            edgeGeometry[parts[0] to parts[1]] = points
        }

        return edgeGeometry
    }
}
