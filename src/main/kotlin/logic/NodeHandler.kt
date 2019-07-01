package logic

import org.w3c.dom.Document
import org.w3c.dom.Element
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.LinkedHashMap


class NodeHandler {

    /**
     * Creates a header node, mostly from required static information
     * @param document Document object needed to create a node
     * @param type Document type: P -> purchases, S -> sales
     * @param ID User's registration id
     * @param dates An array of month's start and end dates
     * @return Header node
     */
    fun header(document: Document, type: String, ID: String, dates: Array<String>) : Element =
        document.createElement("Header").apply {
            this.appendChild(document.createElement("FileDescription").apply {
                staticInfo(type,ID).forEach{ (key, value) -> node(this,key,value) }
                this.appendChild(document.createElement("SelectionCriteria").apply{
                    node(this,"SelectionStartDate",dates[0])
                    node(this,"SelectionEndDate",dates[1])
                })
            })
        }

    /**
     * Creates purchases node from invoices
     * @param document Document object needed to create a node
     * @param invoices List of purchase invoices
     * @param master MasterFiles node which stores all supliers
     * @return Node with purchases
     */
    fun purchases(document: Document, invoices: List<Invoice?>, master: Element): Element =
        document.createElement("SourceDocuments").apply {
            this.appendChild(document.createElement("PurchaseInvoices").apply {
                /* Creating the node only if the invoice is valid */
                invoices.filterNotNull().filter { it.valid() }.forEach {
                    this.appendChild(document.createElement("Invoice").apply {
                        val properties = it.properties
                        node(this, "InvoiceNo", properties["InvoiceNo"] )
                        /* Adding supplier node to invoice */
                        customer(this, "SupplierInfo", properties["Name"] as String, properties["VATRegistrationNumber"] as String)
                        /* Adding supplier node to MasterFiles */
                        customer(master, "Supplier", properties["Name"] as String, properties["VATRegistrationNumber"] as String)
                        sourceBase(this,properties, accountDate = true)
                    })
                }
            })
        }

    /**
     * Creates sales node from invoices
     * @param document Document object needed to create a node
     * @param invoices List of sales invoices
     * @param master MasterFiles node which stores all customers
     * @return Node with customers
     */
    fun sales(document: Document, invoices: List<Invoice?>, master: Element): Element =
        document.createElement("SourceDocuments").apply {
            this.appendChild(document.createElement("SalesInvoices").apply {
                /* Creating the node only if the invoice is valid */
                invoices.filterNotNull().filter { it.valid() }.forEach {
                    this.appendChild(document.createElement("Invoice").apply {
                        val properties = it.properties
                        node(this, "InvoiceNo", properties["InvoiceNo"] )
                        /* Adding customer node to invoice */
                        customer(this, "CustomerInfo", properties["Name"] as String)
                        /* Adding customer node to MasterFiles */
                        customer(master, "Customer", properties["Name"] as String)
                        sourceBase(this,properties, vat2 = true)
                    })
                }
            })
        }

    /**
     * Adds shared properties (mostly required but useless) to invoice
     * @param invoice Invoice node
     * @param properties Invoice's properties
     * @param accountDate whether RegistrationAccountDate node is needed
     * @param vat2 whether VATPointDate2 is needed
     */
    private fun sourceBase(invoice: Element, properties: HashMap<String, Any?>, accountDate: Boolean = false , vat2: Boolean = false){
        node(invoice, "InvoiceDate", properties["InvoiceDate"])
        node(invoice, "InvoiceType", "SF")
        node(invoice, "SpecialTaxation")
        node(invoice, "References")
        node(invoice, "VATPointDate", properties["InvoiceDate"])
        if(accountDate)
            node(invoice, "RegistrationAccountDate", properties["InvoiceDate"])
        totals(invoice, properties, vat2)
    }

    /**
     * Adds totals' information to the node
     * @param invoice Invoice node
     * @param properties Invoice's properties
     * @param vat2 whether VATPointDate2 is needed
     */
    @Suppress("UNCHECKED_CAST")
    private fun totals(invoice: Element, properties: HashMap<String, Any?>, vat2:  Boolean = false) =
        invoice.appendChild(invoice.ownerDocument.createElement("DocumentTotals").apply node@{
            /* Take properties that have totals information */
            properties.filter { it.key.contains("PVM") }.forEach { (key, value) ->
                this.appendChild(invoice.ownerDocument.createElement("DocumentTotal").apply {
                    (value as LinkedHashMap<String, String>).forEach { (key, value) -> node(this, key, value)}
                    if(vat2) node(this,"VATPointDate2", properties["InvoiceDate"])
                })

            }
        })

    /**
     * Adds customer/supplier node to parent
     * @param parent Parent node
     * @param parentName Name of the parent to be created (MasterFiles doesn't need 'Info' at the end)
     * @param name Customer's/Supplier's name
     * @param vat VAT registration number of supplier
     */
    private fun customer(parent: Element, parentName: String, name: String?, vat: String = "ND")
        = parent.appendChild(parent.ownerDocument.createElement(parentName).apply {
            node(this,"${parentName.replace("Info","")}ID")
            node(this,"VATRegistrationNumber", vat)
            node(this,"RegistrationNumber")
            node(this,"Country","LT")
            node(this,"Name",name)
        })

    /**
     * Utility function that appends a node
     * @param parent Parent node
     * @param key New node's name
     * @param value New node's text value
     */
    private fun node(parent: Element, key: String, value: Any? = "ND") =
        parent.appendChild(parent.ownerDocument.createElement(key).apply {
            value?.let { this.textContent = it.toString()}
        })

    /**
     * @param type Either P (purchases) or S (sales)
     * @return Part number used to differentiate parts in the header
     */
    private fun partNumber(type: String) : String{
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = System.currentTimeMillis()
        return type+SimpleDateFormat("MM-dd").format(calendar.time).replace("-","")
    }

    private fun today() : String{
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = System.currentTimeMillis()
        return SimpleDateFormat("yyyy-MM-dd/HH:mm:ss").format(calendar.time).replace("/","T")
    }

    /**
     * @param type Either P (purchases) or S (sales)
     * @param ID User's registration number
     * @return A map of static information required in the header
     */
    private fun staticInfo(type: String, ID: String) = LinkedHashMap<String,String>().apply {
        this+= "FileVersion" to "iSAF1.2"
        this+= "FileDateCreated" to today()
        this+= "DataType" to type
        this+= "SoftwareCompanyName" to "Mingaudas Vagonis"
        this+= "SoftwareName" to "mtrn|isafToXml"
        this+= "SoftwareVersion" to "4"
        this+= "RegistrationNumber" to ID
        this+= "NumberOfParts" to "2"
        this+= "PartNumber" to partNumber(type)
    }

}