package logic

import org.apache.poi.hssf.usermodel.HSSFDateUtil
import org.apache.poi.ss.usermodel.CellType
import org.apache.poi.ss.usermodel.FormulaEvaluator
import org.apache.poi.ss.usermodel.Row
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern
import kotlin.collections.HashMap
import kotlin.collections.LinkedHashMap

/**
 * @param row Row from xlsx file
 * @param evaluator Evaluator used to handle cells with formulas
 * @param profile List of properties of either purchases or sales
 */
open class Invoice(row: Row, evaluator: FormulaEvaluator, profile: List<String>){

    val properties = HashMap<String, Any?>()
    private var valid: Boolean = true

    init {
        try {
            row.forEachIndexed{index, cell ->
                /* If property is valid and not skip */
                if (index < profile.size && !profile[index].contains("Skip"))
                    properties[profile[index]] = when(cell.cellType){
                        CellType.STRING -> cell.stringCellValue
                        /* If numeric cell is formated it is assumed that is a date, if not
                           the number is rounded to 2 decimals */
                        CellType.NUMERIC ->
                            if (HSSFDateUtil.isCellDateFormatted(cell))
                                formatDate(cell.dateCellValue)
                            else round(cell.numericCellValue)
                        CellType.FORMULA -> formula(evaluator.evaluate(cell).formatAsString())
                        /* Empty cells are allowed only for PVM properties, then it is assumed as 0 */
                        else -> if(profile[index].contains("PVM")) 0.0 else FLAG_FAILED
                    }
            }
        } catch (e: IllegalArgumentException){
            this.valid = false
        }

        if(valid())
            process()
    }

    /**
     * @param input Evaluted formula parsed to string
     * @return Processed formula cell
     */
    private fun formula(input: String) : Any? =
        try {
             /* Trying to parse a date from evaluated formula */
             formatDate(Date(input))
        } catch (e: Exception){
             /* If evaluated cell is not a date, trying to parse a number, if not returning the input itself (or FLAG_FAILED) */
             input.toDoubleOrNull()?.let { round(it) }?:input
        }


    fun valid() : Boolean = !properties.containsValue(FLAG_FAILED) && properties.containsKey("Name") && properties.containsKey("InvoiceNo") && valid

    /**
     * Processing parsed properties
     */
    private fun process(){
        properties.forEach{ (key, value) ->
            properties[key] = when(key){
                /* Creating total's node for PVM properties */
                "PVM1","PVM2","PVM25" -> if(value is Double) createTaxable(key,value) else FLAG_FAILED
                "InvoiceNo" ->
                    if (value is String)
                        value.toDoubleOrNull()?.run{ this.toInt() }?:run { if(Pattern.compile( "[0-9]" ).matcher(value).find()) value else FLAG_FAILED }
                    else (value as Double).toInt()
                else -> value
            }
        }
    }

    /**
     * Creating a map required to create total's node
     * @param type Property's name
     * @param value Taxable value
     * @return Map of total's node properties
     */
    private fun createTaxable(type: String, value: Double) : LinkedHashMap<String,String>{
        val percentage : Int = if(type=="PVM2") 9 else 21
        return LinkedHashMap<String,String>().apply {
            this+= "TaxableValue" to round(value).toString()
            this+= "TaxCode" to type
            this+= "TaxPercentage" to percentage.toString()
            this+= "Amount" to round(value*percentage/100.0).toString()
        }
    }

    private fun formatDate(c: Date): String= SimpleDateFormat("yyyy/MM/dd").format(c).replace("/","-")

    private fun round(value: Double) : Double {
        return BigDecimal(value).setScale(2, RoundingMode.HALF_UP).toDouble()
    }

    companion object {
        const val FLAG_FAILED = "FAILED"
        val keys = arrayOf("InvoiceNo","Name","InvoiceDate","VATRegistrationNumber","Total","PVM1","PVM2","PVM25","Skip")
    }
}
