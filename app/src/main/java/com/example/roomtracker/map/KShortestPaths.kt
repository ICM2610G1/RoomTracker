package com.example.roomtracker.map
import java.util.PriorityQueue
import com.google.android.gms.maps.model.LatLng

object KShortestPaths {

    fun findKPaths(
        graph: Graph,
        graphCoordinates: Map<String, LatLng>,
        start: String,
        goal: String,
        k: Int
    ): List<List<String>> {

        val paths = mutableListOf<List<String>>()

        val penalties = mutableMapOf<Pair<String, String>, Double>()

        repeat(k) {

            val path = AStar.findPathWithPenalty(
                graph,
                graphCoordinates,
                start,
                goal,
                penalties
            )

            if (path.isEmpty()) return paths

            // evitar rutas duplicadas o falsas
            if (paths.contains(path)) return paths

            paths.add(path)

            // Penalizar aristas usadas.
            // La penalización base de 200 m es suficiente para rutas cortas,
            // pero en rutas largas un arco de 300 m + 200 m = 500 m puede
            // seguir siendo competitivo frente a un arco alternativo de 150 m.
            // Usar max(200, costo × 3) garantiza que cada arco de la ruta
            // anterior sea al menos 3× más caro que su valor real, obligando
            // a A* a explorar caminos genuinamente distintos.
            for (i in 0 until path.size - 1) {
                val a    = path[i]
                val b    = path[i + 1]
                val edge = a to b

                val originalCost = graph[a]?.find { it.id == b }?.cost ?: 0.0
                val penalty      = maxOf(200.0, originalCost * 3.0)

                penalties[edge] = (penalties[edge] ?: 0.0) + penalty
            }
        }

        return paths
    }
}