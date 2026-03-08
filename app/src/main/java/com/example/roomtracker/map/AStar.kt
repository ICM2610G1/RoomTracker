package com.example.roomtracker.map

import java.util.PriorityQueue

object AStar {

    fun findPath(
        graph: Graph,
        start: String,
        goal: String
    ): List<String> {

        val openSet = PriorityQueue<Pair<String, Double>>(compareBy { it.second })

        val cameFrom = mutableMapOf<String, String>()

        val gScore = mutableMapOf<String, Double>().withDefault { Double.POSITIVE_INFINITY }

        gScore[start] = 0.0

        openSet.add(start to 0.0)

        while (openSet.isNotEmpty()) {

            val current = openSet.poll().first

            if (current == goal) {

                val path = mutableListOf<String>()
                var node = goal

                while (cameFrom.containsKey(node)) {
                    path.add(node)
                    node = cameFrom[node]!!
                }

                path.add(start)

                return path.reversed()
            }

            val neighbors = graph[current] ?: emptyList()

            for (edge in neighbors) {

                val tentative = gScore.getValue(current) + edge.cost

                if (tentative < gScore.getValue(edge.id)) {

                    cameFrom[edge.id] = current
                    gScore[edge.id] = tentative

                    openSet.add(edge.id to tentative)
                }
            }
        }

        return emptyList()
    }
fun findPathAvoiding(
    graph: Graph,
    start: String,
    goal: String,
    blockedEdges: Set<Pair<String,String>>
): List<String> {

    val openSet = PriorityQueue<Pair<String, Double>>(compareBy { it.second })

    val cameFrom = mutableMapOf<String, String>()
    val gScore = mutableMapOf<String, Double>().withDefault { Double.POSITIVE_INFINITY }

    gScore[start] = 0.0
    openSet.add(start to 0.0)

    while (openSet.isNotEmpty()) {

        val current = openSet.poll().first

        if (current == goal) {

            val path = mutableListOf<String>()
            var node = goal

            while (cameFrom.containsKey(node)) {
                path.add(node)
                node = cameFrom[node]!!
            }

            path.add(start)
            return path.reversed()
        }

        for (edge in graph[current] ?: emptyList()) {

            if (blockedEdges.contains(current to edge.id)) continue

            val tentative = gScore.getValue(current) + edge.cost

            if (tentative < gScore.getValue(edge.id)) {

                cameFrom[edge.id] = current
                gScore[edge.id] = tentative
                openSet.add(edge.id to tentative)
            }
        }
    }

    return emptyList()
}
    fun findPathWithPenalty(
        graph: Graph,
        start: String,
        goal: String,
        penaltyEdges: Map<Pair<String,String>, Double>
    ): List<String> {

        val openSet = PriorityQueue<Pair<String, Double>>(compareBy { it.second })

        val cameFrom = mutableMapOf<String, String>()
        val gScore = mutableMapOf<String, Double>().withDefault { Double.POSITIVE_INFINITY }

        gScore[start] = 0.0
        openSet.add(start to 0.0)

        while (openSet.isNotEmpty()) {

            val current = openSet.poll().first

            if (current == goal) {

                val path = mutableListOf<String>()
                var node = goal

                while (cameFrom.containsKey(node)) {
                    path.add(node)
                    node = cameFrom[node]!!
                }

                path.add(start)
                return path.reversed()
            }

            for (edge in graph[current] ?: emptyList()) {

                val penalty =
                    penaltyEdges[current to edge.id] ?: 0.0

                val cost = edge.cost + penalty

                val tentative = gScore.getValue(current) + cost

                if (tentative < gScore.getValue(edge.id)) {

                    cameFrom[edge.id] = current
                    gScore[edge.id] = tentative

                    openSet.add(edge.id to tentative)
                }
            }
        }

        return emptyList()
    }
}