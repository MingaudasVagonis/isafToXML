package logic

import javafx.stage.FileChooser
import javafx.stage.Stage
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.Node
import tornadofx.runAsync
import tornadofx.ui
import java.text.SimpleDateFormat
import java.time.YearMonth
import java.util.*
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.OutputKeys
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult
import kotlin.reflect.KFunction

class XMLHandler{
    private val dates : Array<String>
    private val nodes = NodeHandler()

    init {
        /* Program handles the previous month */
        val prev = Calendar.getInstance()
        prev.add(Calendar.MONTH, -1)
        val yearMonth = YearMonth.of(prev.get(Calendar.YEAR), prev.get(Calendar.MONTH) + 1)
        val base = SimpleDateFormat("yyyy-MM").format(prev.time)
        /* Array consists of month's start, end and year-month string */
        this.dates = arrayOf("$base-01","$base-${yearMonth.lengthOfMonth()}",base.substring(2).replace("-",""))
    }

    /**
     * Structures invoices into an acceptable xml format
     * @param ID User's registration number
     * @param invoices Parsed invoices from excel file
     * @param stage Primary javafx stage
     * @param params NodeParams object
     * */
    fun generate(ID: String, invoices: List<Invoice?>, stage: Stage, params: NodeParams){
        try {
            val document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument()
            val root = document.createElementNS("http://www.vmi.lt/cms/imas/isaf", "iSAFFile")
            root.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance")
            root.appendChild(nodes.header(document, params.type, ID, this.dates))
            /* Creating 'MasterFiles' node which contains either 'Customers' or 'Suppliers Node
             which stores a copy of all customers/suppliers */
            val customers = document.createElement("MasterFiles").let {
                it.appendChild(document.createElement(params.customers))
                it.firstChild as Element
            }
            root.appendChild(customers.parentNode)
            /* Calls eather 'sales' or 'purchases' function of NodeHandler which parses
             invoices into required node */
            root.appendChild(params.func.call(nodes, document, invoices, customers))
            document.appendChild(root)
            /* Output to XML */
            print(stage, params.type, document)
        } catch (e: Exception){
            println(e.printStackTrace())
        }
    }

    /**
     * Prints created document into a file
     * @param stage Primary javafx stage to call a dialog
     * @param type Differentiator string for the file name
     * @param document Document to print
     */
    private fun print(stage: Stage, type: String, document: Document){
        try {
            val fileChooser = FileChooser()
            fileChooser.initialFileName="$type${this.dates[2]}.xml"
            /* Required so that the dialog wouldn't run on tornado.fx thread */
            runAsync {  } ui {
               fileChooser.showSaveDialog(stage).let {
                   val source = DOMSource(document)
                   val trsnformer = TransformerFactory.newInstance().newTransformer()
                   trsnformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
                   trsnformer.transform(source, StreamResult(it))
               }
           }
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    companion object {
        val purchaseParams = NodeParams("P","Suppliers", NodeHandler::purchases )
        val salesParams = NodeParams("S","Customers", NodeHandler::sales )
        data class NodeParams(val type: String, val customers: String, val func: KFunction<Node> )
    }
}