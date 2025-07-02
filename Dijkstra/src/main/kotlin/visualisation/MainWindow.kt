package visualisation

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import controller.AppController
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.layout.onGloballyPositioned
import javax.swing.JFileChooser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.delay
import androidx.compose.ui.Alignment
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Menu
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState

@Composable
fun MainWindowContent(controller: AppController) {
    val addVertexMode = remember { mutableStateOf(false) }
    var canvasSize by remember { mutableStateOf(Size.Zero) }
    val density = LocalDensity.current
    var showFileDialog by remember { mutableStateOf(false) }
    var setStartVertexMode by remember { mutableStateOf(false) }
    var animationTick by remember { mutableStateOf(0L) }
    var speedValue by remember { mutableStateOf(0.5f) } // 0.0..1.0
    val minDelay = 1000L
    val maxDelay = 5000L
    val animationDelay = ((1.0f - speedValue) * (maxDelay - minDelay) + minDelay).toLong()
    val isStartPauseEnabled = controller.graph.vertecies.size > 1 && controller.startVertexId != null
    val isSpeedEnabled = controller.isRunning || !controller.isRunning
    var showFullTable by remember { mutableStateOf(false) }
    val isShowTableEnabled = controller.dijkstraResult != null && (controller.isPaused || !controller.isRunning)
    val isResetEnabled = controller.dijkstraResult != null

    fun handleStartPause() {
        when {
            !controller.isRunning -> controller.startDijkstraAnimation()
            controller.isRunning && !controller.isPaused -> controller.pauseDijkstraAnimation()
            controller.isRunning && controller.isPaused -> controller.resumeDijkstraAnimation()
        }
    }

    fun handleToEnd() {
        controller.goToEnd()
    }

    val isToEndEnabled = controller.dijkstraResult?.steps?.isNotEmpty() == true && controller.currentStep < (controller.dijkstraResult?.steps?.size ?: 1) - 1

    // --- Анимация шагов алгоритма Дейкстры ---
    androidx.compose.runtime.LaunchedEffect(controller.isRunning, controller.isPaused, controller.currentStep, animationDelay) {
        while (controller.isRunning && !controller.isPaused) {
            kotlinx.coroutines.delay(animationDelay)
            controller.nextDijkstraStep()
        }
    }

    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(340.dp)
                    .onGloballyPositioned { coordinates ->
                        canvasSize = Size(
                            coordinates.size.width.toFloat(),
                            coordinates.size.height.toFloat()
                        )
                    }
            ) {
                val paddingPx = with(density) { (24.dp + 2.dp + 6.dp).toPx() }
                GraphCanvas(
                    vertices = controller.graph.vertecies.map {
                        if (it.id == controller.startVertexId) it.copy(color = graph.VertexColor.YELLOW)
                        else it.copy(color = graph.VertexColor.GRAY)
                    },
                    edges = controller.graph.edges,
                    currentStepData = controller.dijkstraResult?.steps?.getOrNull(controller.currentStep),
                    onCanvasClick = if (!controller.isRunning && addVertexMode.value) { x, y ->
                        if (
                            x > paddingPx && x < canvasSize.width - paddingPx &&
                            y > paddingPx && y < canvasSize.height - paddingPx
                        ) {
                            controller.addVertex(x, y)
                        }
                    } else if (!controller.isRunning && setStartVertexMode) { x, y ->
                        val clickedVertex = controller.graph.vertecies.find {
                            val dx = it.xCoordinate - x
                            val dy = it.yCoordinate - y
                            dx * dx + dy * dy <= 24f * 24f
                        }
                        if (clickedVertex != null) {
                            controller.setStartVertex(clickedVertex.id)
                            setStartVertexMode = false
                        }
                    } else null,
                    onAddEdge = if (!controller.isRunning) { { sourceId, targetId, weight ->
                        controller.addEdge(sourceId, targetId, weight)
                    }} else null,
                    onDeleteEdge = if (!controller.isRunning) { { sourceId, targetId ->
                        controller.deleteEdge(sourceId, targetId)
                    }} else null,
                    onDeleteVertex = if (!controller.isRunning) { { vertexId ->
                        controller.deleteVertex(vertexId)
                    }} else null
                )
            }
            // Легенда цветов
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(18.dp)) {
                LegendCircle(Color(0xFFFFD600), "старт")
                LegendCircle(Color(0xFF1976D2), "текущая")
                LegendCircle(Color(0xFF43A047), "посещено")
                LegendCircle(Color(0xFFB0B0B0), "не посещено")
                Row(verticalAlignment = Alignment.CenterVertically) {
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    var showInstruction by remember { mutableStateOf(false) }
                    androidx.compose.material.Button(onClick = { showInstruction = true }) {
                        Text("Инструкция")
                    }
                    if (showInstruction) {
                        InstructionDialog(onClose = { showInstruction = false })
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            DijkstraTable(controller)
            Spacer(modifier = Modifier.height(8.dp))
            StepExplanation(controller)
        }
        Spacer(modifier = Modifier.width(16.dp))
        ControlPanel(
            controller,
            onAddVertexClick = { if (!controller.isRunning) addVertexMode.value = !addVertexMode.value },
            isAddVertexModeActive = addVertexMode.value && !controller.isRunning,
            onLoadGraphClick = {
                if (!controller.isRunning && controller.graph.vertecies.isEmpty() && controller.graph.edges.isEmpty()) {
                    showFileDialog = true
                }
            },
            onSetStartVertexClick = {
                if (!controller.isRunning) setStartVertexMode = true
            },
            onStartPauseClick = { handleStartPause() },
            isStartPauseEnabled = isStartPauseEnabled,
            speedValue = speedValue,
            onSpeedChange = { speedValue = it },
            isSpeedEnabled = isSpeedEnabled,
            onToEndClick = { handleToEnd() },
            isToEndEnabled = isToEndEnabled,
            onShowTableClick = { showFullTable = !showFullTable },
            isShowTableEnabled = isShowTableEnabled,
            isTableShown = showFullTable,
            onResetAlgorithmClick = { controller.resetDijkstra() },
            isResetEnabled = isResetEnabled
        )
        if (showFileDialog) {
            LaunchedEffect(Unit) {
                val filePath = withContext(Dispatchers.IO) { showFileChooser() }
                showFileDialog = false
                if (filePath != null) {
                    controller.loadGraph(filePath)
                }
            }
        }
        if (showFullTable) {
            androidx.compose.ui.window.Window(
                onCloseRequest = { showFullTable = false },
                title = "Таблица алгоритма Дейкстры",
                resizable = true,
                alwaysOnTop = true
            ) {
                Box(Modifier.fillMaxSize().background(Color.White)) {
                    FullDijkstraTable(controller)
                }
            }
        }
    }
}

@Composable
fun StateTable() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(110.dp)
            .background(Color.White, shape = RoundedCornerShape(8.dp))
            .border(1.dp, Color(0xFFCCCCCC), shape = RoundedCornerShape(8.dp))
    )
}

@Composable
fun StepExplanation(controller: AppController) {
    val steps = controller.dijkstraResult?.steps
    val currentStep = controller.currentStep
    val isLast = steps != null && currentStep == steps.lastIndex && steps.isNotEmpty()

    val dijkstraExplanation = when {
        isLast -> "Конец алгоритма"
        else -> steps?.getOrNull(currentStep)?.action
    }

    val explanation = dijkstraExplanation ?: controller.notification ?: ""
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(36.dp)
            .background(Color.White, shape = RoundedCornerShape(8.dp))
            .border(1.dp, Color(0xFFCCCCCC), shape = RoundedCornerShape(8.dp)),
        contentAlignment = Alignment.CenterStart
    ) {
        Text(explanation, fontSize = 15.sp, color = Color(0xFF444444), modifier = Modifier.padding(start = 12.dp))
    }
}


fun showFileChooser(): String? {
    val chooser = JFileChooser()
    val result = chooser.showOpenDialog(null)
    return if (result == JFileChooser.APPROVE_OPTION) chooser.selectedFile.absolutePath else null
}

@Composable
fun LegendCircle(color: Color, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(22.dp)
                .background(color, shape = androidx.compose.foundation.shape.CircleShape)
                .border(1.dp, Color(0xFF888888), shape = androidx.compose.foundation.shape.CircleShape)
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(label, fontSize = 12.sp, color = Color.Gray)
    }
}

fun showMainWindow() = application {
    val controller = remember { AppController() }
    Window(
        onCloseRequest = ::exitApplication,
        title = "Dijkstra visualisation",
        resizable = true,
        state = androidx.compose.ui.window.rememberWindowState(width = 900.dp, height = 600.dp)
    ) {
        MainWindowContent(controller)
    }
}

@Composable
fun InstructionDialog(onClose: () -> Unit) {
    val instructionText = remember { mutableStateOf("") }
    LaunchedEffect(Unit) {
        try {
            instructionText.value = java.io.File("README_instruction.txt").readText()
        } catch (e: Exception) {
            instructionText.value = "Не удалось загрузить инструкцию."
        }
    }
    androidx.compose.ui.window.DialogWindow(
        onCloseRequest = onClose,
        title = "Инструкция",
        state = androidx.compose.ui.window.rememberDialogState(width = 700.dp, height = 500.dp)
    ) {
        val scrollState = rememberScrollState()
        Box(Modifier.fillMaxSize().background(Color.White).padding(24.dp)) {
            Column(Modifier.fillMaxSize()) {
                Box(Modifier.weight(1f).verticalScroll(scrollState)) {
                    Text(
                        instructionText.value,
                        fontSize = 15.sp,
                        color = Color(0xFF222222),
                        modifier = Modifier.fillMaxWidth().padding(end = 12.dp)
                    )
                }
                Spacer(Modifier.height(16.dp))
                androidx.compose.material.Button(onClick = onClose, modifier = Modifier.align(Alignment.End)) {
                    Text("Закрыть")
                }
            }
        }
    }
}