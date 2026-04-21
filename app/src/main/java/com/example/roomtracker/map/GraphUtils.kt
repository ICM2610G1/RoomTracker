package com.example.roomtracker.map

import com.google.android.gms.maps.model.LatLng
import kotlin.math.*

object GraphUtils {

    /**
     * Encuentra el nodo más cercano considerando también la dirección al destino.
     * Si no se pasa destino, usa solo distancia euclidiana.
     */
    fun nearestNode(
        nodes: Map<String, LatLng>,
        point: LatLng,
        graph: Graph,
        destination: LatLng? = null
    ): String {

        // Candidatos con conexiones, ordenados por distancia al punto
        val candidates = nodes
            .filter { (nodeId, _) -> !graph[nodeId].isNullOrEmpty() }
            .map { (nodeId, nodeLocation) ->
                nodeId to distanceMeters(nodeLocation, point)
            }
            .sortedBy { it.second }
            .take(8)

        if (candidates.isEmpty()) return nodes.keys.first()

        // Si no hay destino, devolver el más cercano
        if (destination == null) return candidates.first().first

        // Con destino: elegir el candidato con menor (distancia_al_punto + distancia_al_destino).
        // El peso 0.5 sobre distToDest descarta nodos que están "detrás" del usuario respecto
        // al destino sin sacrificar la prioridad de cercanía al punto de snap.
        // (0.3 era demasiado débil: un nodo a 5 m al oeste ganaba sobre uno a 20 m al este
        //  aunque el destino estuviera 150 m más a la derecha del nodo oeste.)
        return candidates.minByOrNull { (nodeId, distToPoint) ->
            val nodeLocation = nodes[nodeId]!!
            val distToDest = distanceMeters(nodeLocation, destination)
            distToPoint + distToDest * 0.5
        }?.first ?: candidates.first().first
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
