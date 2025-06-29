package org.example.graph.GraphSerializer

import org.example.graph.Graph
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
        if (graphChecker.isValidGraph(graph) && graphChecker.isDataValid(graph)) {
            println("Граф успешно загружен из файла: $filePath")
        }
        else {
            throw IllegalArgumentException("Некорректный граф в файле: $filePath")
        }
        
        return graph

    }
}