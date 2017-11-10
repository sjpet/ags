package se.agslulea.app.ui.activities

import android.content.Context
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import org.jetbrains.anko.toast

import se.agslulea.app.R
import se.agslulea.app.data.db.*

val colourFieldSpaces = "     "

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
                    Triple(ActivityTypeTable.COLOUR, "COLOUR", getString(R.string.activity_colour)),
                    Triple(ActivityTypeTable.IS_ACTIVE, "INT", getString(R.string.is_active)))
            else -> arrayOf()
        }

        val charWidths: List<Int>
        val idColumn: String
        val hasColourSpinner: Boolean
        val rows: List<List<Any>>

        when (table) {
            GroupTable.NAME -> {
                charWidths = listOf(20, 10)
                idColumn = GroupTable.ID
                hasColourSpinner = false
                rows = db.getGroups().drop(1)
            }
            SportTable.NAME -> {
                charWidths = listOf(20, 5)
                idColumn = SportTable.ID
                hasColourSpinner = false
                rows = db.getSports().drop(1)
            }
            FeeTable.NAME -> {
                charWidths = listOf(20, 5, 5)
                idColumn = FeeTable.ID
                hasColourSpinner = false
                rows = db.getFees()
            }
            ActivityTypeTable.NAME -> {
                charWidths = listOf(20, 10)
                idColumn = ActivityTypeTable.ID
                hasColourSpinner = true
                rows = db.getActivityTypes()
            }
            else -> {
                charWidths = listOf()
                idColumn = "_id"
                hasColourSpinner = false
                rows = listOf()
            }
        }

        addHeaderRow(tableLayout, columns.map { x -> x.third })
        val numberDropped = if (hasColourSpinner) { 2 } else { 1 }
        if (adminLevel == 2) {
            for (row in rows) {
                addEditRow(tableLayout,
                        stringify(row.dropLast(numberDropped)),
                        charWidths,
                        if (hasColourSpinner) { row[charWidths.size] as Int } else { null },
                        row.last() as Int)
            }
        } else {
            for (row in rows) {
                addFixedRow(tableLayout,
                        stringify(row.dropLast(numberDropped)),
                        if (hasColourSpinner) { row[charWidths.size] as Int } else { null },
                        row.last() as Int)
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
                is TextView -> if (type == "COLOUR") { "0" } else {thisView.text.toString() }
                is Spinner -> {
                    val spinnerSelected = thisView.selectedItem as Map<*, *>
                    (spinnerSelected[ColourTable.ID] as Int).toString()
                }
                else -> "0"
            }

            if (thisView !is TextView) {
                columnValuePairs += if (type != "STRING") {
                    Pair(column, value.toLong())
                } else {
                    Pair(column, value)
                }
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

        val colourSpinner: Spinner?
        if (columns[charWidths.size].second == "COLOUR") {
            colourSpinner = Spinner(this)
            colourSpinner.adapter = ColourSpinnerAdapter(this, db.getColours())
            lastRow.addView(colourSpinner)
        } else {
            colourSpinner = null
        }

        val addButton = Button(this)
        addButton.text = getString(R.string.row_add)
        lastRow.addView(addButton)

        addButton.setOnClickListener {
            // Get string values
            var values: List<String> = listOf()
            var columnValuePairs: Array<Pair<String, Any?>> = arrayOf()
            val numberDropped = if (colourSpinner == null) { 1 } else { 2 }
            for ((column, _) in columns.dropLast(numberDropped)) {
                val value = columnMap[column]!!.text.toString()
                values += value
                columnValuePairs += Pair(column, value)
            }

            val colourId: Int?
            if (colourSpinner != null) {
                val colourSpinnerSelected = colourSpinner.selectedItem as Map<*, *>
                colourId = colourSpinnerSelected[ColourTable.ID] as Int
                columnValuePairs += Pair(columns[charWidths.size].first, colourId)
            } else {
                colourId = null
            }

            // Last column (IsActive) is always true
            columnValuePairs += Pair(columns.last().first, 1)

            // Assign ID
            columnValuePairs += Pair(idColumn, nextId)
            inserts += Pair(table, columnValuePairs)

            // convert to an editable row and create a new last row
            tableLayout.removeView(lastRow)
            addEditRow(tableLayout, values, charWidths, colourId, 1)
            addLastRow(tableLayout, charWidths, table, columns, idColumn, nextId + 1)
        }

        tableLayout.addView(lastRow)
    }

    private fun addFixedRow(tableLayout: TableLayout,
                            values: List<String>,
                            colourId: Int?,
                            isActive: Int) {

        val fixedRow = layoutInflater.inflate(R.layout.table_row_template, tableLayout,
                false) as TableRow

        for (value in values) {
            val textView = layoutInflater.inflate(R.layout.table_text_template,
                    fixedRow, false) as TextView
            textView.text = value
            fixedRow.addView(textView)
        }

        if (colourId != null) {
            val colourField = layoutInflater.inflate(R.layout.table_text_template,
                    fixedRow, false) as TextView
            colourField.text = colourFieldSpaces
            colourField.background = ColorDrawable(db.getColourValue(colourId))
            fixedRow.addView(colourField)
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
                           colourId: Int?,
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

        if (colourId != null) {
            val colourSpinner = Spinner(this)
            val colours = db.getColours()
            colourSpinner.adapter = ColourSpinnerAdapter(this, colours)
            colourSpinner.setSelection(colours.map { it[ColourTable.ID] as Int }.indexOf(colourId))
            editRow.addView(colourSpinner)
        }

        val activeBox = CheckBox(this)
        activeBox.isChecked = isActive == 1
        editRow.addView(activeBox)

        tableLayout.addView(editRow)
        tableRows += editRow
    }

    private class ColourSpinnerAdapter(
            val ctx: Context,
            val colourList: List<Map<String, Any>>) : SimpleAdapter(
            ctx,
            colourList,
            R.layout.id_colour_item,
            arrayOf(ColourTable.ID),
            intArrayOf(R.id.item_id)) {

        private inner class ColourItemHolder(val colourResourceId: TextView,
                                             val colourField: TextView)

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {

            val thisView: View
            val holder: ColourItemHolder

            if (convertView == null) {
                val vi = ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                thisView = vi.inflate(R.layout.id_colour_item, parent, false)

                holder = ColourItemHolder(
                        thisView.findViewById(R.id.item_id) as TextView,
                        thisView.findViewById(R.id.item_colour) as TextView)

                thisView.tag = holder

            } else {
                thisView = convertView
                holder = thisView.tag as ColourItemHolder
            }

            val thisColour = colourList[position]
            val colourResourceId = thisColour[ColourTable.ID] as Int
            holder.colourResourceId.text = colourResourceId.toString()
            holder.colourField.text = colourFieldSpaces
            holder.colourField.background = ColorDrawable(thisColour[ColourTable.VALUE] as Int)

            return thisView
        }

        override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup?): View {

            val thisView: View
            val holder: ColourItemHolder

            if (convertView == null) {
                val vi = ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                thisView = vi.inflate(R.layout.id_colour_item, parent, false)

                holder = ColourItemHolder(
                        thisView.findViewById(R.id.item_id) as TextView,
                        thisView.findViewById(R.id.item_colour) as TextView)

                thisView.tag = holder

            } else {
                thisView = convertView
                holder = thisView.tag as ColourItemHolder
            }

            val thisColour = colourList[position]
            val colourResourceId = thisColour[ColourTable.ID] as Int
            holder.colourResourceId.text = colourResourceId.toString()
            holder.colourField.text = colourFieldSpaces
            holder.colourField.background = ColorDrawable(thisColour[ColourTable.VALUE] as Int)

            return thisView
        }
    }

}

fun stringify(anyList: List<Any?>) = anyList.map { x ->
    when (x) {
        is String -> x
        is Long -> x.toString()
        else -> ""
    }
}