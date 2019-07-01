package logic.controllers

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.stream.JsonReader
import javafx.beans.property.ReadOnlyListWrapper
import javafx.beans.property.SimpleStringProperty
import javafx.stage.StageStyle
import logic.Invoice
import tornadofx.Controller
import tornadofx.observable
import ui.ErrorFragment
import java.io.File
import java.io.FileReader
import java.util.List.copyOf


class ProfileController : Controller(){
    /* Profile properties arrays [ allAcceptableProperties, selectedProperties ] */
    val salesProperties = arrayOf( ReadOnlyListWrapper(copyOf(Invoice.keys.toMutableList()).toMutableList().observable()), ReadOnlyListWrapper(ArrayList<String>().observable()))
    val purchasesProperties = arrayOf( ReadOnlyListWrapper(copyOf(Invoice.keys.toMutableList()).toMutableList().observable()), ReadOnlyListWrapper(ArrayList<String>().observable()))
    /* ID property */
    val idProperty = SimpleStringProperty()

    init {
        parseProfile()
    }

    /**
     * UI callback when property from profile is deleted or added
     * @param option profile's property
     * @param type ADD or DELETE
     * @param order 0 -> sales, 1-> purchases
     */
    fun callback(option: String, type: String, order: Int){
        val targetProperties = if ( order == 0 ) salesProperties else purchasesProperties
        /* How many skips there already are */
        val skips = targetProperties[1].count{ it.contains("Skip") } + 1
        if(type == DELETE) {
            targetProperties[1].remove(option)
            /* If selected option is not skip option is added to source array */
            if(!option.contains("Skip"))
                targetProperties[0].add(option)
        } else if( option == "Skip" ){
            /* Skip option is added to selected array but also left in source array */
            targetProperties[1].add("$option: $skips")
        } else {
            targetProperties[1].add(option)
            targetProperties[0].remove(option)
        }
    }

    /**
     * Parses saved json file to profile it exists
     */
    private fun parseProfile(){
        try {
            if(profileExists()){
                val profile = profile()
                idProperty.value=profile.id
                profile.optionsPurchase.takeIf { it.isNotEmpty() }?.let { list->
                    purchasesProperties[0].removeIf {list.contains(it) && !it.contains("Skip") }
                    purchasesProperties[1].addAll(list)
                }
                profile.optionsSale.takeIf { it.isNotEmpty() }?.let { list->
                    salesProperties[0].removeIf {list.contains(it) && !it.contains("Skip") }
                    salesProperties[1].addAll(list)
                }
            }
        } catch (e : Exception) {
            println(e.toString())
        }

    }

    /**
     * Saves the profile to json located where the jar is
     */
    fun saveProfile(callback : ()->Unit){
        if(check()){
            runAsync {
                val gson = GsonBuilder().setPrettyPrinting().create()
                val profile = Profile(
                    idProperty.value,
                    salesProperties[1].value,
                    purchasesProperties[1].value
                )
                profileFile().writeText(gson.toJson(profile))
            } ui {
                /* Closing the window after saving */
                callback.invoke()
            }
        }
    }

    /**
     * Checking fields
     */
    private fun check() : Boolean{
        val params : Pair<String, String>
        when{
            idProperty.value.isNullOrEmpty() -> params= "message" to "ID is empty"
            salesProperties.isEmpty() -> params= "message" to "Sales are empty"
            purchasesProperties.isEmpty() -> params= "message" to "Purchases are empty"
            else -> return true
        }
        find<ErrorFragment>(params).openModal(block = true, resizable = false, stageStyle = StageStyle.UTILITY)
        return false
    }

    companion object {
        const val DELETE = "del"
        const val ADD = "add"
        private const val fileName = "isaf-profile.json"
        private fun profileFile() : File = File("/Users/mingaudasvagonis/Desktop/$fileName") //File(MainWindow::class.java.protectionDomain.codeSource.location.toURI()).path
        fun profileExists() : Boolean = profileFile().exists()
        fun profile() : Profile {
            val reader = JsonReader(FileReader(profileFile()))
            return Gson().fromJson(reader, Profile::class.java)
        }
    }

    data class Profile(
        val id: String,
        val optionsSale: List<String>,
        val optionsPurchase: List<String>
    )
}