package logic

import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileInputStream

class XLSXHandler {

    companion object {
        /**
         * Parses an excel file to invoices
         * @param path Xlsx file path
         * @param sheet Sheet's number, 0 -> sales and 1 -> purchases
         * @param profile List of property names in order to parse from excel row
         * @return List of parsed invoices
         */
        fun parseXLSX( path : String, sheet: Int, profile: List<String>) : List<Invoice?>{
            val workbook = XSSFWorkbook(FileInputStream(File(path)))
            /* Evaluator used to handle cells with formulas */
            val evaluator = workbook.creationHelper.createFormulaEvaluator()
            val iterator = workbook.getSheetAt(sheet).iterator()
            /* Adding to the list only if the invoice is valid */
            return ArrayList<Invoice?>().apply{
                iterator.forEach {
                   this+= Invoice(it,evaluator,profile).takeIf{invoice ->  invoice.valid()}
                }
            }
        }
    }
}