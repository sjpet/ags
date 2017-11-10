package se.agslulea.app.ui.activities

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.CheckBox
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.toast

import se.agslulea.app.R
import se.agslulea.app.classes.TimetableActivity
import se.agslulea.app.data.db.ActivityTypeTable
import se.agslulea.app.data.db.AppDb
import se.agslulea.app.data.db.GroupTable
import se.agslulea.app.data.db.SportTable
import se.agslulea.app.helpers.*
import java.util.*

class NewActivityActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_activity)

        val db = AppDb()
        val now = Calendar.getInstance()
        val today = longDateFormat.format(now.time)

        val typeSpinner = findViewById(R.id.new_activity_type_spinner) as Spinner
        val sportSpinner = findViewById(R.id.new_activity_sport_spinner) as Spinner
        val groupSpinner = findViewById(R.id.new_activity_group_spinner) as Spinner
        val dateEditText = findViewById(R.id.new_activity_date_edit_text) as EditText
        val startTimeEditText = findViewById(R.id.new_activity_start_time_edit_text) as EditText
        val endTimeEditText = findViewById(R.id.new_activity_end_time_edit_text) as EditText
        val proceedButton = findViewById(R.id.proceed_button) as Button
        val replacesCheckBox = findViewById(R.id.replaces_check_box) as CheckBox

        dateEditText.setText(today)

        val ongoingActivity: TimetableActivity? = db.ongoingActivity()
        val preselectedType: Int
        val preselectedSport: Int
        val preselectedGroup: Int
        if (ongoingActivity == null) {
            val nearestQuarter = getNearestQuarter()
            preselectedType = 1
            preselectedSport = 1
            preselectedGroup = 1
            startTimeEditText.setText(nearestQuarter)
            endTimeEditText.setText(addTime(nearestQuarter, "01:00"))
        } else {
            preselectedType = ongoingActivity.type
            preselectedSport = ongoingActivity.sport
            preselectedGroup = ongoingActivity.group
            startTimeEditText.setText(ongoingActivity.startTime)
            endTimeEditText.setText(ongoingActivity.endTime)
        }

        setSpinnerAdapter(this, typeSpinner, db.getActivityTypeNames(),
                arrayOf(ActivityTypeTable.ID, ActivityTypeTable.TYPE), preselectedType)
        setSpinnerAdapter(this, sportSpinner, db.getSportNames(),
                arrayOf(SportTable.ID, SportTable.SPORT), preselectedSport)
        setSpinnerAdapter(this, groupSpinner, db.getGroupNames(),
                arrayOf(GroupTable.ID, GroupTable.GROUP), preselectedGroup)



        proceedButton.setOnClickListener {
            val type = (typeSpinner.selectedItem as Map<*, *>)[ActivityTypeTable.ID] as Int
            val sport = (sportSpinner.selectedItem as Map<*, *>)[SportTable.ID] as Int
            val group = (groupSpinner.selectedItem as Map<*, *>)[GroupTable.ID] as Int
            val date = dateEditText.text.toString()
            val startTime = startTimeEditText.text.toString()
            val endTime = endTimeEditText.text.toString()
            val replacesScheduled = replacesCheckBox.isChecked

            when {
                !isValidDate(date) -> toast(R.string.invalid_date)
                !isValidTime(startTime) -> toast(R.string.invalid_start_time)
                !isValidTime(endTime) || endTime <= startTime -> toast(R.string.invalid_end_time)
                else -> {
                    startActivity<RollCallActivity>(
                            "type" to type,
                            "sport" to sport,
                            "group" to group,
                            "date" to date,
                            "startTime" to startTime,
                            "endTime" to endTime,
                            "replacesScheduled" to replacesScheduled)
                    finish()
                }
            }
        }
    }
}