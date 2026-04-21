package com.example.roomtracker.map

import android.content.Context
import org.json.JSONObject

object GraphLoader {

    /**
     * Carga la topología del grafo desde graph.json usando los costos
     * almacenados directamente (distancia real de caminata a lo largo
     * de la LineString, no distancia en línea recta).
     *
     * Los costos en graph.json son generados por generate_graph.py como
     * la suma de segmentos Haversine de cada LineString, representando
     * la distancia real que se camina por ese tramo.
     */
    fun loadGraph(context: Context): Graph {

        val localFile = java.io.File(context.filesDir, "graph.json")
        if (!localFile.exists()) return emptyMap()
        val jsonObject = JSONObject(localFile.readText())

        val graph = mutableMapOf<String, List<GraphEdge>>()
        val keys = jsonObject.keys()

        while (keys.hasNext()) {

            val nodeId = keys.next()
            val edgesArray = jsonObject.getJSONArray(nodeId)

            val edges = mutableListOf<GraphEdge>()

            for (i in 0 until edgesArray.length()) {

                val edge = edgesArray.getJSONObject(i)
                val neighborId = edge.getString("id")
                val cost = edge.getDouble("cost")

                edges.add(GraphEdge(id = neighborId, cost = cost))
            }

            graph[nodeId] = edges
        }

        return graph
    }
}