package se.agslulea.app

import java.util.*

val whitespaceRegex = "\\s+".toRegex()
val numberRegex = "[0-9]+".toRegex()
val personalIdRegex = "[0-9]{8}-[0-9]{4}".toRegex()
val emailRegex = ("^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@" +
        "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$").toRegex()

val prepositions = listOf("van", "von", "der", "af", "de", "la", "da")

val daysInMonth = listOf(31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31)

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

    // Validate date
    val year = s.substring(0..3).toInt()
    val month = s.substring(4..5).toInt()
    val day = s.substring(6..7).toInt()

    if (month !in 1..12) {
        return false
    }

    val isLeapYear = ((year % 4) == 0  && (((year % 100) > 0) || (year % 400) == 0))
    val daysThisMonth = daysInMonth[month - 1] + if (isLeapYear && month == 2) { 1 } else { 0 }
    if (day !in 1..daysThisMonth) {
        return false
    }

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
    val now = Calendar.getInstance()
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