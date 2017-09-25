package se.agslulea.app.ui.activities

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

import java.util.Calendar

import se.agslulea.app.R
import se.agslulea.app.classes.Activity
import se.agslulea.app.helpers.layoutWeight

class TimetableActivity : AppCompatActivity() {

    private val listOfDays = listOf(Calendar.MONDAY, Calendar.TUESDAY, Calendar.WEDNESDAY,
            Calendar.THURSDAY, Calendar.FRIDAY, Calendar.SATURDAY, Calendar.SUNDAY)
    private val strips = mutableMapOf<Int, LinearLayout>()
    private val dateLabels = mutableMapOf<Int, TextView>()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_timetable)
        
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

        val newButton = findViewById(R.id.new_activity_button) as Button

        val testTimetable: Map<Int, Pair<String, List<Activity>>> = mapOf(
                Calendar.MONDAY to Pair("1/1", listOf()),
                Calendar.TUESDAY to Pair("2/1", listOf(
                        Activity(1, 3, 1, "MMA", "2017-01-02", "18:00", "19:30"),
                        Activity(1, 2, 1, "BJJ", "2017-01-02", "19:30", "20:30"),
                        Activity(1, 0, 1, "Öppen Matta", "2017-01-02", "20:30", "21:30"))),
                Calendar.WEDNESDAY to Pair("3/1", listOf()),
                Calendar.THURSDAY to Pair("4/1", listOf(
                        Activity(1, 1, 2, "SW\nTjej", "2017-01-04", "18:00", "19:00"),
                        Activity(1, 1, 1, "SW", "2017-01-04", "19:15", "20:15"),
                        Activity(1, 0, 1, "Öppen Matta", "2017-01-04", "20:15", "21:15"))),
                Calendar.FRIDAY to Pair("5/1", listOf()),
                Calendar.SATURDAY to Pair("6/1", listOf(
                        Activity(1, 1, 1, "SW", "2017-01-06", "10:30", "11:30"),
                        Activity(1, 0, 1, "Öppen Matta", "2017-01-06", "11:30", "12:30"))),
                Calendar.SUNDAY to Pair("7/1", listOf(
                        Activity(1, 2, 1, "BJJ", "2017-01-07", "11:00", "12:00"),
                        Activity(1, 0, 1, "Öppen Matta", "2017-01-07", "12:00",
                                "13:00"),
                        Activity(1, 2, 3, "BJJ\nStor & Liten", "2017-01-07", "16:45", "17:45"),
                        Activity(1, 2, 4, "BJJ\nBarn", "2017-01-07", "18:00", "19:00"))))
        
        populateTimetable(testTimetable)

    }
    
    private fun populateTimetable(timetable: Map<Int, Pair<String, List<Activity>>>) {
        val (earliestStart, latestFinish) = timeRange(flatActivityList(timetable))

        listOfDays.map { weekday ->
            val (dateString, activityList) = timetable[weekday]!!

            dateLabels[weekday]?.text = dateString
            
            var currentTime = earliestStart
            activityList.map { activity: Activity -> 
                if (activity.startTime > earliestStart) {
                    addBlank(weekday, layoutWeight(currentTime, activity.startTime))
                }
                addActivity(weekday,
                        activity.label,
                        activity.startTime,
                        activity.endTime)
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
            label: String,
            startTime: String,
            endTime: String) {

        val activity = layoutInflater.inflate(R.layout.timetable_block_template, strips[weekday],
            false) as LinearLayout

        val params = activity.layoutParams as LinearLayout.LayoutParams
        params.weight = layoutWeight(startTime, endTime)
        activity.layoutParams = params

        val startTimeText = activity.findViewById(R.id.block_start_time) as TextView
        val endTimeText = activity.findViewById(R.id.block_end_time) as TextView
        val sportText = activity.findViewById(R.id.block_label) as TextView

        startTimeText.text = startTime
        endTimeText.text = endTime
        sportText.text = label

        strips[weekday]?.addView(activity)

        strips[weekday]?.invalidate()

    }
}

