package se.agslulea.app.ui.activities

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
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
        val level = intent.getIntExtra("level", 0)

        if (table == null || level < 1) {
            toast(R.string.action_not_allowed)
            finish()
        }

        val tableLayout = findViewById(R.id.table_table) as TableLayout
        val saveButton = findViewById(R.id.table_save_button) as Button

        val headers = when (table) {
            GroupTable.NAME -> listOf(getString(R.string.group_name))
            SportTable.NAME -> listOf(getString(R.string.sport_name),
                    getString(R.string.sport_shorthand))
            FeeTable.NAME -> listOf(getString(R.string.fee_name),
                    getString(R.string.fee_shorthand), getString(R.string.fee_periodicity))
            ActivityTypeTable.NAME -> listOf(getString(R.string.activity_type))
            else -> listOf()
        }

        val charWidths = when (table) {
            GroupTable.NAME -> listOf(30)
            SportTable.NAME -> listOf(30, 5)
            FeeTable.NAME -> listOf(30, 5, 5)
            ActivityTypeTable.NAME -> listOf(30)
            else -> listOf()
        }

        val columnNames = when (table) {
            GroupTable.NAME -> arrayOf(GroupTable.GROUP, GroupTable.IS_ACTIVE)
            SportTable.NAME -> arrayOf(SportTable.SPORT, SportTable.SHORTHAND,
                    SportTable.IS_ACTIVE)
            FeeTable.NAME -> arrayOf(FeeTable.FEE, FeeTable.KEY, FeeTable.IS_ACTIVE)
            ActivityTypeTable.NAME -> arrayOf(ActivityTypeTable.TYPE, ActivityTypeTable.IS_ACTIVE)
            else -> arrayOf()
        }

        val idColumn = when (table) {
            GroupTable.NAME -> GroupTable.ID
            SportTable.NAME -> SportTable.ID
            FeeTable.NAME -> FeeTable.ID
            ActivityTypeTable.NAME -> ActivityTypeTable.ID
            else -> "_id"
        }

        addHeaderRow(tableLayout, headers)

        // add existing rows
        val rows = when (table) {
            GroupTable.NAME -> db.getGroups()
            SportTable.NAME -> db.getSports()
            FeeTable.NAME -> db.getFees()
            ActivityTypeTable.NAME -> db.getActivityTypes()
            else -> listOf()
        }
        if (level == 1) {
            for (row in rows) {
                addEditRow(tableLayout,
                        stringify(row.dropLast(1)),
                        charWidths,
                        row.last() as Int,
                        columnNames)
            }
        } else {
            for (row in rows) {
                addFixedRow(tableLayout, stringify(row.dropLast(1)), row.last() as Int)
            }
        }

        val nextId = rows.size + 1
        addLastRow(tableLayout, charWidths, table, columnNames, idColumn, nextId)

        saveButton.setOnClickListener {
            for ((insertTable, insertPairs) in inserts) {
                db.insert(insertTable, *insertPairs)
            }
            var rowIndex = 0
            for (tableRow in tableRows) {
                rowIndex += 1
                updateRow(table, idColumn, rowIndex, tableRow, columnNames)
            }
            finish()
        }

    }

    private fun updateRow(table: String,
                          idColumn: String,
                          rowId: Int,
                          tableRow: TableRow,
                          columnNames: Array<String>) {
        var columnValuePairs: Array<Pair<String, Any?>> = arrayOf()
        var k = 0
        for (column in columnNames.dropLast(1)) {
            val thisView = tableRow.getChildAt(k)
            val value = if (thisView is EditText) {
                thisView.text.toString()
            } else if (thisView is TextView) {
                thisView.text
            } else {
                ""
            }
            columnValuePairs += Pair(column, value)
            k += 1
        }
        val toggleButton = tableRow.getChildAt(k) as Button
        columnValuePairs += Pair(columnNames.last(),
                if (toggleButton.text == getString(R.string.row_deactivate)) { 1 } else { 0 })

        db.update(table, idColumn, rowId, *columnValuePairs)
    }

    private fun addHeaderRow(tableLayout: TableLayout, columns: List<String>) {

        val headerRow = layoutInflater.inflate(R.layout.table_row_template, tableLayout,
                false) as TableRow

        for (column in columns) {
            val textView = TextView(this)
            textView.text = column
            headerRow.addView(textView)
        }

        tableLayout.addView(headerRow)
    }

    private fun addLastRow(tableLayout: TableLayout,
                           charWidths: List<Int>,
                           table: String,
                           columnNames: Array<String>,
                           idColumn: String,
                           nextId: Int) {

        val lastRow = layoutInflater.inflate(R.layout.table_row_template, tableLayout,
                false) as TableRow
        val columnMap: MutableMap<String, EditText> = mutableMapOf()

        for ((columnName, charWidth) in columnNames zip charWidths) {
            val editText = EditText(this)
            editText.setEms(charWidth)
            lastRow.addView(editText)
            columnMap[columnName] = editText
        }

        val addButton = layoutInflater.inflate(R.layout.button_template, lastRow, false) as Button
        addButton.text = getString(R.string.row_add)
        lastRow.addView(addButton)

        addButton.setOnClickListener {
            // Get string values
            var values: List<String> = listOf()
            var columnValuePairs: Array<Pair<String, Any?>> = arrayOf()
            for (column in columnNames.dropLast(1)) {
                val value = columnMap[column]!!.text.toString()
                values += value
                columnValuePairs += Pair(column, value)
            }

            // Last column (IsActive) is always true
            columnValuePairs += Pair(columnNames.last(), 1)

            // Assign ID
            columnValuePairs += Pair(idColumn, nextId)
            inserts += Pair(table, columnValuePairs)

            // convert to an editable row and create a new last row
            tableLayout.removeView(lastRow)
            addEditRow(tableLayout, values, charWidths, 1, columnNames)
            addLastRow(tableLayout, charWidths, table, columnNames, idColumn, nextId + 1)
        }

        tableLayout.addView(lastRow)
    }

    private fun addFixedRow(tableLayout: TableLayout,
                            values: List<String>,
                            isActive: Int) {

        val fixedRow = layoutInflater.inflate(R.layout.table_row_template, tableLayout,
                false) as TableRow

        for (value in values) {
            val textView = TextView(this)
            textView.text = value
            fixedRow.addView(textView)
        }

        val toggleActiveButton = layoutInflater.inflate(R.layout.button_template, fixedRow,
                false) as Button
        if (isActive == 1) {
            toggleActiveButton.text = getString(R.string.row_deactivate)
        } else {
            toggleActiveButton.text = getString(R.string.row_activate)
        }
        fixedRow.addView(toggleActiveButton)

        toggleActiveButton.setOnClickListener {
            if (toggleActiveButton.text == getString(R.string.row_deactivate)) {
                toggleActiveButton.text = getString(R.string.row_activate)
            } else {
                toggleActiveButton.text = getString(R.string.row_deactivate)
            }
        }

        tableLayout.addView(fixedRow)
        tableRows += fixedRow
    }

    private fun addEditRow(tableLayout: TableLayout,
                           values: List<String>,
                           charWidths: List<Int>,
                           isActive: Int,
                           columnNames: Array<String>) {

        val editRow = layoutInflater.inflate(R.layout.table_row_template, tableLayout,
                false) as TableRow

        for ((value, charWidth) in values.zip(charWidths)) {
            val editText = EditText(this)
            editText.setEms(charWidth)
            editText.setText(value)
            editRow.addView(editText)
        }

        val toggleActiveButton = layoutInflater.inflate(R.layout.button_template, editRow,
                false) as Button
        if (isActive == 1) {
            toggleActiveButton.text = getString(R.string.row_deactivate)
        } else {
            toggleActiveButton.text = getString(R.string.row_activate)
        }
        editRow.addView(toggleActiveButton)

        toggleActiveButton.setOnClickListener {
            if (toggleActiveButton.text == getString(R.string.row_deactivate)) {
                toggleActiveButton.text = getString(R.string.row_activate)
            } else {
                toggleActiveButton.text = getString(R.string.row_deactivate)
            }
        }

        tableLayout.addView(editRow)
        tableRows += editRow
    }
}

fun stringify(anyList: List<Any?>) = anyList.map {
    x -> if (x is String) {
        x
    } else if (x is Long) {
        x.toString()
    } else {
        ""
    }
}