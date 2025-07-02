package graph.GraphSerializer

import graph.Graph
import com.google.gson.Gson

// Класс для загрузки графа из файла
class GraphLoader {
    fun loadGraphFromFile(filePath: String): Graph {
        if (filePath.isEmpty()) {
            throw IllegalArgumentException("Файл не может быть пустым")
        }

        if (!filePath.endsWith(suffix = ".JSON", ignoreCase = true)) {
            throw IllegalArgumentException("Файл должен иметь расширение .JSON")
        }

        val gson = Gson()
        val fileContent = java.io.File(filePath).readText()
        val graph = gson.fromJson(fileContent, Graph::class.java)
        val graphChecker = GraphChecker()
        try {
            graphChecker.isValidGraph(graph)
            graphChecker.isDataValid(graph)
        } catch (e: IllegalArgumentException) {
            throw IllegalArgumentException("Ошибка валидации графа: ${e.message}")
        }
        return graph

    }
}