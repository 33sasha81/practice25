package controller

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import algorithm.DijkstraResult
import algorithm.dijkstra
import graph.*
import graph.GraphSerializer.GraphLoader
import graph.GraphSerializer.GraphSaver
import graph.VertexColor

class AppController {
    var graph by mutableStateOf(Graph())
        private set

    // Добавляем отслеживание начальной вершины
    var startVertexId by mutableStateOf<Int?>(null)
        private set

    var dijkstraResult by mutableStateOf<DijkstraResult?>(null)
        private set

    var notification by mutableStateOf<String?>(null)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set    

    // --- Состояние анимации алгоритма Дейкстры ---
    var isRunning by mutableStateOf(false)
        private set
    var isPaused by mutableStateOf(false)
        private set
    var currentStep by mutableStateOf(0)
        private set


    private val saver = GraphSaver()
    private val loader = GraphLoader()

    // Добавление вершины с заданными координатами
    fun addVertex(x: Int, y: Int) {
        val currentGraph = graph
        val name = getNextVertexName()
        val vertexId = if (currentGraph.vertecies.isEmpty()) 0 else (currentGraph.vertecies.maxOf { it.id } + 1)
        val newVertex = Vertex(vertexId, x, y, name = name)
        currentGraph.addVertex(newVertex)
        graph = createGraphCopy(currentGraph)
        notification = "Вершина добавлена: $name"
    }

    // Удаление вершины по ID
    fun deleteVertex(vertexId: Int) {
        try {
            val currentGraph = graph

            // Если удаляем начальную вершину, сбрасываем её
            if (startVertexId == vertexId) {
                startVertexId = null
            }

            val vertexName = currentGraph.vertecies.find { it.id == vertexId }?.name

            currentGraph.deleteVertexByid(vertexId)
            graph = createGraphCopy(currentGraph)
            notification = "Вершина $vertexName удалена"
        } 
        catch (e: Exception) {
            errorMessage = "Ошибка при удалении вершины: ${e.message}"
        }
    }

    // Добавление ребра между вершинами
    fun addEdge(sourceId: Int, targetId: Int, weight: Int = 1) {
        val currentGraph = graph
        // Проверка на кратные рёбра (без учёта направления)
        val exists = currentGraph.edges.any {
            (it.source == sourceId && it.target == targetId) ||
            (it.source == targetId && it.target == sourceId)
        }
        if (exists) {
            errorMessage = "Ребро между $sourceId и $targetId уже существует!"
            return
        }
        val newEdge = Edge(weight, sourceId, targetId)
        if (newEdge.isValid()) {
            currentGraph.addEdge(newEdge)
            graph = createGraphCopy(currentGraph)
            notification = "Ребро с весом $weight добавлено"
        } else {
            errorMessage = "Ошибка при добавлении ребра: некорректные данные"
        }
    }

    // Удаление ребра между вершинами
    fun deleteEdge(sourceId: Int, targetId: Int) {
        val currentGraph = graph

        try {
            currentGraph.deleteEdgeBySourceAndTarget(sourceId, targetId)
            graph = createGraphCopy(currentGraph)
            notification = "Ребро удалено"
        } 
        catch (e: Exception) {
            errorMessage = "Ошибка при удалении ребра: ${e.message}"
        }
    }

    // Изменение веса ребра между вершинами
    fun changeEdgeWeight(sourceId: Int, targetId: Int, newWeight: Int) {
        val currentGraph = graph
        val edge = currentGraph.edges.find {
            (it.source == sourceId && it.target == targetId) ||
            (it.source == targetId && it.target == sourceId)
        }
        if (edge != null) {
            edge.updateWeight(newWeight)
            graph = createGraphCopy(currentGraph)
            notification = "Вес ребра изменён на $newWeight"
        } else {
            errorMessage = "Ребро между $sourceId и $targetId не найдено"
        }
    }

    // Изменение цвета вершины
    fun changeVertexColor(vertexId: Int, color: VertexColor) {
        val currentGraph = graph
        val vertex = currentGraph.vertecies.find { it.id == vertexId }
        if (vertex != null) {
            vertex.color = color
            graph = createGraphCopy(currentGraph)
            notification = "Цвет вершины $vertexId изменен на $color"
        } else {
            errorMessage = "Вершина с ID $vertexId не найдена"
        }
    }

    // Установка начальной вершины
    fun setStartVertex(vertexId: Int) {
        val currentGraph = graph

        // Проверяем, существует ли вершина с таким ID
        val vertex = currentGraph.vertecies.find { it.id == vertexId }
        if (vertex == null) {
            errorMessage = "Вершина с ID $vertexId не найдена"
            return
        }

        // Если уже есть начальная вершина, сбрасываем её цвет
        if (startVertexId != null) {
            val previousStartVertex = currentGraph.vertecies.find { it.id == startVertexId }
            previousStartVertex?.color = VertexColor.GRAY
        }

        // Устанавливаем новую начальную вершину
        startVertexId = vertexId
        vertex.color = VertexColor.YELLOW

        graph = createGraphCopy(currentGraph)
        notification = "Начальная вершина установлена"
    }

    // Сброс начальной вершины
    fun clearStartVertex() {
        if (startVertexId != null) {
            val currentGraph = graph
            val vertex = currentGraph.vertecies.find { it.id == startVertexId }
            vertex?.color = VertexColor.GRAY
            startVertexId = null
            graph = createGraphCopy(currentGraph)
            notification = "Начальная вершина сброшена"
        }
    }

    // Получение информации о начальной вершине
    fun getStartVertex(): Vertex? {
        return if (startVertexId != null) {
            graph.vertecies.find { it.id == startVertexId }
        } else null
    }

    // Соохранение графа в файл
    fun saveGraph() {
        try {
            val savedPath = saver.saveGraphToFile(graph, ".")
            notification = "Граф успешно сохранен в: $savedPath"
        } catch (e: Exception) {
            errorMessage = "Ошибка при сохранении графа: ${e.message}"
        }
    }

    // Загрузка графа из файла
    fun loadGraph(filePath: String) {
        try {
            graph = loader.loadGraphFromFile(filePath)
            val startVertex = graph.vertecies.find { it.color == VertexColor.YELLOW }
            startVertexId = startVertex?.id
            notification = "Граф успешно загружен из файла: $filePath"
        } catch (e: Exception) {
            errorMessage = e.message ?: "Ошибка при загрузке графа"
        }
    }

    // Вспомогательная функция для создания копии графа, чтобы обновить состояние
    private fun createGraphCopy(original: Graph): Graph {
        val newGraph = Graph(original.isDirected)
        original.vertecies.forEach { newGraph.addVertex(it.copy()) } // Копируем вершины
        original.edges.forEach { newGraph.addEdge(it.copy()) } // Копируем ребра
        return newGraph
    }

    // Генерация уникального имени для новой вершины
    // Имена формируются как в Excel
    private fun getNextVertexName(): String {
        val existingNames = graph.vertecies.map { it.name }.toSet()
        var i = 0
        while (true) {
            val name = generateName(i)
            if (name !in existingNames) {
                return name
            }
            i++
        }
    }

    // Генерация имени для вершины по индексу
    private fun generateName(index: Int): String {
        var num = index
        val nameBuilder = StringBuilder()
        while (num >= 0) {
            nameBuilder.insert(0, ('A' + num % 26))
            num = num / 26 - 1
        }
        return nameBuilder.toString()
    }

    // Запуск анимации алгоритма
    fun startDijkstraAnimation() {
        if (graph.vertecies.isEmpty() || startVertexId == null) return
        dijkstraResult = algorithm.dijkstra(graph, startVertexId!!)
        isRunning = true
        isPaused = false
        currentStep = 0
    }

    // Пауза анимации
    fun pauseDijkstraAnimation() {
        if (isRunning) {
            isPaused = true
        }
    }

    // Возобновление анимации
    fun resumeDijkstraAnimation() {
        if (isRunning && isPaused) {
            isPaused = false
        }
    }

    // Остановка анимации
    fun stopDijkstraAnimation() {
        isRunning = false
        isPaused = false
        currentStep = 0
    }

    fun dismissError() {
        errorMessage = null
    }
    
    // Следующий шаг анимации
    fun nextDijkstraStep() {
        val steps = dijkstraResult?.steps ?: return
        if (currentStep < steps.size - 1) {
            currentStep++
        } else {
            isRunning = false
            isPaused = false
        }
    }

    fun goToEnd() {
        val steps = dijkstraResult?.steps
        if (steps != null && steps.isNotEmpty()) {
            currentStep = steps.size - 1
            isRunning = false
            isPaused = false
        }
    }

    fun resetDijkstra() {
        dijkstraResult = null
        isRunning = false
        isPaused = false
        currentStep = 0
    }

}
