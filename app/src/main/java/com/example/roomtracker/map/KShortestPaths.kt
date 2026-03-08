package com.example.roomtracker.map
import java.util.PriorityQueue

object KShortestPaths {

    fun findKPaths(
        graph: Graph,
        start: String,
        goal: String,
        k: Int
    ): List<List<String>> {

        val paths = mutableListOf<List<String>>()

        val penalties = mutableMapOf<Pair<String,String>, Double>()

        repeat(k) {

            val path = AStar.findPathWithPenalty(
                graph,
                start,
                goal,
                penalties
            )

            if (path.isEmpty()) return paths

            paths.add(path)

            // penalizar aristas usadas
            for (i in 0 until path.size - 1) {

                val edge = path[i] to path[i + 1]

                penalties[edge] =
                    (penalties[edge] ?: 0.0) + 40.0
            }
        }

        return paths
    }
}