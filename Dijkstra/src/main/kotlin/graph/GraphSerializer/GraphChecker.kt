package org.example.graph.GraphSerializer

import org.example.graph.Graph
import org.example.graph.Edge
import org.example.graph.Vertex

// TODO: добавить обработку ошибок

class GraphChecker {

    // Метод для проверки корректности графа
    fun isValidGraph(graph: Graph): Boolean {
        
        // Проверка на наличие вершин
        if (graph.vertecies.isEmpty()) {
            println("Граф не содержит вершин.")
            return false
        }
    
        // Проверка на наличие ребер
        if (graph.edges.isEmpty()) {
            println("Граф не содержит ребер.")
            return false
        }
    
        val vertexIds = graph.vertecies.map { it.id }.toSet()
    
        // Проверка связи вершин и ребер
        for (edge in graph.edges) {
            if (edge.source !in vertexIds || edge.target !in vertexIds) {
                println("Некорректное ребро: от вершины ${edge.source} к вершине ${edge.target} не существует.")
                return false
            }
        }
         
        // Проверка уникальности ID вершин
        if (vertexIds.size != graph.vertecies.size) {
            println("Найдены дублирующиеся ID вершин.")
            return false
        }
    
        return true
    }

    fun isDataValid(graph: Graph): Boolean {
    
        // Проверка корректности вершин
        for (vertex in graph.vertecies) {
            if (!vertex.isValid()) {
                println("Некорректная вершина: ${vertex.id} с координатами (${vertex.xCoordinate}, ${vertex.yCoordinate}) и цветом ${vertex.color}.")
                return false
            }
        }
    
        // Проверка корректности ребер
        for (edge in graph.edges) {
            if (!edge.isValid()) {
                println("Некорректное ребро: от вершины ${edge.source} к вершине ${edge.target} с весом ${edge.weight}.")
                return false
            }
        }

        return true
    }

}