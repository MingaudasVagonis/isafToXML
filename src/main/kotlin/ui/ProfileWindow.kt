package ui

import logic.controllers.ProfileController
import javafx.beans.property.ReadOnlyListWrapper
import javafx.geometry.Insets
import javafx.scene.Node
import javafx.scene.layout.Priority
import tornadofx.*


class ProfileWindow : View("Build Profile"){

    private val controller : ProfileController by inject()

    override val root = vbox {

        label("Build Profile") {
            style{ backgroundColor +=StyleSheet.gradient }
            addClass(StyleSheet.header)
            useMaxWidth =true

        }
        textfield(controller.idProperty) {
            useMaxWidth = true
            promptText = "ID"
            vboxConstraints { margin = Insets(10.0) }
            style{
                textFill = StyleSheet.accent
                font = StyleSheet.mediumFont
            }
        }
        tabpane {
            tab("Sales"){
                isClosable = false
                add(getOptions(controller.salesProperties){ option,type -> controller.callback(option,type,0) })
            }
            tab("Purchases"){
                isClosable = false
                add(getOptions(controller.purchasesProperties){ option,type -> controller.callback(option,type,1) })
            }
        }

        hbox {
            button ("Save Profile") {
                hboxConstraints { margin = insets(10) }
                addClass(StyleSheet.singleButton)
                action {controller.saveProfile{close()}}
                style { useMaxWidth = true }
                hgrow = Priority.ALWAYS
            }
        }

    }

    private fun getOptions(source: Array<ReadOnlyListWrapper<String>>, callback : (String,String) -> Unit) : Node{
        return hbox {
            listview(source[1]) {
                cellFormat {
                    graphic = hbox(15){
                        button("-") {
                            addClass(StyleSheet.minus)
                            action { callback(it, ProfileController.DELETE) }
                        }
                        vbox {
                            region { vgrow =Priority.ALWAYS }
                            label(it){
                                style { StyleSheet.smallText }
                            }
                            region { vgrow =Priority.ALWAYS }
                        }
                    }
                }
            }
            listview(source[0]) {
                cellFormat {
                    graphic = hbox {
                        vbox {
                            region { vgrow =Priority.ALWAYS }
                            label(it){
                                style{StyleSheet.smallText}
                            }
                            region { vgrow =Priority.ALWAYS }
                        }
                        region{
                            hgrow = Priority.ALWAYS
                        }
                        button("+") {
                            addClass(StyleSheet.plus)
                            action { callback(it, ProfileController.ADD) }
                        }
                    }
                }
            }
        }
    }

}

