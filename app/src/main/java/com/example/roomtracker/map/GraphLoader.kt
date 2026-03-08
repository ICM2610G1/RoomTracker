package com.example.roomtracker.map

import android.content.Context
import org.json.JSONObject

object GraphLoader {

    fun loadGraph(context: Context): Graph {

        val input = context.assets.open("graph.json")
        val json = input.bufferedReader().use { it.readText() }

        val jsonObject = JSONObject(json)

        val graph = mutableMapOf<String, List<GraphEdge>>()

        val keys = jsonObject.keys()

        while (keys.hasNext()) {

            val nodeId = keys.next()
            val edgesArray = jsonObject.getJSONArray(nodeId)

            val edges = mutableListOf<GraphEdge>()

            for (i in 0 until edgesArray.length()) {

                val edge = edgesArray.getJSONObject(i)

                edges.add(
                    GraphEdge(
                        id = edge.getString("id"),
                        cost = edge.getDouble("cost")
                    )
                )
            }

            graph[nodeId] = edges
        }

        return graph
    }
}