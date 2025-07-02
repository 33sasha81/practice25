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
import androidx.compose.ui.graphics.Color
import androidx.compose.material.ButtonDefaults

import controller.AppController

@Composable
fun ControlPanel(
    controller: AppController,
    onAddVertexClick: () -> Unit = {},
    isAddVertexModeActive: Boolean = false,
    onLoadGraphClick: () -> Unit = {},
    onSetStartVertexClick: () -> Unit = {},
    onStartPauseClick: () -> Unit = {},
    isStartPauseEnabled: Boolean = true,
    speedValue: Float = 0.5f,
    onSpeedChange: (Float) -> Unit = {},
    isSpeedEnabled: Boolean = true,
    onToEndClick: () -> Unit = {},
    isToEndEnabled: Boolean = true,
    onShowTableClick: () -> Unit = {},
    isShowTableEnabled: Boolean = true,
    isTableShown: Boolean = false,
    onResetAlgorithmClick: () -> Unit = {},
    isResetEnabled: Boolean = true
) {
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .width(250.dp)
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            ControlButton(
                text = "Добавить вершины",
                height = 56.dp,
                onClick = {
                    onAddVertexClick()
                },
                enabled = true,
                buttonColors = if (isAddVertexModeActive) ButtonDefaults.buttonColors(backgroundColor = Color(0xFFE0E0E0)) else ButtonDefaults.buttonColors()
            )
            ControlButton(
                text = "Задать начальную вершину",
                height = 56.dp,
                onClick = onSetStartVertexClick,
                enabled = true
            )
            ControlButton(
                text = if (isTableShown) "Скрыть таблицу" else "Показать таблицу",
                height = 56.dp,
                onClick = onShowTableClick,
                enabled = isShowTableEnabled
            )
            ControlButton(
                text = "Сброс алгоритма",
                height = 56.dp,
                onClick = onResetAlgorithmClick,
                enabled = isResetEnabled
            )
            ControlButton("Сохранить граф",
            height = 56.dp,
            onClick = {
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
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    ControlButton(
                        text = "Старт/Пауза",
                        fontSize = 16.sp,
                        height = 56.dp,
                        enabled = isStartPauseEnabled,
                        modifier = Modifier.weight(1f),
                        onClick = onStartPauseClick
                    )
                    ControlButton(
                        text = "В конец",
                        fontSize = 16.sp,
                        height = 56.dp,
                        enabled = isToEndEnabled,
                        modifier = Modifier.weight(1f),
                        onClick = onToEndClick
                    )
                }
                Spacer(modifier = Modifier.height(18.dp))
                Text("Скорость")
                Slider(
                    value = speedValue,
                    onValueChange = onSpeedChange,
                    enabled = isSpeedEnabled,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
fun ControlButton(
    text: String,
    fontSize: androidx.compose.ui.unit.TextUnit = androidx.compose.ui.unit.TextUnit.Unspecified,
    height: androidx.compose.ui.unit.Dp = 40.dp,
    onClick: () -> Unit = {},
    enabled: Boolean = true,
    modifier: Modifier = Modifier,
    buttonColors: androidx.compose.material.ButtonColors = ButtonDefaults.buttonColors()
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .fillMaxWidth()
            .height(height),
        colors = buttonColors
    ) {
        Text(
            text,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            fontSize = fontSize
        )
    }
}