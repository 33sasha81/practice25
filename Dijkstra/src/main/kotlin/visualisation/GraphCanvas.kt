package visualisation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import graph.Vertex
import graph.VertexColor
import androidx.compose.foundation.Canvas
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.Alignment
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.text.drawText
import graph.Edge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.material.AlertDialog
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.DpOffset
import algorithm.DijkstraStep

@Composable
fun GraphCanvas(
    vertices: List<Vertex>,
    edges: List<Edge> = emptyList(),
    currentStepData: DijkstraStep? = null,
    onCanvasClick: ((x: Int, y: Int) -> Unit)? = null,
    onAddEdge: ((sourceId: Int, targetId: Int, weight: Int) -> Unit)? = null,
    onDeleteEdge: ((sourceId: Int, targetId: Int) -> Unit)? = null,
    onDeleteVertex: ((vertexId: Int) -> Unit)? = null
) {
    val density = LocalDensity.current
    val textMeasurer = rememberTextMeasurer()
    var selectedVertices by remember { mutableStateOf(listOf<Int>()) }
    var showWeightDialog by remember { mutableStateOf(false) }
    var edgeWeightInput by remember { mutableStateOf("") }
    var selectedEdge by remember { mutableStateOf<Pair<Int, Int>?>(null) }
    var showDeleteIcon by remember { mutableStateOf(false) }
    var selectedVertex by remember { mutableStateOf<Int?>(null) }
    var showDeleteVertexIcon by remember { mutableStateOf(false) }
    var showEditWeightDialog by remember { mutableStateOf(false) }
    var editWeightInput by remember { mutableStateOf("") }

    // Размер иконки
    val iconSize = with(density) { 28.dp.toPx() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White, shape = RoundedCornerShape(12.dp))
            .border(2.dp, Color(0xFFCCCCCC), shape = RoundedCornerShape(12.dp))
            .pointerInput(onCanvasClick, edges, selectedEdge, showDeleteIcon, selectedVertex, showDeleteVertexIcon, showEditWeightDialog) {
                detectTapGestures(
                    onDoubleTap = { offset ->
                        // Проверка двойного клика по ребру
                        var foundEdge: Pair<Int, Int>? = null
                        for (edge in edges) {
                            val from = vertices.find { it.id == edge.source }
                            val to = vertices.find { it.id == edge.target }
                            if (from != null && to != null) {
                                val start = Offset(from.xCoordinate.toFloat(), from.yCoordinate.toFloat())
                                val end = Offset(to.yCoordinate.toFloat(), to.yCoordinate.toFloat())
                                val dist = distancePointToSegment(offset, start, end)
                                if (dist < 16f) {
                                    foundEdge = Pair(edge.source, edge.target)
                                    break
                                }
                            }
                        }
                        if (foundEdge != null) {
                            selectedEdge = foundEdge
                            showDeleteIcon = true
                            selectedVertex = null
                            showDeleteVertexIcon = false
                        } else {
                            // Проверка двойного клика по вершине
                            val clickedVertex = vertices.indexOfLast {
                                val dx = it.xCoordinate - offset.x
                                val dy = it.yCoordinate - offset.y
                                dx * dx + dy * dy <= 24f * 24f
                            }
                            val clickedVertexId = if (clickedVertex != -1) vertices[clickedVertex].id else null
                            if (clickedVertexId != null) {
                                selectedVertex = clickedVertexId
                                showDeleteVertexIcon = true
                                selectedEdge = null
                                showDeleteIcon = false
                            } else {
                                selectedVertex = null
                                showDeleteVertexIcon = false
                                selectedEdge = null
                                showDeleteIcon = false
                            }
                        }
                    },
                    onTap = { offset ->
                        // Удаление вершины
                        if (showDeleteVertexIcon && selectedVertex != null) {
                            val v = vertices.find { it.id == selectedVertex }
                            if (v != null) {
                                val center = Offset(v.xCoordinate.toFloat(), v.yCoordinate.toFloat())
                                val iconRect = androidx.compose.ui.geometry.Rect(
                                    center.x + iconSize * 0.7f,
                                    center.y - iconSize / 2,
                                    center.x + iconSize * 0.7f + iconSize,
                                    center.y + iconSize / 2
                                )
                                if (iconRect.contains(offset)) {
                                    onDeleteVertex?.invoke(selectedVertex!!)
                                    selectedVertex = null
                                    showDeleteVertexIcon = false
                                    return@detectTapGestures
                                }
                            }
                        }
                        // Удаление ребра
                        if (showDeleteIcon && selectedEdge != null) {
                            val (sourceId, targetId) = selectedEdge!!
                            val from = vertices.find { it.id == sourceId }
                            val to = vertices.find { it.id == targetId }
                            if (from != null && to != null) {
                                val mid = Offset((from.xCoordinate + to.xCoordinate) / 2f, (from.yCoordinate + to.yCoordinate) / 2f)
                                val trashX = mid.x + iconSize * 1.2f
                                val trashY = mid.y - iconSize / 2
                                val trashRect = androidx.compose.ui.geometry.Rect(
                                    trashX - iconSize * 0.3f,
                                    trashY - iconSize * 0.3f,
                                    trashX + iconSize * 1.3f,
                                    trashY + iconSize * 1.3f
                                )
                                if (trashRect.contains(offset)) {
                                    onDeleteEdge?.invoke(sourceId, targetId)
                                    selectedEdge = null
                                    showDeleteIcon = false
                                    return@detectTapGestures
                                }
                                // Обработка клика по иконке карандаша
                                val pencilX = mid.x - iconSize * 2.2f
                                val pencilY = mid.y - iconSize / 2
                                val pencilRect = androidx.compose.ui.geometry.Rect(
                                    pencilX - iconSize * 0.3f,
                                    pencilY - iconSize * 0.3f,
                                    pencilX + iconSize * 1.3f,
                                    pencilY + iconSize * 1.3f
                                )
                                if (pencilRect.contains(offset)) {
                                    editWeightInput = edges.find { (it.source == sourceId && it.target == targetId) || (it.source == targetId && it.target == sourceId) }?.weight?.toString() ?: ""
                                    showEditWeightDialog = true
                                    return@detectTapGestures
                                }
                            }
                        }
                        // Добавление вершины
                        if (onCanvasClick != null) {
                            onCanvasClick(offset.x.toInt(), offset.y.toInt())
                            selectedEdge = null
                            showDeleteIcon = false
                            selectedVertex = null
                            showDeleteVertexIcon = false
                            return@detectTapGestures
                        }
                        // Проверка клика по вершине
                        val clickedVertex = vertices.indexOfLast {
                            val dx = it.xCoordinate - offset.x
                            val dy = it.yCoordinate - offset.y
                            dx * dx + dy * dy <= 24f * 24f
                        }
                        val clickedVertexId = if (clickedVertex != -1) vertices[clickedVertex].id else null
                        if (clickedVertexId != null) {
                            if (selectedVertex == clickedVertexId && !showDeleteVertexIcon) {
                                showDeleteVertexIcon = true
                            } else {
                                selectedVertex = clickedVertexId
                                showDeleteVertexIcon = false
                            }
                            selectedEdge = null
                            showDeleteIcon = false
                            // Добавление ребра
                            selectedVertices =
                                if (selectedVertices.size == 2) listOf(clickedVertexId)
                                else selectedVertices + clickedVertexId
                            if (selectedVertices.size == 2) {
                                if (selectedVertices[0] != selectedVertices[1]) {
                                    showWeightDialog = true
                                } else {
                                    selectedVertices = emptyList()
                                }
                            }
                        } else {
                            selectedVertex = null
                            showDeleteVertexIcon = false
                            // Проверка клика по ребру
                            var foundEdge: Pair<Int, Int>? = null
                            for (edge in edges) {
                                val from = vertices.find { it.id == edge.source }
                                val to = vertices.find { it.id == edge.target }
                                if (from != null && to != null) {
                                    val start = Offset(from.xCoordinate.toFloat(), from.yCoordinate.toFloat())
                                    val end = Offset(to.xCoordinate.toFloat(), to.yCoordinate.toFloat())
                                    val dist = distancePointToSegment(offset, start, end)
                                    if (dist < 16f) {
                                        foundEdge = Pair(edge.source, edge.target)
                                        break
                                    }
                                }
                            }
                            if (foundEdge != null) {
                                if (selectedEdge == foundEdge && !showDeleteIcon) {
                                    showDeleteIcon = true
                                } else {
                                    selectedEdge = foundEdge
                                    showDeleteIcon = false
                                }
                                selectedVertex = null
                                showDeleteVertexIcon = false
                            } else {
                                selectedEdge = null
                                showDeleteIcon = false
                            }
                        }
                    }
                )
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            // Рисуем рёбра
            edges.forEach { edge ->
                val from = vertices.find { it.id == edge.source }
                val to = vertices.find { it.id == edge.target }
                if (from != null && to != null) {
                    val start = Offset(from.xCoordinate.toFloat(), from.yCoordinate.toFloat())
                    val end = Offset(to.xCoordinate.toFloat(), to.yCoordinate.toFloat())
                    val isSelected = selectedEdge == Pair(edge.source, edge.target) || selectedEdge == Pair(edge.target, edge.source)
                    // Выделение рёбер текущего шага
                    val isCurrentStepEdge = currentStepData?.let { step ->
                        val cur = step.currentVertexId
                        if (cur == null) false else
                            (edge.source == cur && edge.target !in step.visited) ||
                            (edge.target == cur && edge.source !in step.visited)
                    } ?: false
                    drawLine(
                        color = when {
                            isCurrentStepEdge -> Color(0xFF1976D2) // синий
                            isSelected -> Color.Red
                            else -> Color.Black
                        },
                        start = start,
                        end = end,
                        strokeWidth = when {
                            isCurrentStepEdge -> 6f
                            isSelected -> 5f
                            else -> 3f
                        },
                        pathEffect = if (isSelected) PathEffect.dashPathEffect(floatArrayOf(16f, 8f)) else null
                    )
                    // Рисуем вес ребра с белым фоном
                    val mid = Offset((start.x + end.x) / 2, (start.y + end.y) / 2)
                    val weightText = edge.weight.toString()
                    val textLayout = textMeasurer.measure(
                        weightText,
                        style = TextStyle(
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black,
                            textAlign = TextAlign.Center,
                            fontFamily = FontFamily.Default
                        )
                    )
                    val padding = 6.dp.toPx()
                    val bgWidth = textLayout.size.width + padding * 2
                    val bgHeight = textLayout.size.height + padding
                    drawRoundRect(
                        color = Color.White,
                        topLeft = Offset(
                            mid.x - bgWidth / 2f,
                            mid.y - bgHeight / 2f
                        ),
                        size = androidx.compose.ui.geometry.Size(bgWidth, bgHeight),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(8.dp.toPx(), 8.dp.toPx())
                    )
                    drawText(
                        textLayout,
                        topLeft = Offset(
                            mid.x - textLayout.size.width / 2f,
                            mid.y - textLayout.size.height / 2f
                        )
                    )
                    // Если ребро выделено и showDeleteIcon — рисуем иконки
                    if (isSelected && showDeleteIcon) {
                        // Иконка мусорки (справа)
                        val iconText = "🗑️"
                        val iconLayout = textMeasurer.measure(
                            iconText,
                            style = TextStyle(
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black,
                                textAlign = TextAlign.Center,
                                fontFamily = FontFamily.Default
                            )
                        )
                        val trashX = mid.x + iconSize * 1.2f
                        val trashY = mid.y - iconSize / 2
                        drawText(
                            iconLayout,
                            topLeft = Offset(
                                trashX,
                                trashY
                            )
                        )
                        // Иконка карандаша (слева)
                        val pencilText = "✎"
                        val pencilLayout = textMeasurer.measure(
                            pencilText,
                            style = TextStyle(
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black,
                                textAlign = TextAlign.Center,
                                fontFamily = FontFamily.Default
                            )
                        )
                        val pencilX = mid.x - iconSize * 2.2f
                        val pencilY = mid.y - iconSize / 2
                        drawText(
                            pencilLayout,
                            topLeft = Offset(
                                pencilX,
                                pencilY
                            )
                        )
                    }
                }
            }
            vertices.forEach { vertex ->
                val isVisited = currentStepData?.visited?.contains(vertex.id) == true
                // Не выделяем синим, если шаг последний (все посещены или currentVertexId уже в visited)
                val isCurrent = currentStepData?.currentVertexId == vertex.id &&
                    (currentStepData.visited.contains(vertex.id).not() || currentStepData.visited.size != vertices.size)
                val color = when {
                    isCurrent -> Color(0xFF1976D2) // синий
                    vertex.color == VertexColor.YELLOW -> Color(0xFFFFD600)
                    isVisited -> Color(0xFF43A047)
                    else -> Color(0xFFB0B0B0)
                }
                val center = Offset(vertex.xCoordinate.toFloat(), vertex.yCoordinate.toFloat())
                val isSelected = selectedVertex == vertex.id
                drawCircle(
                    color = color,
                    radius = 24f,
                    center = center
                )
                drawCircle(
                    color = if (isCurrent) Color(0xFF1976D2) else if (isSelected) Color.Red else Color.Black,
                    radius = 24f,
                    center = center,
                    style = Stroke(width = if (isCurrent) 5f else if (isSelected) 4f else 2f)
                )
                // Имя вершины (A, B, C, ...)
                val name = vertex.name
                val textLayout = textMeasurer.measure(
                    name,
                    style = TextStyle(
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        textAlign = TextAlign.Center,
                        fontFamily = FontFamily.Default
                    )
                )
                drawText(
                    textLayout,
                    topLeft = Offset(
                        center.x - textLayout.size.width / 2f,
                        center.y - textLayout.size.height / 2f
                    )
                )
                // Если вершина выделена и showDeleteVertexIcon — рисуем иконку мусорки
                if (isSelected && showDeleteVertexIcon) {
                    val iconText = "🗑️"
                    val iconLayout = textMeasurer.measure(
                        iconText,
                        style = TextStyle(
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black,
                            textAlign = TextAlign.Center,
                            fontFamily = FontFamily.Default
                        )
                    )
                    val iconX = center.x + iconSize * 0.7f
                    val iconY = center.y - iconSize / 2
                    drawText(
                        iconLayout,
                        topLeft = Offset(
                            iconX,
                            iconY
                        )
                    )
                }
            }
        }
        // Диалог для ввода веса ребра
        if (showWeightDialog && selectedVertices.size == 2) {
            AlertDialog(
                onDismissRequest = { showWeightDialog = false; selectedVertices = emptyList() },
                title = { Text("Введите вес ребра") },
                text = {
                    OutlinedTextField(
                        value = edgeWeightInput,
                        onValueChange = { edgeWeightInput = it.filter { c -> c.isDigit() }.take(3) },
                        label = { Text("Вес (1-999)") }
                    )
                },
                confirmButton = {
                    Button(onClick = {
                        val weight = edgeWeightInput.toIntOrNull()
                        if (weight != null && weight in 1..999 && onAddEdge != null) {
                            onAddEdge(selectedVertices[0], selectedVertices[1], weight)
                            showWeightDialog = false
                            selectedVertices = emptyList()
                            edgeWeightInput = ""
                        }
                    }) { Text("OK") }
                },
                dismissButton = {
                    Button(onClick = {
                        showWeightDialog = false
                        selectedVertices = emptyList()
                        edgeWeightInput = ""
                    }) { Text("Отмена") }
                }
            )
        }
        // Диалог для редактирования веса ребра
        if (showEditWeightDialog && selectedEdge != null) {
            AlertDialog(
                onDismissRequest = { showEditWeightDialog = false },
                title = { Text("Редактировать вес ребра") },
                text = {
                    OutlinedTextField(
                        value = editWeightInput,
                        onValueChange = { editWeightInput = it.filter { c -> c.isDigit() }.take(3) },
                        label = { Text("Вес (1-999)") }
                    )
                },
                confirmButton = {
                    Button(onClick = {
                        val weight = editWeightInput.toIntOrNull()
                        val (sourceId, targetId) = selectedEdge!!
                        if (weight != null && weight in 1..999 && onAddEdge != null) {
                            // Удаляем старое ребро и добавляем новое с новым весом
                            onDeleteEdge?.invoke(sourceId, targetId)
                            onAddEdge(sourceId, targetId, weight)
                            showEditWeightDialog = false
                        }
                    }) { Text("OK") }
                },
                dismissButton = {
                    Button(onClick = {
                        showEditWeightDialog = false
                    }) { Text("Отмена") }
                }
            )
        }
    }
}

// Вспомогательная функция для расстояния от точки до отрезка
fun distancePointToSegment(p: Offset, a: Offset, b: Offset): Float {
    val ab = b - a
    val ap = p - a
    val abLen = ab.getDistance()
    if (abLen == 0f) return ap.getDistance()
    val t = ((ap.x * ab.x + ap.y * ab.y) / (abLen * abLen)).coerceIn(0f, 1f)
    val proj = Offset(a.x + ab.x * t, a.y + ab.y * t)
    return (p - proj).getDistance()
} 