package ui

import javafx.geometry.Pos
import javafx.scene.paint.Color
import javafx.scene.paint.CycleMethod
import javafx.scene.paint.LinearGradient
import javafx.scene.paint.Stop
import tornadofx.*

class StyleSheet : Stylesheet() {

    companion object {
        val saveButton by cssclass()
        val dragLabel by cssclass()
        val header by cssclass()
        val singleButton by cssclass()
        val plus by cssclass()
        val minus by cssclass()
        val mediumButton by cssclass()
        val accent = c("#ff5050")
        private val accentLight = c("#ff0066")
        private val smallFont = loadFont("/fonts/sf.otf", 10.0)!!
        private val mainFont = loadFont("/fonts/sf.otf", 30.0)!!
        val mediumFont = loadFont("/fonts/sf.otf", 20.0)!!
        val gradient = LinearGradient(0.0, 0.0, 300.0, 300.0, false, CycleMethod.NO_CYCLE, Stop(0.0, accent), Stop(1.0, accentLight))
        val smallText = mixin {
            textFill = c(104,112,127)
            font = smallFont
        }

    }

    init {

        val buttonBase = mixin {
            focusColor = Color.TRANSPARENT
            textFill = Color.WHITE
            backgroundColor += gradient
            borderWidth += box(2.px)
            borderColor += box(Color.TRANSPARENT)
            borderRadius += box(5.px)
            backgroundRadius += box(5.px)
            and(hover) {
                backgroundColor += Color.WHITE
                borderColor += box(gradient)
                textFill = accent
            }
        }

        textField{
            focusColor = Color.TRANSPARENT
            borderColor +=box(gradient)
            backgroundColor += Color.WHITE
            borderRadius +=box(5.px)
            backgroundRadius +=box(5.px)
            +smallText
        }

        listView{
            focusColor = Color.TRANSPARENT
        }
        listCell{
            focusColor = Color.TRANSPARENT
            highlightFill = Color.TRANSPARENT
            and(selected) {
                backgroundColor += Color.TRANSPARENT
            }
            +smallText
        }



        button{
            backgroundColor += Color.WHITE
            borderRadius +=box(5.px)
            backgroundRadius +=box(5.px)
            and(plus){
                textFill = c("#00cc00")
                borderColor += box(c("#00cc00"))
            }
            and(minus){
                textFill = c("#cc0000")
                borderColor += box(c("#cc0000"))
            }
        }

        tab {
            backgroundColor += accent
            focusColor = Color.WHITE
            font = smallFont
        }

        tabLabel {
            textFill = Color.WHITE
            focusColor = Color.TRANSPARENT
        }

        header {
            textFill = Color.WHITE
            font= mainFont
            padding = box(10.px,15.px)
        }

        dragLabel {
            padding = box(20.px)
            borderColor += box(accent)
            prefHeight = 410.px
            borderWidth += box(3.px)
            textFill = accent
            backgroundColor +=Color.WHITE
            alignment = Pos.CENTER
            font = mainFont
        }

       saveButton {
            prefWidth = 300.px
            prefHeight = 70.px
            +buttonBase
            font = mainFont
        }

        mediumButton{
            prefHeight = 30.px
            prefWidth = 150.px
            +buttonBase
            font = mediumFont
            padding = box(10.px,5.px)
        }

        singleButton {
            prefHeight = 70.px
            +buttonBase
            font = mainFont
        }
    }
}