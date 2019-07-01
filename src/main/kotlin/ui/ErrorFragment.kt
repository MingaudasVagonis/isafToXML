package ui

import javafx.scene.layout.Priority
import javafx.scene.text.TextAlignment
import tornadofx.*

class ErrorFragment : Fragment("Error") {
    private val message: String by param()

    override val root = vbox {
        label(message) {
            vboxConstraints { margin = insets(left=20.0, right=20.0, top= 10.0) }
            style{
                textAlignment = TextAlignment.CENTER
                font = StyleSheet.mediumFont
                textFill = StyleSheet.accent
            }
        }
        hbox {
            vboxConstraints { margin = insets(10.0) }
            region { hgrow = Priority.ALWAYS }
            button("Close") {
                addClass(StyleSheet.mediumButton)
                action {
                    close()
                }
            }
            region { hgrow = Priority.ALWAYS }
        }

    }
}