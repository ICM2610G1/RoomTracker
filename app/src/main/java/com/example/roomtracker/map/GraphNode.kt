package com.example.roomtracker.map

data class GraphEdge(
    val id: String,
    val cost: Double
)

typealias Graph = Map<String, List<GraphEdge>>