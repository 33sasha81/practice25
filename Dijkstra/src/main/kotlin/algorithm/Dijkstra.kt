package algorithm

import java.util.*
import graph.Graph

// Класс для хранения состояния на каждом шаге алгоритма
data class DijkstraStep(
        val step: Int,
        val action: String,
        val currentVertexId: Int?,
        val distances: Map<Int, Int>,
        val visited: Set<Int>,
        val priorityQueue: List<Pair<Int, Int>>
)

// Класс для хранения итогового результата
data class DijkstraResult(
        val distances: Map<Int, Int>,
        val previous: Map<Int, Int?>,
        val steps: List<DijkstraStep>
)

fun dijkstra(graph: Graph, startVertexId: Int): DijkstraResult {
    // Инициализация
    val distances = mutableMapOf<Int, Int>()
    val previous = mutableMapOf<Int, Int?>()
    val visited = mutableSetOf<Int>()
    val pq = PriorityQueue<Pair<Int, Int>>(compareBy { it.first })

    graph.vertecies.forEach {
        distances[it.id] = Int.MAX_VALUE
        previous[it.id] = null
    }

    distances[startVertexId] = 0
    pq.add(0 to startVertexId)

    val steps = mutableListOf<DijkstraStep>()
    var stepCounter = 0

    // Начальное состояние
    steps.add(
            DijkstraStep(
                    step = stepCounter,
                    action = "Инициализация",
                    currentVertexId = null,
                    distances = distances.toMap(),
                    visited = visited.toSet(),
                    priorityQueue = pq.toList().map { it.second to it.first }
            )
    )

    while (pq.isNotEmpty()) {
        stepCounter++
        val (currentDistance, currentVertexId) = pq.poll()

        if (currentVertexId in visited) {
            continue
        }

        visited.add(currentVertexId)

        val action = "Обрабатываем вершину ${graph.getVertexById(currentVertexId).name}"

        // Обновляем расстояния до соседей
        graph.edges
                .filter { it.source == currentVertexId || it.target == currentVertexId }
                .forEach { edge ->
                    val neighborId =
                            if (edge.source == currentVertexId) edge.target else edge.source
                    if (neighborId !in visited) {
                        val newDistance = currentDistance + edge.weight
                        if (newDistance < (distances[neighborId] ?: Int.MAX_VALUE)) {
                            distances[neighborId] = newDistance
                            previous[neighborId] = currentVertexId
                            pq.add(newDistance to neighborId)
                        }
                    }
                }

        steps.add(
                DijkstraStep(
                        step = stepCounter,
                        action = action,
                        currentVertexId = currentVertexId,
                        distances = distances.toMap(),
                        visited = visited.toSet(),
                        priorityQueue = pq.toList().map { it.second to it.first }
                )
        )
    }

    return DijkstraResult(distances.toMap(), previous.toMap(), steps)
}
