package org.example.graph
// TODO: поменять пакеты
// Класс для представления графа
class Graph(var isDirected: Boolean = false) {
    // TODO: переделать вершины на map
    private val _vertecies = mutableListOf<Vertex>()
    private val _edges = mutableListOf<Edge>()

    val vertecies: List<Vertex>
        get() = _vertecies.toList()

    val edges: List<Edge>
        get() = _edges.toList()

    fun addVertex(vertex: Vertex) {
        _vertecies.add(vertex)
    }

    fun addEdge(edge: Edge) {
        _edges.add(edge)
    }

    // Поулучаем вершину по ID
    fun getVertexById(id: Int): Vertex {
        return _vertecies.find { it.id == id } ?: throw IllegalArgumentException("Вершина с ID $id не найдена")
    }

    fun deleteVertexByid(id: Int) {
        // Удаляем все рёбра, связанные с этой вершиной
        _edges.removeAll { it.source == id || it.target == id }
        val vertex = getVertexById(id)
        _vertecies.remove(vertex)
    }

    // Удаляем ребро по ID начальной и конечной вершины
    fun deleteEdgeBySourceAndTarget(source: Int, target: Int) {
        _edges.removeAll { it.source == source && it.target == target }
    }

    override fun toString(): String {
        val sb = StringBuilder()
        sb.append("Граф(isDirected=$isDirected)\n")
        sb.append("Вершины:\n")
        _vertecies.forEach { sb.append("  $it\n") }
        sb.append("Ребра:\n")
        _edges.forEach { sb.append("  $it\n") }
        return sb.toString()
    }
}