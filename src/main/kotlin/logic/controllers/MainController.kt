package logic.controllers
import javafx.beans.property.SimpleStringProperty
import javafx.stage.StageStyle
import logic.XLSXHandler
import logic.XMLHandler
import tornadofx.Controller
import ui.ErrorFragment
import ui.ProfileWindow
import java.util.regex.Pattern

class MainController : Controller() {
    val urlProperty = SimpleStringProperty("Drop an xlsx file")
    private val xml = XMLHandler()
    private var path : String? = null

    fun bringOutProfile() = ProfileWindow().openWindow(resizable = false, stageStyle = StageStyle.UTILITY)

    fun setUrl ( url:String ){
        path = url.substring(1,url.length-1).replace(Pattern.quote("\\"),"/")
        urlProperty.value=path!!.split("/").last()
    }

    fun generate(sheet: Int){
        if(path.isNullOrEmpty()){
            val params : Pair<String, String> = "message" to "File path is empty"
            find<ErrorFragment>(params).openModal(block = true, resizable = false, stageStyle = StageStyle.UTILITY)
            return
        }
        if(ProfileController.profileExists())
            runAsync {
                val profile = ProfileController.profile()
                val invoices = XLSXHandler.parseXLSX(path!!, sheet, if(sheet==0) profile.optionsSale else profile.optionsPurchase)
                xml.generate(profile.id, invoices, primaryStage, if(sheet==0) XMLHandler.salesParams else XMLHandler.purchaseParams)
            }
        else bringOutProfile()

    }

}