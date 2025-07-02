package visualisation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material.Divider
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import controller.AppController

@Composable
fun FullDijkstraTable(controller: AppController) {
    val result = controller.dijkstraResult ?: return
    val steps = result.steps
    val vertecies = controller.graph.vertecies
    val currentStep = controller.currentStep

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .border(1.dp, Color(0xFFCCCCCC))
            .padding(8.dp)
    ) {
        // Header
        Row(Modifier.fillMaxWidth()) {
            Text("Шаг", modifier = Modifier.width(40.dp), fontWeight = FontWeight.Bold, fontSize = 13.sp, textAlign = TextAlign.Center)
            Text("Действие", modifier = Modifier.width(120.dp), fontWeight = FontWeight.Bold, fontSize = 13.sp, textAlign = TextAlign.Center)
            vertecies.forEach { v ->
                Text(v.name, modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold, fontSize = 13.sp, textAlign = TextAlign.Center)
            }
        }
        // Steps
        steps.forEachIndexed { idx, step ->
            val isFuture = idx > currentStep
            val rowColor = if (isFuture) Color(0xFFF0F0F0) else Color.White
            Row(
                Modifier
                    .fillMaxWidth()
                    .background(rowColor)
            ) {
                Text(idx.toString(), modifier = Modifier.width(40.dp), fontSize = 13.sp, textAlign = TextAlign.Center)
                Text(step.action, modifier = Modifier.width(120.dp), fontSize = 13.sp, textAlign = TextAlign.Center)
                vertecies.forEach { v ->
                    val value = step.distances[v.id]?.let { if (it == Int.MAX_VALUE) "∞" else it.toString() } ?: "∞"
                    Text(value, modifier = Modifier.weight(1f), fontSize = 13.sp, textAlign = TextAlign.Center, color = if (isFuture) Color.Gray else Color.Black)
                }
            }
            Divider(color = Color(0xFFCCCCCC), thickness = 1.dp)
        }
        // Итоговая строка с длинами всех путей
        Row(Modifier.fillMaxWidth().background(Color(0xFFE3F2FD))) {
            Text("Итог", modifier = Modifier.width(160.dp), fontWeight = FontWeight.Bold, fontSize = 13.sp, textAlign = TextAlign.Center)
            vertecies.forEach { v ->
                val value = result.distances[v.id]?.let { if (it == Int.MAX_VALUE) "∞" else it.toString() } ?: "∞"
                Text(value, modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold, fontSize = 13.sp, textAlign = TextAlign.Center, color = Color(0xFF1976D2))
            }
        }
    }
} 