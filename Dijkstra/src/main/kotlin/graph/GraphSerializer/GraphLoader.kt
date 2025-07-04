package graph.GraphSerializer

import graph.Graph
import com.google.gson.Gson

// Класс для загрузки графа из файла
class GraphLoader {
    fun loadGraphFromFile(filePath: String): GraphWithDijkstraData {
        if (filePath.isEmpty()) {
            throw IllegalArgumentException("Файл не может быть пустым")
        }

        if (!filePath.endsWith(suffix = ".JSON", ignoreCase = true)) {
            throw IllegalArgumentException("Файл должен иметь расширение .JSON")
        }

        val gson = Gson()
        val fileContent = java.io.File(filePath).readText()

        // Пытаемся загрузить как новый формат с результатами Дейкстры
        return try {
            val graphWithDijkstra = gson.fromJson(fileContent, GraphWithDijkstraData::class.java)
            val graphChecker = GraphChecker()
            try {
                graphChecker.isValidGraph(graphWithDijkstra.graph)
                graphChecker.isDataValid(graphWithDijkstra.graph)
            } catch (e: IllegalArgumentException) {
                throw IllegalArgumentException("Ошибка валидации графа: ${e.message}")
            }
            graphWithDijkstra
        } catch (e: Exception) {
            // Если не удалось загрузить как новый формат, пробуем старый
            try {
                val graph = gson.fromJson(fileContent, Graph::class.java)
                val graphChecker = GraphChecker()
                try {
                    graphChecker.isValidGraph(graph)
                    graphChecker.isDataValid(graph)
                } catch (e: IllegalArgumentException) {
                    throw IllegalArgumentException("Ошибка валидации графа: ${e.message}")
                }
                // Для старого формата возвращаем без результатов Дейкстры
                GraphWithDijkstraData(graph, null, null)
            } catch (e2: Exception) {
                throw IllegalArgumentException("Не удалось загрузить файл: ${e2.message}")
            }
        }
    }
}