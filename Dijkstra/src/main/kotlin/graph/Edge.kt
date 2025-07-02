package graph

// Класс для представления ребра графа
data class Edge(var weight: Int, var source: Int, var target: Int) {
    
    fun updateWeight(newWeight: Int) {
        if (newWeight >= 0) {
            weight = newWeight
        } 
        else {
            throw IllegalArgumentException("Weight cannot be negative")
        }
    }

    fun checkWeight(): Boolean {
        return weight >= 0
    }

    fun checkLoop(): Boolean {
        return source != target
    }

    fun isValid(): Boolean {
        return checkWeight() && checkLoop()
    }

    override fun toString(): String {
        return "Edge(from=$source to $target, weight=$weight)"
    }
}