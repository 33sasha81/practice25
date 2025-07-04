package visualisation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import controller.AppController

@Composable
fun DijkstraTable(controller: AppController) {
    val vertecies = controller.graph.vertecies
    val finalStep = controller.finalDijkstraStep
    val result = controller.dijkstraResult
    val steps = result?.steps ?: emptyList()
    val currentStep = controller.currentStep

    // Если есть финальный шаг (после загрузки графа) — показываем только его
    if (finalStep != null) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White, shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp))
                .border(1.dp, Color(0xFFCCCCCC), shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp))
                .padding(8.dp)
        ) {
            Row(Modifier.fillMaxWidth()) {
                Text("Вершина", modifier = Modifier.weight(1f), fontSize = 14.sp)
                vertecies.forEach { v ->
                    Text(
                        v.name,
                        modifier = Modifier.weight(1f),
                        fontSize = 14.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
            Row(Modifier.fillMaxWidth()) {
                Text("Кратчайший путь", modifier = Modifier.weight(1f), fontSize = 14.sp)
                vertecies.forEach { v ->
                    val value = finalStep.distances[v.id]?.let { if (it == Int.MAX_VALUE) "∞" else it.toString() } ?: "∞"
                    Text(value, modifier = Modifier.weight(1f), fontSize = 14.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                }
            }
        }
        return
    }

    // Обычный режим (анимация)
    val beforeDistances = if (currentStep > 0 && steps.isNotEmpty()) steps[currentStep - 1].distances else vertecies.associate { it.id to "∞" }
    val afterDistances = if (steps.isNotEmpty() && currentStep < steps.size) steps[currentStep].distances else vertecies.associate { it.id to "∞" }
    val currentStepData = steps.getOrNull(currentStep)
    val highlightVertexIds = if (currentStepData?.currentVertexId != null)
        controller.graph.edges
            .filter { it.source == currentStepData.currentVertexId || it.target == currentStepData.currentVertexId }
            .map { if (it.source == currentStepData.currentVertexId) it.target else it.source }
            .filter { it !in currentStepData.visited }
    else emptyList()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp))
            .border(1.dp, Color(0xFFCCCCCC), shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp))
            .padding(8.dp)
    ) {
        Row(Modifier.fillMaxWidth()) {
            Text("Вершина", modifier = Modifier.weight(1f), fontSize = 14.sp)
            vertecies.forEach { v ->
                val highlight = v.id in highlightVertexIds
                Text(
                    v.name,
                    modifier = Modifier.weight(1f).background(if (highlight) Color(0xFFD2E3FC) else Color.Transparent),
                    fontSize = 14.sp,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
        Row(Modifier.fillMaxWidth()) {
            Text("Вес до шага", modifier = Modifier.weight(1f), fontSize = 14.sp)
            vertecies.forEach { v ->
                val value = beforeDistances[v.id]?.let { if (it == Int.MAX_VALUE) "∞" else it.toString() } ?: "∞"
                Text(value, modifier = Modifier.weight(1f), fontSize = 14.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
            }
        }
        Row(Modifier.fillMaxWidth()) {
            Text("Вес после шага", modifier = Modifier.weight(1f), fontSize = 14.sp)
            vertecies.forEach { v ->
                val value = afterDistances[v.id]?.let { if (it == Int.MAX_VALUE) "∞" else it.toString() } ?: "∞"
                Text(value, modifier = Modifier.weight(1f), fontSize = 14.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
            }
        }
    }
}