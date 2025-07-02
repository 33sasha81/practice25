package graph.GraphSerializer

import graph.Graph
import graph.Edge
import graph.Vertex
import java.lang.IllegalArgumentException

class GraphChecker {

    // Метод для проверки корректности графа
    fun isValidGraph(graph: Graph){
        
        // Проверка на наличие вершин
        if (graph.vertecies.isEmpty()) {
            throw IllegalArgumentException("Граф не содержит вершин.")
        }
    
        val vertexIds = graph.vertecies.map { it.id }.toSet()
    
        // Проверка связи вершин и ребер
        for (edge in graph.edges) {
            if (edge.source !in vertexIds || edge.target !in vertexIds) {
                throw IllegalArgumentException("Некорректное ребро: от вершины ${edge.source} к вершине ${edge.target} не существует.")
            }
        }
         
        // Проверка уникальности ID вершин
        if (vertexIds.size != graph.vertecies.size) {
            throw IllegalArgumentException("Найдены дублирующиеся ID вершин.")
        }

    }

    fun isDataValid(graph: Graph) {
    
        // Проверка корректности вершин
        for (vertex in graph.vertecies) {
            if (!vertex.isValid()) {
                throw IllegalArgumentException("Некорректная вершина: ${vertex.id} с координатами (${vertex.xCoordinate}, ${vertex.yCoordinate}) и цветом ${vertex.color}.")
            }
        }
    
        // Проверка корректности ребер
        for (edge in graph.edges) {
            if (!edge.isValid()) {
                throw IllegalArgumentException("Некорректное ребро: от вершины ${edge.source} к вершине ${edge.target} с весом ${edge.weight}.")
            }
        }
    }

}