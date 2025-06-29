package controller

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import org.example.graph.*
import org.example.graph.GraphSerializer.GraphLoader
import org.example.graph.GraphSerializer.GraphSaver

class AppController {
    var graph by mutableStateOf(Graph())
        private set

    private val saver = GraphSaver()
    private val loader = GraphLoader()

    // Добавление новой вершины со случайными координатами
    fun addVertex() {
        val currentGraph = graph
        // Генерация уникального имени вершины (A, B, C,...)
        val usedNames = currentGraph.vertecies.map { it.name }.toSet()
        val name = (0..25).map { ('A' + it).toString() }.first { it !in usedNames }

        // Генерация нового ID вершины
        val vertexId = if (currentGraph.vertecies.isEmpty()) 0 else (currentGraph.vertecies.maxOf { it.id } + 1)

        val newVertex = Vertex(vertexId, (50..400).random(), (50..400).random(), name = name)
        currentGraph.addVertex(newVertex)
        graph = createGraphCopy(currentGraph)
        println("Вершина добавлена: $newVertex")
    }

    // Добавление новой вершины с указанными координатами
    fun addVertex(x: Int, y: Int) {
        val currentGraph = graph

        val usedNames = currentGraph.vertecies.map { it.name }.toSet()
        val name = (0..25).map { ('A' + it).toString() }.first { it !in usedNames }

        val vertexId = if (currentGraph.vertecies.isEmpty()) 0 else (currentGraph.vertecies.maxOf { it.id } + 1)

        val newVertex = Vertex(vertexId, x, y, name = name)
        currentGraph.addVertex(newVertex)
        graph = createGraphCopy(currentGraph)
        println("Вершина добавлена: $newVertex")
    }

    // Удаление вершины по ID
    fun deleteVertex(vertexId : Int) {
        try {
            val currentGraph = graph
            currentGraph.deleteVertexByid(vertexId)
            graph = createGraphCopy(currentGraph)
            println("Вершина с ID $vertexId удалена")
        } 
        catch (e: Exception) {
            println("Ошибка при удалении вершины: ${e.message}")
        }
    }

    // Добавление ребра между вершинами
    fun addEdge(sourceId: Int, targetId: Int, weight: Int = 1) {
        val currentGraph = graph
        // Проверка на кратные ребра (без учета направления)
        val exists = currentGraph.edges.any {
            (it.source == sourceId && it.target == targetId) ||
            (it.source == targetId && it.target == sourceId)
        }

        if (exists) {
            println("Ребро между $sourceId и $targetId уже существует!")
            return
        }

        val newEdge = Edge(weight, sourceId, targetId)
        if (newEdge.isValid()) {
            currentGraph.addEdge(newEdge)
            graph = createGraphCopy(currentGraph)
            println("Ребро добавлено: $newEdge")
        } else {
            println("Ошибка при добавлении ребра: некорректные данные")
        }
    }

    // Удаление ребра между вершинами
    fun deleteEdge(sourceId: Int, targetId: Int) {
        val currentGraph = graph
        
        try {
            currentGraph.deleteEdgeBySourceAndTarget(sourceId, targetId)
            graph = createGraphCopy(currentGraph)
            println("Ребро от $sourceId к $targetId удалено")
        } 
        catch (e: Exception) {
            println("Ошибка при удалении ребра: ${e.message}")
        }
    }

    // Сохранение графа в файл
    fun saveGraph() {
        try {
            val savedPath = saver.saveGraphToFile(graph, ".")
            println("Граф успешно сохранен в: $savedPath")
        } 
        catch (e: Exception) {
            println("Ошибка при сохранении графа: ${e.message}")
        }
    }

    // Загрузка графа из файла
    fun loadGraph(filePath: String) {
        try {
            graph = loader.loadGraphFromFile(filePath)
            println("Граф успешно загружен из файла: $filePath")
        } 
        catch (e: Exception) {
            println("Ошибка при загрузке графа: ${e.message}")
        }
    }

    // Вспомогательная функция для создания копии графа, чтобы обновить состояние
    private fun createGraphCopy(original: Graph): Graph {
        val newGraph = Graph(original.isDirected)
        original.vertecies.forEach { newGraph.addVertex(it.copy()) } // Копируем вершины
        original.edges.forEach { newGraph.addEdge(it.copy()) }     // Копируем ребра
        return newGraph
    }
}