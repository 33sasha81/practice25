import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import controller.AppController
import visualisation.ControlPanel
import visualisation.MainWindowContent

fun main() = application {
    val appController = remember { AppController() }

    Window(onCloseRequest = ::exitApplication, title = "Dijkstra") {
        MainWindowContent(appController)
    }
}