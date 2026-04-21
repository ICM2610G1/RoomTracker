package com.example.roomtracker.map

import java.util.PriorityQueue
import com.google.android.gms.maps.model.LatLng

object AStar {

    /**
     * Peso de la heurística.
     *
     * weight = 1.0 → A* óptimo clásico: siempre encuentra la ruta más corta.
     * Los costos del grafo ahora usan la distancia real de caminata (path length)
     * y la heurística es la distancia en línea recta (Haversine), que siempre es
     * ≤ al costo real → la heurística es admisible y A* es óptimo.
     */
    private const val HEURISTIC_WEIGHT = 1.0

    // ── API pública ───────────────────────────────────────────────────────────

    fun findPath(
        graph: Graph,
        graphCoordinates: Map<String, LatLng>,
        start: String,
        goal: String
    ): List<String> = runAStar(graph, graphCoordinates, start, goal)

    fun findPathAvoiding(
        graph: Graph,
        graphCoordinates: Map<String, LatLng>,
        start: String,
        goal: String,
        blockedEdges: Set<Pair<String, String>>
    ): List<String> = runAStar(
        graph, graphCoordinates, start, goal,
        blockedEdges = blockedEdges
    )

    fun findPathWithPenalty(
        graph: Graph,
        graphCoordinates: Map<String, LatLng>,
        start: String,
        goal: String,
        penaltyEdges: Map<Pair<String, String>, Double>
    ): List<String> = runAStar(
        graph, graphCoordinates, start, goal,
        penaltyEdges = penaltyEdges
    )

    // ── núcleo ────────────────────────────────────────────────────────────────

    private fun runAStar(
        graph: Graph,
        graphCoordinates: Map<String, LatLng>,
        start: String,
        goal: String,
        blockedEdges: Set<Pair<String, String>> = emptySet(),
        penaltyEdges: Map<Pair<String, String>, Double> = emptyMap()
    ): List<String> {

        if (start == goal) return listOf(start)

        // Par (nodeId, fScore): se ordena por fScore para explorar primero los
        // nodos más prometedores. Usar Comparator explícito evita boxing doble.
        val openSet = PriorityQueue<Pair<String, Double>>(16, compareBy { it.second })

        val cameFrom  = HashMap<String, String>()
        val gScore    = HashMap<String, Double>().apply { put(start, 0.0) }
        val closedSet = HashSet<String>()

        val startH = heuristic(graphCoordinates, start, goal)
        openSet.add(start to startH)

        while (openSet.isNotEmpty()) {

            val (current, currentF) = openSet.poll()

            // Entrada obsoleta: el nodo ya fue procesado con un fScore mejor
            if (closedSet.contains(current)) continue

            val currentG = gScore[current] ?: Double.POSITIVE_INFINITY
            // Descartar entradas duplicadas en la cola con fScore desactualizado
            if (currentF > currentG + HEURISTIC_WEIGHT * heuristic(graphCoordinates, current, goal) + 1e-9) continue

            if (current == goal) return reconstructPath(cameFrom, start, goal)

            closedSet.add(current)

            for (edge in graph[current] ?: continue) {

                val neighbor = edge.id

                if (closedSet.contains(neighbor)) continue
                if (blockedEdges.contains(current to neighbor)) continue

                val penalty = penaltyEdges[current to neighbor] ?: 0.0
                val tentativeG = currentG + edge.cost + penalty

                if (tentativeG < (gScore[neighbor] ?: Double.POSITIVE_INFINITY)) {

                    cameFrom[neighbor] = current
                    gScore[neighbor]   = tentativeG

                    val f = tentativeG + HEURISTIC_WEIGHT * heuristic(graphCoordinates, neighbor, goal)
                    openSet.add(neighbor to f)
                }
            }
        }

        return emptyList()
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private fun heuristic(
        graphCoordinates: Map<String, LatLng>,
        from: String,
        to: String
    ): Double {
        val a = graphCoordinates[from] ?: return 0.0
        val b = graphCoordinates[to]   ?: return 0.0
        return GraphUtils.distanceMeters(a, b)
    }

    private fun reconstructPath(
        cameFrom: Map<String, String>,
        start: String,
        goal: String
    ): List<String> {
        val path = ArrayDeque<String>()
        var node = goal
        while (cameFrom.containsKey(node)) {
            path.addFirst(node)
            node = cameFrom[node]!!
        }
        path.addFirst(start)
        return path.toList()
    }
}
