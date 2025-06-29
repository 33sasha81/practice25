package visualisation

import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Slider
import androidx.compose.material.Text
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.style.TextAlign

import controller.AppController

@Composable
fun ControlPanel(
    controller: AppController, // Контроллер для управления графом
    onAddVertexClick: () -> Unit = {}, // Обработчик добавления вершины
    onLoadGraphClick: () -> Unit = {}, // Обработчик загрузки графа
    onSetStartVertexClick: () -> Unit = {} // Обработчик выбора начальной вершины
) {
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .width(250.dp)
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Основная колонка с кнопками управления
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            ControlButton(
                text = "Вставить вершину",
                height = 56.dp,
                onClick = {
                    println("Кнопка 'Вставить вершину' нажата")
                    onAddVertexClick()
                },
                enabled = true
            )

            ControlButton(
                text = "Задать начальную вершину",
                height = 56.dp,
                onClick = onSetStartVertexClick,
                enabled = true
            )

            ControlButton("Показать таблицу", height = 56.dp, enabled = false)

            ControlButton("Скачать граф",
            height = 56.dp,
            onClick = {
                println("Кнопка 'Скачать граф' нажата")
                controller.saveGraph()
            },
            enabled = true
            )

            ControlButton(
                text = "Загрузить граф",
                height = 56.dp,
                onClick = onLoadGraphClick,
                enabled = true
            )
        }

        // Нижняя панель с дополнительными кнопками
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            elevation = 4.dp
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Ряд с двумя кнопками управления анимацией
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    ControlButton(
                        text = "Старт/Пауза",
                        fontSize = 16.sp,
                        height = 56.dp,
                        enabled = false,
                        modifier = Modifier.weight(1f)
                    )

                    ControlButton(
                        text = "В конец",
                        fontSize = 16.sp,
                        height = 56.dp,
                        enabled = false,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(18.dp)) // Вертикальный отступ

                Text("Скорость")
                Slider(
                    value = 0.5f,
                    onValueChange = {},
                    enabled = false,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

// Настройки кнопки
@Composable
fun ControlButton(
    text: String,
    fontSize: androidx.compose.ui.unit.TextUnit = androidx.compose.ui.unit.TextUnit.Unspecified,
    height: androidx.compose.ui.unit.Dp = 40.dp,
    onClick: () -> Unit = {},
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .fillMaxWidth()
            .height(height)
    ) {
        Text(
            text,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            fontSize = fontSize
        )
    }
}