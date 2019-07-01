package ui

import javafx.scene.input.TransferMode
import javafx.scene.layout.Priority
import javafx.scene.paint.Color
import logic.controllers.MainController
import tornadofx.*

class MainWindow : View("isafToXML") {

    private val controller : MainController by inject()

    override val root = vbox {
        hbox {
            style{ backgroundColor += StyleSheet.gradient }
            label("isafToXML") { addClass(StyleSheet.header) }
            region { hgrow = Priority.ALWAYS }
            vbox{
                region { vgrow = Priority.ALWAYS }
                button(graphic = imageview("/icons/settings.png"){
                    style{
                        fitWidth = 50.0
                        fitHeight = 50.0
                    }
                }){
                   style{ backgroundColor += Color.TRANSPARENT }
                   action { controller.bringOutProfile() }
                }
                region { vgrow = Priority.ALWAYS}
            }

        }

        label(controller.urlProperty) {
            addClass(StyleSheet.dragLabel)
            useMaxWidth = true
            vboxConstraints { margin = insets(top=10,left=10,right=10) }
            setOnDragOver {
                event-> if (event.gestureSource != this@label && event.dragboard.hasFiles())
                            event.acceptTransferModes(TransferMode.COPY_OR_MOVE[0])
                        event.consume()
            }
            setOnDragDropped {
                event-> val db = event.dragboard
                        if (db.hasFiles())
                            controller.setUrl(db.files.toString())
            }
        }

        hbox {
            useMaxWidth = true
            button ("Sales") {
                hboxConstraints { margin = insets(10) }
                addClass(StyleSheet.saveButton)
                action { controller.generate(0) }
            }
            button ("Purchases") {
                hboxConstraints { margin = insets(10,10,10) }
                addClass(StyleSheet.saveButton)
                action { controller.generate(1) }
            }
        }
    }
}