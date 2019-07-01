import ui.MainWindow
import ui.StyleSheet
import javafx.stage.Stage
import javafx.stage.StageStyle
import tornadofx.App
import tornadofx.launch
import tornadofx.reloadStylesheetsOnFocus

fun main(args: Array<String>) {
    launch<IsafApp>(args)
}

class IsafApp : App(MainWindow::class, StyleSheet::class){
    init {
        reloadStylesheetsOnFocus()
    }
    override fun start(stage: Stage) {
        stage.isResizable = false
        stage.initStyle(StageStyle.UTILITY)
        super.start(stage)
    }
}