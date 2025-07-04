package graph.GraphSerializer

import java.util.UUID
import com.google.gson.Gson
import graph.Graph
import algorithm.DijkstraResult
import algorithm.dijkstra

// Класс для сохранения графа в файл
class GraphSaver {

    fun saveGraphToFile(graph: Graph, filePath: String, startVertexId: Int? = null): String {
        if (filePath.isEmpty()) {
            throw IllegalArgumentException("File path cannot be empty")
        }

        // Определяем начальную вершину
        val finalStartVertexId = startVertexId ?: determineStartVertex(graph)

        // Запускаем алгоритм Дейкстры
        val dijkstraResult = if (finalStartVertexId != null) {
            dijkstra(graph, finalStartVertexId)
        } else null

        // Создаем объект с расширенными данными
        val graphWithDijkstra = GraphWithDijkstraData(
            graph = graph,
            dijkstraResult = dijkstraResult,
            startVertexId = finalStartVertexId
        )

        val gson = Gson()
        val jsonContent = gson.toJson(graphWithDijkstra)
        val fileName = "graph-data-" + UUID.randomUUID().toString() + ".json" // генератор уникального имени файла

        // Формируем полный путь к файлу
        val fullPath = if (filePath.endsWith("/")) {
            filePath + fileName
        }
        else {
            "$filePath/$fileName"
        }

        java.io.File(fullPath).writeText(jsonContent)
        return fullPath
    }

    // Определяем начальную вершину: если есть выбранная - используем её, иначе первую по алфавиту
    private fun determineStartVertex(graph: Graph): Int? {
        if (graph.vertecies.isEmpty()) return null

        // Сначала ищем вершину с желтым цветом (выбранную пользователем)
        val selectedVertex = graph.vertecies.find { it.color.name == "YELLOW" }
        if (selectedVertex != null) {
            return selectedVertex.id
        }

        // Если нет выбранной, берем первую по алфавиту
        return graph.vertecies.minByOrNull { it.name }?.id
    }
}