package org.example.graph

// Класс для представления вершины графа
data class Vertex(var id: Int, var xCoordinate: Int, var yCoordinate: Int, var color: VertexColor = VertexColor.GRAY, var name: String) {
    
    fun moveTo(newX: Int, newY: Int) {
        xCoordinate = newX
        yCoordinate = newY
    }
    
    fun paint(newColor: VertexColor) {
        color = newColor
    }

    fun checkCoordinates(): Boolean {
        return xCoordinate >= 0 && yCoordinate >= 0
    }

    fun checkId(): Boolean {
        return id >= 0
    }

    fun checkColor(): Boolean {
        return try {
            color in VertexColor.values()
        } 
        catch (e: Exception) {
            println("Некорректный цвет вершины: $color")
            false
        }
    }

    fun isValid(): Boolean {
        return checkId() && checkCoordinates() && checkColor()
    }

}