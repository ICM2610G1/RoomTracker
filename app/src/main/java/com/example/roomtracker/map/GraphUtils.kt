package com.example.roomtracker.map

import com.google.android.gms.maps.model.LatLng
import kotlin.math.*

object GraphUtils {

    /**
     * Encuentra el nodo más cercano que tenga conexiones
     */
    fun nearestNode(
        nodes: Map<String, LatLng>,
        point: LatLng,
        graph: Graph
    ): String {

        var closestNode: String? = null
        var minDistance = Double.MAX_VALUE

        for ((nodeId, nodeLocation) in nodes) {

            val neighbors = graph[nodeId]

            // ignorar nodos sin edges
            if (neighbors.isNullOrEmpty()) continue

            val distance = distanceMeters(
                nodeLocation,
                point
            )

            if (distance < minDistance) {
                minDistance = distance
                closestNode = nodeId
            }
        }

        return closestNode ?: nodes.keys.first()
    }

    /**
     * Distancia Haversine en metros
     */
    fun distanceMeters(
        a: LatLng,
        b: LatLng
    ): Double {

        val earthRadius = 6371000.0

        val lat1 = Math.toRadians(a.latitude)
        val lat2 = Math.toRadians(b.latitude)

        val dLat = lat2 - lat1
        val dLon = Math.toRadians(b.longitude - a.longitude)

        val h = sin(dLat / 2).pow(2) +
                cos(lat1) *
                cos(lat2) *
                sin(dLon / 2).pow(2)

        val c = 2 * atan2(
            sqrt(h),
            sqrt(1 - h)
        )

        return earthRadius * c
    }
}