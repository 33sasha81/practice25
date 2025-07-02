package graph.GraphSerializer

import java.util.UUID

import com.google.gson.Gson
import graph.Graph

// Класс для сохранения графа в файл
class GraphSaver {
    
    fun saveGraphToFile(graph: Graph, filePath: String): String {
        if (filePath.isEmpty()) {
            throw IllegalArgumentException("File path cannot be empty")
        }

        val gson = Gson()
        val jsonContent = gson.toJson(graph)
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
}