package se.agslulea.app.ui.activities

import android.graphics.Typeface
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.widget.*

import se.agslulea.app.R
import se.agslulea.app.classes.ScheduledActivity
import se.agslulea.app.data.db.*
import se.agslulea.app.helpers.*

class EditTimetableActivity : AppCompatActivity() {

    private var tableLayout: TableLayout? = null
    val db = AppDb()

    private val inserts: MutableList<ScheduledActivity> = mutableListOf()
    private val removals: MutableList<Int> = mutableListOf()
    private val updates: MutableMap<Int, ScheduledActivity> = mutableMapOf()

    private var activityList: List<ScheduledActivity> = listOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_timetable)

        val year = intent.getIntExtra("year", -1)
        val week = intent.getIntExtra("week", -1)
        val weekday = intent.getIntExtra("weekday", -1)

        if (year < 0 || week < 0 || weekday < 0) {
            finish()
        }

        tableLayout = findViewById(R.id.timetable_table) as TableLayout
        val saveButton = findViewById(R.id.timetable_save_button) as Button
        val fromDateEditText = findViewById(R.id.from_date_edit_text) as EditText
        val toDateEditText = findViewById(R.id.to_date_edit_text) as EditText

        val timetableEntry = db.getTimetableEntry(year, week, weekday)

        // Add header row
        val headerRow = layoutInflater.inflate(R.layout.table_row_template, tableLayout,
                false) as TableRow

        for (column in listOf(getString(R.string.timetable_activity_type),
                getString(R.string.timetable_sport),
                getString(R.string.timetable_group),
                getString(R.string.timetable_start),
                getString(R.string.timetable_end))) {
            val textView = layoutInflater.inflate(R.layout.table_text_template,
                    headerRow, false) as TextView
            textView.text = column
            textView.typeface = Typeface.DEFAULT_BOLD
            headerRow.addView(textView)
        }

        tableLayout?.addView(headerRow)

        // Add additional rows and set dates
        if (timetableEntry != null) {
            activityList = db.getActivityList(timetableEntry[TimetableTable.ID] as Int)
            for (activity in activityList) {
                addRow(activity)
            }
            fromDateEditText.setText(timetableEntry[TimetableTable.FROM_DATE] as String)
            toDateEditText.setText(timetableEntry[TimetableTable.LAST_DATE] as String)
            val (_, latestTime) = timeRange(activityList)
            addLastRow(latestTime, addTime(latestTime, "01:00"))
        } else {
            val calendar = calendarAt(year, week, weekday)
            val today = longDateFormat.format(calendar.time)
            fromDateEditText.setText(today)
            toDateEditText.setText(today)
            addLastRow()
        }

        saveButton.setOnClickListener {
            val fromDate = fromDateEditText.text.toString()
            val toDate = toDateEditText.text.toString()

            saveButton.setFocusable(true)
            saveButton.setFocusableInTouchMode(true)
            saveButton.requestFocus()

            if (timetableEntry == null ||
                    timetableEntry[TimetableTable.FROM_DATE] != fromDate ||
                    timetableEntry[TimetableTable.LAST_DATE] != toDate) {
                val timetableId = db.newTimetable(weekday, fromDate, toDate)
                activityList.map { activity ->
                    val classId = db.newClass(activity)
                    db.addClassToTimetable(timetableId, classId)
                }
                inserts.map { activity ->
                    val classId = db.newClass(activity)
                    db.addClassToTimetable(timetableId, classId)
                }
                updates.map { (_, activity) -> db.updateClass(activity) }
                removals.map { classId -> db.removeClassFromTimetable(timetableId, classId) }
            } else {
                val timetableId = timetableEntry[TimetableTable.ID] as Int
                inserts.map { activity ->
                    val classId = db.newClass(activity)
                    db.addClassToTimetable(timetableId, classId)
                }
                updates.map { (_, activity) ->
                    db.updateClass(activity) }
                removals.map { classId -> db.removeClassFromTimetable(timetableId, classId) }
            }

            finish()
        }

    }

    private fun addRow(activity: ScheduledActivity) {

        val row = layoutInflater.inflate(R.layout.table_row_template, tableLayout,
                false) as TableRow

        val typeSpinner = addSpinner(this, row, db.getActivityTypeNames(),
                arrayOf(ActivityTypeTable.ID, ActivityTypeTable.TYPE), activity.type)
        val sportSpinner = addSpinner(this, row, db.getSportNames(),
                arrayOf(SportTable.ID, SportTable.SPORT), activity.sport)
        val groupSpinner = addSpinner(this, row, db.getGroupNames(),
                arrayOf(GroupTable.ID, GroupTable.GROUP), activity.group)
        val startTimeText = addEditText(
                this, row, InputType.TYPE_DATETIME_VARIATION_TIME, 5, activity.startTime)
        val endTimeText = addEditText(
                this, row, InputType.TYPE_DATETIME_VARIATION_TIME, 5, activity.endTime)

        setCustomListener(typeSpinner, activity,
                { (typeSpinner.selectedItem as Map<*, *>)[ActivityTypeTable.ID] as Int },
                { a: ScheduledActivity -> a.type },
                { a: ScheduledActivity?, b: Any -> a?.type = b as Int})

        setCustomListener(sportSpinner, activity,
                { (sportSpinner.selectedItem as Map<*, *>)[SportTable.ID] as Int },
                { a: ScheduledActivity -> a.sport },
                { a: ScheduledActivity?, b: Any -> a?.sport = b as Int})

        setCustomListener(groupSpinner, activity,
                { (groupSpinner.selectedItem as Map<*, *>)[GroupTable.ID] as Int },
                { a: ScheduledActivity -> a.group },
                { a: ScheduledActivity?, b: Any -> a?.group = b as Int})

        setCustomListener(startTimeText, activity,
                { startTimeText.text.toString() },
                { a: ScheduledActivity -> a.startTime},
                { a: ScheduledActivity?, b: Any -> a?.startTime = b as String})

        setCustomListener(endTimeText, activity,
                { endTimeText.text.toString() },
                { a: ScheduledActivity -> a.endTime},
                { a: ScheduledActivity?, b: Any -> a?.endTime = b as String})

        val removeButton = Button(this)
        removeButton.text = getString(R.string.remove)
        removeButton.setOnClickListener {
            if (activity.id == -1) {
                inserts.remove(activity)
            } else {
                removals.add(activity.id)
            }
            tableLayout?.removeView(row)
        }
        row.addView(removeButton)

        tableLayout?.addView(row)
    }

    private fun addLastRow(startTime: String = "18:00", endTime: String = "19:00") {
        val lastRow = layoutInflater.inflate(R.layout.table_row_template, tableLayout,
                false) as TableRow

        val typeSpinner = addSpinner(this, lastRow, db.getActivityTypeNames(),
                arrayOf(ActivityTypeTable.ID, ActivityTypeTable.TYPE), 1)
        val sportSpinner = addSpinner(this, lastRow, db.getSportNames(),
                arrayOf(SportTable.ID, SportTable.SPORT), 1)
        val groupSpinner = addSpinner(this, lastRow, db.getGroupNames(),
                arrayOf(GroupTable.ID, GroupTable.GROUP), 1)
        val startTimeText = addEditText(
                this, lastRow, InputType.TYPE_DATETIME_VARIATION_TIME, 5, startTime)
        val endTimeText = addEditText(
                this, lastRow, InputType.TYPE_DATETIME_VARIATION_TIME, 5, endTime)


        val addButton = Button(this)
        addButton.text = getString(R.string.row_add)
        lastRow.addView(addButton)

        addButton.setOnClickListener {
            typeSpinner.selectedItem
            val typeId = (typeSpinner.selectedItem as Map<*, *>)[ActivityTypeTable.ID] as Int
            val sportId = (sportSpinner.selectedItem as Map<*, *>)[SportTable.ID] as Int
            val groupId = (groupSpinner.selectedItem as Map<*, *>)[GroupTable.ID] as Int
            val startTime = startTimeText.text.toString()
            val endTime = endTimeText.text.toString()

            val activity = ScheduledActivity(-1, typeId, sportId, groupId, startTime, endTime)

            inserts.add(activity)

            // convert to an editable row and create a new last row
            tableLayout?.removeView(lastRow)
            addRow(activity)
            addLastRow(endTime, addTime(endTime, "01:00"))
        }

        tableLayout?.addView(lastRow)
    }

    private fun setCustomListener(view: View,
                                  activity: ScheduledActivity,
                                  getter: () -> Any,
                                  originalGetter: (a: ScheduledActivity) -> Any,
                                  setter: (a: ScheduledActivity?, v: Any) -> Unit) {
        if (view is EditText) {
            view.setOnFocusChangeListener { _, hasFocus ->
                if (!hasFocus) {
                    val newValue = getter()
                    if (newValue != originalGetter(activity) || activity.id in updates) {
                        if (activity.id == -1) {
                            setter(activity, newValue)
                        } else {
                            if (activity.id !in updates) {
                                updates[activity.id] = ScheduledActivity(activity.id, activity.type,
                                        activity.sport, activity.group, activity.startTime,
                                        activity.endTime)
                            }
                            setter(updates[activity.id], newValue)
                        }
                    }
                }
            }
        } else if (view is Spinner) {
            view.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>, view: View, pos: Int,
                                            id: Long) {
                    val newValue = getter()
                    if (newValue != originalGetter(activity) || activity.id in updates) {
                        if (activity.id == -1) {
                            setter(activity, newValue)
                        } else {
                            if (activity.id !in updates) {
                                updates[activity.id] = ScheduledActivity(activity.id, activity.type,
                                        activity.sport, activity.group, activity.startTime,
                                        activity.endTime)
                            }
                            setter(updates[activity.id], newValue)
                        }
                    }
                }
                override fun onNothingSelected(parent: AdapterView<out Adapter>?) {}
            }
        }
    }

}