package se.agslulea.app.ui.activities

import android.graphics.drawable.ColorDrawable
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import org.jetbrains.anko.startActivity

import se.agslulea.app.R
import se.agslulea.app.classes.TimetableActivity
import se.agslulea.app.data.db.AppDb
import se.agslulea.app.helpers.flatActivityList
import se.agslulea.app.helpers.layoutWeight
import se.agslulea.app.helpers.timeRange
import se.agslulea.app.helpers.weeksInYear
import java.util.*

class TimetableActivity : AppCompatActivity() {

    private var timetableWeekText: TextView? = null

    private val listOfDays = listOf(Calendar.MONDAY, Calendar.TUESDAY, Calendar.WEDNESDAY,
            Calendar.THURSDAY, Calendar.FRIDAY, Calendar.SATURDAY, Calendar.SUNDAY)
    private val strips = mutableMapOf<Int, LinearLayout>()
    private val dateLabels = mutableMapOf<Int, TextView>()

    private val db = AppDb()

    private var year: Int = 2000
    private var week: Int = 1
    private var adminLevel: Int = 0
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_timetable)

        adminLevel = intent.getIntExtra("adminLevel", 0)

        val now = Calendar.getInstance(Locale("sv", "SE"))
        year = now.get(Calendar.YEAR)
        week = now.get(Calendar.WEEK_OF_YEAR)

        timetableWeekText = findViewById(R.id.timetable_week_text) as TextView

        strips[Calendar.MONDAY] = findViewById(R.id.monday_strip) as LinearLayout
        strips[Calendar.TUESDAY] = findViewById(R.id.tuesday_strip) as LinearLayout
        strips[Calendar.WEDNESDAY] = findViewById(R.id.wednesday_strip) as LinearLayout
        strips[Calendar.THURSDAY] = findViewById(R.id.thursday_strip) as LinearLayout
        strips[Calendar.FRIDAY] = findViewById(R.id.friday_strip) as LinearLayout
        strips[Calendar.SATURDAY] = findViewById(R.id.saturday_strip) as LinearLayout
        strips[Calendar.SUNDAY] = findViewById(R.id.sunday_strip) as LinearLayout

        dateLabels[Calendar.MONDAY] = findViewById(R.id.monday_date) as TextView
        dateLabels[Calendar.TUESDAY] = findViewById(R.id.tuesday_date) as TextView
        dateLabels[Calendar.WEDNESDAY] = findViewById(R.id.wednesday_date) as TextView
        dateLabels[Calendar.THURSDAY] = findViewById(R.id.thursday_date) as TextView
        dateLabels[Calendar.FRIDAY] = findViewById(R.id.friday_date) as TextView
        dateLabels[Calendar.SATURDAY] = findViewById(R.id.saturday_date) as TextView
        dateLabels[Calendar.SUNDAY] = findViewById(R.id.sunday_date) as TextView

        val previousButton = findViewById(R.id.previous_week_button) as ImageButton
        val nextButton = findViewById(R.id.next_week_button) as ImageButton
        val newButton = findViewById(R.id.new_activity_button) as Button

        if (adminLevel > 0) {
            val editTimetableBar = findViewById(R.id.edit_timetable_button_bar) as LinearLayout
            val editTimetableButtons = mapOf(
                    Calendar.MONDAY to findViewById(R.id.monday_edit_button) as Button,
                    Calendar.TUESDAY to findViewById(R.id.tuesday_edit_button) as Button,
                    Calendar.WEDNESDAY to findViewById(R.id.wednesday_edit_button) as Button,
                    Calendar.THURSDAY to findViewById(R.id.thursday_edit_button) as Button,
                    Calendar.FRIDAY to findViewById(R.id.friday_edit_button) as Button,
                    Calendar.SATURDAY to findViewById(R.id.saturday_edit_button) as Button,
                    Calendar.SUNDAY to findViewById(R.id.sunday_edit_button) as Button)

            newButton.visibility = View.GONE
            editTimetableBar.visibility = View.VISIBLE

            listOfDays.map { weekday ->
                editTimetableButtons[weekday]?.setOnClickListener {
                    startActivity<EditTimetableActivity>(
                            "year" to year, "week" to week, "weekday" to weekday)
                }
            }
        }

        previousButton.setOnClickListener {
            week -= 1
            if (week < 1) {
                year -= 1
                week = 52
            }
            updateTitle()
            drawTimetable()
        }

        nextButton.setOnClickListener {
            week += 1
            if (week > weeksInYear(year)) {
                year += 1
                week = 1
            }
            updateTitle()
            drawTimetable()
        }

        newButton.setOnClickListener {
            startActivity<NewActivityActivity>()
        }

        updateTitle()
        drawTimetable()

    }

    override fun onResume() {
        super.onResume()
        updateTitle()
        drawTimetable()
    }

    private fun updateTitle() {
        timetableWeekText?.text = getString(R.string.timetable_title, week, year)
    }

    private fun drawTimetable() {
        clearTimetable()
        populateTimetable(db.getTimetable(year, week))
    }

    private fun clearTimetable() = strips.map { (_, strip) -> strip.removeAllViews() }

    private fun populateTimetable(timetable: Map<Int, Triple<String, String, List<TimetableActivity>>>) {
        val (earliestStart, latestFinish) = timeRange(flatActivityList(timetable))

        listOfDays.map { weekday ->
            val (fullDate, shortDate, activityList) = timetable[weekday]!!

            dateLabels[weekday]?.text = shortDate
            
            var currentTime = earliestStart
            activityList.map { activity: TimetableActivity ->
                if (activity.startTime > earliestStart) {
                    addBlank(weekday, layoutWeight(currentTime, activity.startTime))
                }
                addActivity(weekday, activity, fullDate)
                currentTime = activity.endTime
            }
            if (currentTime < latestFinish) {
                addBlank(weekday, layoutWeight(currentTime, latestFinish))
            }
        }
    }

    private fun addBlank(weekday: Int, weight: Float) {
        val blank = LinearLayout(this)
        blank.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                0, weight)
        strips[weekday]?.addView(blank)
    }

    private fun addActivity(
            weekday: Int,
            activity: TimetableActivity,
            date: String) {

        val activitySlot = layoutInflater.inflate(R.layout.timetable_block_template, strips[weekday],
            false) as LinearLayout

        activitySlot.background = ColorDrawable(db.getActivityTypeColour(activity.type))

        val params = activitySlot.layoutParams as LinearLayout.LayoutParams
        params.weight = layoutWeight(activity.startTime, activity.endTime)
        activitySlot.layoutParams = params

        val startTimeText = activitySlot.findViewById(R.id.block_start_time) as TextView
        val endTimeText = activitySlot.findViewById(R.id.block_end_time) as TextView
        val sportText = activitySlot.findViewById(R.id.block_label) as TextView

        startTimeText.text = activity.startTime
        endTimeText.text = activity.endTime
        sportText.text = db.activityLabel(activity.type, activity.sport, activity.group)

        activitySlot.setOnClickListener{
            if (activity.isReported) {
                startActivity<RollCallActivity>(
                        "type" to activity.type,
                        "sport" to activity.sport,
                        "group" to activity.group,
                        "date" to date,
                        "startTime" to activity.startTime,
                        "endTime" to activity.endTime,
                        "replacesScheduled" to activity.replacesScheduled,
                        "activityId" to activity.id)
            } else {
                startActivity<RollCallActivity>(
                        "type" to activity.type,
                        "sport" to activity.sport,
                        "group" to activity.group,
                        "date" to date,
                        "startTime" to activity.startTime,
                        "endTime" to activity.endTime,
                        "replacesScheduled" to true)
            }
        }

        strips[weekday]?.addView(activitySlot)
        strips[weekday]?.invalidate()
    }
}

