package graph.GraphSerializer

import algorithm.DijkstraResult
import graph.Graph

// Класс для хранения графа вместе с результатами алгоритма Дейкстры
data class GraphWithDijkstraData(
    val graph: Graph,
    val dijkstraResult: DijkstraResult?,
    val startVertexId: Int?
) 