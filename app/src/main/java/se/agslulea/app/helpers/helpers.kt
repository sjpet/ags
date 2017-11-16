package se.agslulea.app.helpers

import android.content.Context
import android.widget.EditText
import android.widget.SimpleAdapter
import android.widget.Spinner
import android.widget.TableRow
import se.agslulea.app.R
import se.agslulea.app.classes.TimetableActivity
import se.agslulea.app.data.db.MemberMetaTable
import se.agslulea.app.data.db.MemberTable
import java.text.SimpleDateFormat
import java.util.*

val whitespaceRegex = "\\s+".toRegex()
val numberRegex = "[0-9]+".toRegex()
val personalIdRegex = "[0-9]{8}-[0-9]{4}".toRegex()
val emailRegex = ("^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@" +
        "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$").toRegex()
val yearFrom = "[0-9]{4}-".toRegex()
val yearTo = "-[0-9]{4}".toRegex()
val yearRange = "[0-9]{4}-[0-9]{4}".toRegex()
val dateRegex = "[0-9]{4}-[0-9]{2}-[0-9]{2}".toRegex()
val timeRegex = "[0-9]{2}:[0-9]{2}".toRegex()

val prepositions = listOf("van", "von", "der", "af", "de", "la", "da")

val daysInMonth = listOf(31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31)

val listOfDays = listOf(Calendar.MONDAY, Calendar.TUESDAY, Calendar.WEDNESDAY, Calendar.THURSDAY,
        Calendar.FRIDAY, Calendar.SATURDAY, Calendar.SUNDAY)

val longDateFormat = SimpleDateFormat("yyyy-MM-dd")
val shortDateFormat = SimpleDateFormat("d/M")
val timeFormat = SimpleDateFormat("HH:mm")

fun capitalizeName(s: String): String {
    return s
            .split(whitespaceRegex)
            .map { x -> if (x in prepositions) { x } else { x.capitalize() } }
            .joinToString(" ")
}

fun isValidEmail(s: String) = s.matches(emailRegex)

fun isValidPersonalId(s: String): Boolean {
    if (!s.matches(personalIdRegex)) {
        return false
    }

    val date = s.substring(0..3) + "-" + s.substring(4..5) + "-" + s.substring(6..7)
    if (!isValidDate(date)) { return false }

    // Validate control digit
    val sReduced = s.substring(2..7) + s.substring(9..11)

    var nextMultiplier = 2
    var checksum = 0
    for (c in sReduced) {
        val a = nextMultiplier*Character.toString(c).toInt()
        checksum += a % 10
        if (a > 9) { checksum += 1 }
        nextMultiplier = if (nextMultiplier == 2) { 1 } else { 2 }
    }

    val expectedControlDigit = (10 - checksum % 10) % 10

    return s[12].toString().toInt() == expectedControlDigit
}

fun formatPersonalId(s: String): String {
    val numCharacters = s.length
    if (numCharacters < 10) { return "19000000-0000" }
    val now = Calendar.getInstance(Locale("sv", "SE"))
    val yearInCentury = now.get(Calendar.YEAR) % 100
    val givenYear = if (s.substring(0..1).matches(numberRegex)) { s.substring(0..1).toInt() } else { 0 }
    val century = if (givenYear < yearInCentury) { "20" } else { "19" }
    return when (numCharacters) {
        12 -> s.substring(0..7) + "-" + s.substring(8..11)
        11 -> century + s
        10 -> century + s.substring(0..5) + "-" + s.substring(6..9)
        13 -> s
        else -> "19000000-0000"
    }
}

fun filterMemberList(members: List<Map<String, Any>>,
                     selectedGroup: Int,
                     searchQuery: String?,
                     omnipresent: Set<Int> = setOf()): List<Map<String, Any>> {
    return members.filter {
        member -> (if (selectedGroup > 0 && searchQuery == null) {
        selectedGroup in member[MemberMetaTable.GROUPS] as List<Int> ||
                member[MemberTable.ID] as Int in omnipresent
    } else {
        true
    } && if (searchQuery != null) {
        when {
            searchQuery.matches(yearFrom) -> {
                val year = searchQuery.substring(0..3).toInt()
                (member[MemberMetaTable.DATE_OF_BIRTH] as String)
                        .substring(0..3).toInt() >= year
            }
            searchQuery.matches(yearTo) -> {
                val year = searchQuery.substring(1..4).toInt()
                (member[MemberMetaTable.DATE_OF_BIRTH] as String)
                        .substring(0..3).toInt() <= year
            }
            searchQuery.matches(yearRange) -> {
                val years = Pair(searchQuery.substring(0..3).toInt(),
                        searchQuery.substring(5..8).toInt())
                val (fromYear, toYear) = if (years.first > years.second) {
                    Pair(years.second, years.first)
                } else {
                    years
                }
                (member[MemberMetaTable.DATE_OF_BIRTH] as String)
                        .substring(0..3).toInt() in (fromYear..toYear)
            }
            else -> searchQuery.toLowerCase() in
                    (member[MemberMetaTable.FULL_NAME] as String).toLowerCase() ||
                    searchQuery in (member[MemberMetaTable.DATE_OF_BIRTH] as String)
        }
    } else {
        true
    })
    }
}

fun layoutWeight(startTime: String, endTime: String): Float {
    return if (startTime.matches(timeRegex) && endTime.matches(timeRegex)) {
        val minutes = 60*(endTime.substring(0..1).toInt() - startTime.substring(0..1).toInt()) +
                (endTime.substring(3..4).toInt() - startTime.substring(3..4).toInt())
        if (minutes > 0) { minutes.toFloat() } else { 0f }
    } else {
        0f
    }
}

fun weeksInYear(year: Int): Int {
    val calendar = Calendar.getInstance(Locale("sv", "SE"))
    calendar.set(Calendar.YEAR, year)
    calendar.set(Calendar.MONTH, Calendar.DECEMBER)
    calendar.set(Calendar.DAY_OF_MONTH, 31)
    val weekday = calendar.get(Calendar.DAY_OF_WEEK)
    return if (weekday == Calendar.THURSDAY || (isLeapYear(year) && weekday == Calendar.FRIDAY)) {
        53
    } else {
        52
    }
}

private fun isLeapYear(year: Int): Boolean =
        ((year % 4) == 0  && (((year % 100) > 0) || (year % 400) == 0))

fun calendarAt(year: Int, week: Int, weekday: Int): Calendar {
    val calendar = Calendar.getInstance(Locale("sv", "SE"))
    calendar.set(Calendar.DAY_OF_WEEK, weekday)
    calendar.set(Calendar.WEEK_OF_YEAR, week)
    calendar.set(Calendar.YEAR, year)
    return calendar
}

fun flatActivityList(timetable: Map<Int, Triple<String, String, List<TimetableActivity>>>):
        List<TimetableActivity> = timetable.map { (_, value) ->
            val (_, _, activityList) = value
            activityList
        }.flatten()

fun timeRange(activityList: List<TimetableActivity>): Pair<String, String> {
    var earliestStart: String? = null
    var latestEnd: String? = null
    for (activity in activityList) {
        if (earliestStart == null || earliestStart > activity.startTime) {
            earliestStart = activity.startTime
        }
        if (latestEnd == null || latestEnd < activity.endTime) {
            latestEnd = activity.endTime
        }
    }

    return if (earliestStart != null && latestEnd != null) {
        Pair(earliestStart, latestEnd)
    } else {
        Pair("00:00", "00:00")
    }
}

private fun splitTime(time: String) =
        Pair(time.substring(0..1).toInt(), time.substring(3..4).toInt())

fun addTime(time: String, addedTime: String) =
    if (addedTime.matches(timeRegex) && addedTime.matches(timeRegex)) {
        val (hours, minutes) = splitTime(time)
        val (addedHours, addedMinutes) = splitTime(addedTime)
        "%02d:%02d".format(
                hours + addedHours + if (minutes + addedMinutes > 59) { 1 } else { 0 } % 24,
                (minutes + addedMinutes) % 60)
    } else {
        time
    }

fun getNearestQuarter(): String {
    val now = Calendar.getInstance(Locale("sv", "SE"))
    val (hours, minutes) = splitTime(timeFormat.format(now.time))
    return "%02d:%02d".format((hours + if (minutes > 51) { 1 } else { 0 }) % 24,
            (((minutes + 8) / 15) % 4) * 15)
}

fun addEditText(ctx: Context,
                row: TableRow,
                inputType: Int,
                ems: Int,
                default: String,
                singleLine: Boolean = true): EditText {
    val editText = EditText(ctx)
    editText.setEms(ems)
    editText.setText(default)
    editText.inputType = inputType
    editText.setSingleLine(singleLine)
    row.addView(editText)
    return editText
}

fun addSpinner(ctx: Context,
               row: TableRow,
               items: List<Map<String, Any?>>,
               keys: Array<String>,
               selection: Int = 1): Spinner {
    val spinner = Spinner(ctx)
    setSpinnerAdapter(ctx, spinner, items, keys, selection)
    row.addView(spinner)
    return spinner
}

fun setSpinnerAdapter(ctx: Context,
                      spinner: Spinner,
                      items: List<Map<String, Any?>>,
                      keys: Array<String>,
                      selection: Int = 1) {
    spinner.adapter = SimpleAdapter(
            ctx, items, R.layout.id_string_item, keys, intArrayOf(R.id.item_id, R.id.item_name))
    spinner.setSelection(items.map { x -> x[keys[0]] as Int }.indexOf(selection))
}

fun isValidDate(date: String): Boolean = if (date.matches(dateRegex)) {
    val year = date.substring(0..3).toInt()
    val month = date.substring(5..6).toInt()
    val day = date.substring(8..9).toInt()

    var isValid = true
    if (month !in 1..12) {
        isValid = false
    }

    val daysThisMonth =
            daysInMonth[month - 1] + if (isLeapYear(year) && month == 2) { 1 } else { 0 }
    if (day !in 1..daysThisMonth) {
        isValid = false
    }

    isValid

} else {
    false
}

fun isValidTime(time: String): Boolean = if (time.matches(timeRegex)) {
    val (hours, minutes) = splitTime(time)
    hours in 0..23 && minutes in 0..59
} else {
    false
}