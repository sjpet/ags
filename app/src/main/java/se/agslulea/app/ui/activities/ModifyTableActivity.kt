package se.agslulea.app.ui.activities

import android.graphics.Typeface
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.TypedValue
import android.widget.*
import org.jetbrains.anko.toast

import se.agslulea.app.R
import se.agslulea.app.data.db.*

class ModifyTableActivity : AppCompatActivity() {

    private val db = AppDb()

    private var inserts: Array<Pair<String, Array<Pair<String, Any?>>>> = arrayOf()
    private var tableRows: Array<TableRow> = arrayOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_modify_table)

        val table = intent.getStringExtra("table")
        val adminLevel = intent.getIntExtra("adminLevel", 0)

        if (table == null || adminLevel < 1) {
            toast(R.string.action_not_allowed)
            finish()
        }

        val tableLayout = findViewById(R.id.table_table) as TableLayout
        val saveButton = findViewById(R.id.table_save_button) as Button

        val charWidths = when (table) {
            GroupTable.NAME -> listOf(30, 10)
            SportTable.NAME -> listOf(30, 5)
            FeeTable.NAME -> listOf(30, 5, 5)
            ActivityTypeTable.NAME -> listOf(30, 10)
            else -> listOf()
        }

        val columns = when (table) {
            GroupTable.NAME -> arrayOf(
                    Triple(GroupTable.GROUP, "STRING", getString(R.string.group_name)),
                    Triple(GroupTable.SHORTHAND, "STRING", getString(R.string.group_shorthand)),
                    Triple(GroupTable.IS_ACTIVE, "INT", getString(R.string.is_active)))
            SportTable.NAME -> arrayOf(
                    Triple(SportTable.SPORT, "STRING", getString(R.string.sport_name)),
                    Triple(SportTable.SHORTHAND, "STRING", getString(R.string.sport_shorthand)),
                    Triple(SportTable.IS_ACTIVE, "INT", getString(R.string.is_active)))
            FeeTable.NAME -> arrayOf(
                    Triple(FeeTable.FEE, "STRING", getString(R.string.fee_name)),
                    Triple(FeeTable.KEY, "STRING", getString(R.string.fee_shorthand)),
                    Triple(FeeTable.PERIOD, "INT", getString(R.string.fee_periodicity)),
                    Triple(FeeTable.IS_ACTIVE, "INT", getString(R.string.is_active)))
            ActivityTypeTable.NAME -> arrayOf(
                    Triple(ActivityTypeTable.TYPE, "STRING", getString(R.string.activity_type)),
                    Triple(ActivityTypeTable.SHORTHAND, "STRING",
                            getString(R.string.type_shorthand)),
                    Triple(ActivityTypeTable.IS_ACTIVE, "INT", getString(R.string.is_active)))
            else -> arrayOf()
        }

        val idColumn = when (table) {
            GroupTable.NAME -> GroupTable.ID
            SportTable.NAME -> SportTable.ID
            FeeTable.NAME -> FeeTable.ID
            ActivityTypeTable.NAME -> ActivityTypeTable.ID
            else -> "_id"
        }

        addHeaderRow(tableLayout, columns.map { x -> x.third })

        // add existing rows
        val rows = when (table) {
            GroupTable.NAME -> db.getGroups().drop(1)
            SportTable.NAME -> db.getSports().drop(1)
            FeeTable.NAME -> db.getFees()
            ActivityTypeTable.NAME -> db.getActivityTypes()
            else -> listOf()
        }
        if (adminLevel == 2) {
            for (row in rows) {
                addEditRow(tableLayout,
                        stringify(row.dropLast(1)),
                        charWidths,
                        row.last() as Int)
            }
        } else {
            for (row in rows) {
                addFixedRow(tableLayout, stringify(row.dropLast(1)), row.last() as Int)
            }
        }

        val nextId = rows.size + 1
        addLastRow(tableLayout, charWidths, table, columns, idColumn, nextId)

        saveButton.setOnClickListener {
            for ((insertTable, insertPairs) in inserts) {
                db.insert(insertTable, *insertPairs)
            }
            var rowIndex = 0
            for (tableRow in tableRows) {
                rowIndex += 1
                updateRow(table, idColumn, rowIndex, tableRow, columns)
            }
            finish()
        }

    }

    private fun updateRow(table: String,
                          idColumn: String,
                          rowId: Int,
                          tableRow: TableRow,
                          columns: Array<Triple<String, String, String>>) {
        var columnValuePairs: Array<Pair<String, Any?>> = arrayOf()
        var k = 0
        for ((column, type, _) in columns.dropLast(1)) {
            val thisView = tableRow.getChildAt(k)
            val value: String = when (thisView) {
                is EditText -> thisView.text.toString()
                is TextView -> thisView.text.toString()
                else -> "0"
            }

            columnValuePairs += if (type == "INT") {
                Pair(column, value.toLong())
            } else {
                Pair(column, value)
            }

            k += 1
        }
        val activeBox = tableRow.getChildAt(k) as CheckBox
        columnValuePairs += Pair(columns.last().first,
                if (activeBox.isChecked) { 1 } else { 0 })

        db.update(table, idColumn, rowId, *columnValuePairs)
    }

    private fun addHeaderRow(tableLayout: TableLayout, columns: List<String>) {

        val headerRow = layoutInflater.inflate(R.layout.table_row_template, tableLayout,
                false) as TableRow

        for (column in columns) {
            val textView = layoutInflater.inflate(R.layout.table_text_template,
                    headerRow, false) as TextView
            textView.text = column
            textView.typeface = Typeface.DEFAULT_BOLD
            headerRow.addView(textView)
        }

        tableLayout.addView(headerRow)
    }

    private fun addLastRow(tableLayout: TableLayout,
                           charWidths: List<Int>,
                           table: String,
                           columns: Array<Triple<String, String, String>>,
                           idColumn: String,
                           nextId: Int) {

        val lastRow = layoutInflater.inflate(R.layout.table_row_template, tableLayout,
                false) as TableRow
        val columnMap: MutableMap<String, EditText> = mutableMapOf()

        for ((column, charWidth) in columns.map { (x, _, _) -> x } zip charWidths) {
            val editText = EditText(this)
            editText.setEms(charWidth)
            editText.setSingleLine(true)
            lastRow.addView(editText)
            columnMap[column] = editText
        }

        val addButton = Button(this)
        addButton.text = getString(R.string.row_add)
        lastRow.addView(addButton)

        addButton.setOnClickListener {
            // Get string values
            var values: List<String> = listOf()
            var columnValuePairs: Array<Pair<String, Any?>> = arrayOf()
            for ((column, _) in columns.dropLast(1)) {
                val value = columnMap[column]!!.text.toString()
                values += value
                columnValuePairs += Pair(column, value)
            }

            // Last column (IsActive) is always true
            columnValuePairs += Pair(columns.last().first, 1)

            // Assign ID
            columnValuePairs += Pair(idColumn, nextId)
            inserts += Pair(table, columnValuePairs)

            // convert to an editable row and create a new last row
            tableLayout.removeView(lastRow)
            addEditRow(tableLayout, values, charWidths, 1)
            addLastRow(tableLayout, charWidths, table, columns, idColumn, nextId + 1)
        }

        tableLayout.addView(lastRow)
    }

    private fun addFixedRow(tableLayout: TableLayout,
                            values: List<String>,
                            isActive: Int) {

        val fixedRow = layoutInflater.inflate(R.layout.table_row_template, tableLayout,
                false) as TableRow

        for (value in values) {
            val textView = layoutInflater.inflate(R.layout.table_text_template,
                    fixedRow, false) as TextView
            textView.text = value
            fixedRow.addView(textView)
        }

        val activeBox = CheckBox(this)
        activeBox.isChecked = isActive == 1
        fixedRow.addView(activeBox)

        tableLayout.addView(fixedRow)
        tableRows += fixedRow
    }

    private fun addEditRow(tableLayout: TableLayout,
                           values: List<String>,
                           charWidths: List<Int>,
                           isActive: Int) {

        val editRow = layoutInflater.inflate(R.layout.table_row_template, tableLayout,
                false) as TableRow

        for ((value, charWidth) in values.zip(charWidths)) {
            val editText = EditText(this)
            editText.setEms(charWidth)
            editText.setText(value)
            editText.setSingleLine(true)
            editRow.addView(editText)
        }

        val activeBox = CheckBox(this)
        activeBox.isChecked = isActive == 1
        editRow.addView(activeBox)

        tableLayout.addView(editRow)
        tableRows += editRow
    }
}

fun stringify(anyList: List<Any?>) = anyList.map { x ->
    when (x) {
        is String -> x
        is Long -> x.toString()
        else -> ""
    }
}