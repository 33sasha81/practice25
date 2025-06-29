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

@Composable
fun MainWindowContent(controller: AppController) {
    val addVertexMode = remember { mutableStateOf(false) } // Режим добавления вершины
    var canvasSize by remember { mutableStateOf(Size.Zero) } // Размер холста графа
    val density = LocalDensity.current // Для преобразования dp в пиксели
    var showFileDialog by remember { mutableStateOf(false) } // Показать диалог выбора файла
    var setStartVertexMode by remember { mutableStateOf(false) } // Режим выбора стартовой вершины
    var startVertexId by remember { mutableStateOf<Int?>(null) } // ID стартовой вершины

    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Левая колонка (граф, таблица состояний, пояснения)
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
        ) {
            // Контейнер для холста графа
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(340.dp)
                    .onGloballyPositioned { coordinates ->
                        // Запоминаем размеры холста при их изменении
                        canvasSize = Size(
                            coordinates.size.width.toFloat(),
                            coordinates.size.height.toFloat()
                        )
                    }
            ) {
                // Рассчитываем отступы в пикселях
                val paddingPx = with(density) { (24.dp + 2.dp + 6.dp).toPx() }

                // Компонент отображения графа
                GraphCanvas(
                    vertices = controller.graph.vertecies.map {
                        if (it.id == startVertexId) it.copy(color = org.example.graph.VertexColor.YELLOW)
                        else it.copy(color = org.example.graph.VertexColor.GRAY)
                    },
                    edges = controller.graph.edges,
                    // Обработчик клика по холсту
                    onCanvasClick = if (addVertexMode.value) { x, y ->
                        // Проверяем, что клик внутри допустимой области
                        if (
                            x > paddingPx && x < canvasSize.width - paddingPx &&
                            y > paddingPx && y < canvasSize.height - paddingPx
                        ) {
                            controller.addVertex(x, y) // Добавляем вершину
                        }
                        addVertexMode.value = false // Выходим из режима добавления
                    } else if (setStartVertexMode) { x, y ->
                        // Поиск вершины, по которой кликнули
                        val clickedVertex = controller.graph.vertecies.find {
                            val dx = it.xCoordinate - x
                            val dy = it.yCoordinate - y
                            dx * dx + dy * dy <= 24f * 24f // Проверка попадания в круг вершины
                        }
                        if (clickedVertex != null) {
                            startVertexId = clickedVertex.id // Устанавливаем стартовую вершину
                            setStartVertexMode = false // Выходим из режима выбора
                        }
                    } else null,

                    // Обработчики операций с графом
                    onAddEdge = { sourceId, targetId, weight ->
                        controller.addEdge(sourceId, targetId, weight)
                    },
                    onDeleteEdge = { sourceId, targetId ->
                        controller.deleteEdge(sourceId, targetId)
                    },
                    onDeleteVertex = { vertexId ->
                        controller.deleteVertex(vertexId)
                        if (startVertexId == vertexId) startVertexId = null
                    }
                )
            }

            Spacer(modifier = Modifier.height(12.dp)) // Вертикальный отступ
            StateTable() // Таблица состояний
            Spacer(modifier = Modifier.height(8.dp)) // Вертикальный отступ
            StepExplanation() // Пояснение шагов
        }

        Spacer(modifier = Modifier.width(16.dp)) // Горизонтальный отступ

        // Панель управления справа
        ControlPanel(
            controller,
            onAddVertexClick = { addVertexMode.value = true }, // Включение режима добавления вершины
            onLoadGraphClick = {
                // Показываем выбор файла только если граф пустой
                if (controller.graph.vertecies.isEmpty() && controller.graph.edges.isEmpty()) {
                    showFileDialog = true
                }
            },
            onSetStartVertexClick = {
                setStartVertexMode = true // Включение режима выбора стартовой вершины
            }
        )

        // Обработка показа выбора файла
        if (showFileDialog) {
            LaunchedEffect(Unit) {
                val filePath = withContext(Dispatchers.IO) { showFileChooser() }
                showFileDialog = false
                if (filePath != null) {
                    controller.loadGraph(filePath) // Загружаем граф из файла
                }
            }
        }
    }
}

// Компонент таблицы состояний
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

// Компонент пояснения шагов
@Composable
fun StepExplanation() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(36.dp)
            .background(Color.White, shape = RoundedCornerShape(8.dp))
            .border(1.dp, Color(0xFFCCCCCC), shape = RoundedCornerShape(8.dp))
    )
}

// Функция показа выбора файла
fun showFileChooser(): String? {
    val chooser = JFileChooser()
    val result = chooser.showOpenDialog(null)
    return if (result == JFileChooser.APPROVE_OPTION) chooser.selectedFile.absolutePath else null
}

// Функция создания и показа главного окна приложения
fun showMainWindow() = application {
    val controller = remember { AppController() } // Создаем контроллер приложения

    Window(
        onCloseRequest = ::exitApplication, // Обработчик закрытия окна
        title = "Dijkstra visualisation", // Заголовок окна
        resizable = true, // Возможность изменять размер
        state = androidx.compose.ui.window.rememberWindowState(width = 900.dp, height = 600.dp)
    ) {
        MainWindowContent(controller) // Содержимое главного окна
    }
}