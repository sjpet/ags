package se.agslulea.app.data.db

import android.content.Context
import android.os.Environment
import org.jetbrains.anko.db.*
import se.agslulea.app.R
import se.agslulea.app.classes.TimetableActivity
import se.agslulea.app.helpers.*
import se.agslulea.app.ui.App
import java.util.*
import java.io.File



class AppDb(ctx: Context = App.instance,
            private val dbHelper: DbHelper = DbHelper.instance) {

    private val exportName = "ags_export.db"
    private val importName = "ags_import.db"
    private val backupNameFormat = "ags_backup_%d.db"
    private val noAdaString = ctx.applicationContext.getString(R.string.no_ada)

    fun getPassword(level: Int) = dbHelper.use {
        select(PasswordsTable.NAME, PasswordsTable.KEY).whereArgs(
                "${PasswordsTable.LEVEL} = {level}",
                "level" to level).parseOpt(
                StringParser)
    }

    fun setPassword(level: Int, key: String) = dbHelper.use {
        insert(PasswordsTable.NAME, PasswordsTable.LEVEL to level, PasswordsTable.KEY to key)
    }

    fun updatePassword(level: Int, key: String) = dbHelper.use {
        update(PasswordsTable.NAME, PasswordsTable.KEY to key).whereArgs(
                "${PasswordsTable.LEVEL} = {level}", "level" to level).exec()
    }

    fun getColours() = dbHelper.use {
        select(ColourTable.NAME, ColourTable.ID, ColourTable.COLOUR, ColourTable.VALUE)
                .parseList(rowParser { id: Int, colourName: String, colourValue: Int ->
                    mapOf(ColourTable.ID to id, ColourTable.COLOUR to colourName,
                            ColourTable.VALUE to colourValue)
                })
    }

    fun getColourValue(colourId: Int): Int = dbHelper.use {
        select(ColourTable.NAME, ColourTable.VALUE)
                .whereArgs("${ColourTable.ID} = {colourId}", "colourId" to colourId)
                .parseSingle(IntParser)
    }

    fun getGroups() = dbHelper.use {
        select(GroupTable.NAME, GroupTable.GROUP, GroupTable.SHORTHAND, GroupTable.IS_ACTIVE)
                .parseList(rowParser { a: String, b: String, c: Int -> listOf(a, b, c) })
    }

    fun getGroupNames() = dbHelper.use {
        select(GroupTable.NAME, GroupTable.ID, GroupTable.GROUP)
                .whereArgs("${GroupTable.IS_ACTIVE} = 1")
                .parseList(rowParser { id: Int, group: String ->
                    mapOf(GroupTable.ID to id, GroupTable.GROUP to group)
                }).sortedBy { x -> x[GroupTable.ID] as Int }
    }

    fun getGroupName(groupId: Int) = dbHelper.use {
        select(GroupTable.NAME, GroupTable.GROUP).whereArgs("${GroupTable.ID} = {groupId}",
                "groupId" to groupId).parseSingle(StringParser)
    }

    fun getSports() = dbHelper.use {
        select(SportTable.NAME, SportTable.SPORT, SportTable.SHORTHAND,
                SportTable.IS_ACTIVE).parseList(rowParser { a: String, b: String, c: Int ->
            listOf(a, b, c)
        })
    }

    fun getSportNames() = dbHelper.use {
        select(SportTable.NAME, SportTable.ID, SportTable.SPORT)
                .whereArgs("${SportTable.IS_ACTIVE} = 1")
                .parseList(rowParser { id: Int, sport: String ->
                    mapOf(SportTable.ID to id, SportTable.SPORT to sport)
                }).sortedBy { x -> x[SportTable.ID] as Int }
    }

    fun getSportShorthand(sportId: Int) = dbHelper.use {
        select(SportTable.NAME, SportTable.SHORTHAND).whereArgs("${SportTable.ID} = {sportId}",
                "sportId" to sportId).parseSingle(StringParser)
    }

    fun getFees() = dbHelper.use {
        select(FeeTable.NAME, FeeTable.FEE, FeeTable.KEY, FeeTable.PERIOD,
                FeeTable.IS_ACTIVE).parseList(rowParser { a: String, b: String, c: String, d: Int ->
            listOf(a, b, c, d)
        })
    }

    fun getFeeNamesWithTime() = dbHelper.use {
        val now = Calendar.getInstance(Locale("sv", "SE"))
        val year = now.get(Calendar.YEAR)
        val shortYear = year.toString().substring(2..3)
        val semester = if (now.get(Calendar.MONTH) > 6) {
            "HT" + shortYear
        } else {
            "VT" + shortYear
        }
        select(FeeTable.NAME, FeeTable.ID, FeeTable.FEE, FeeTable.PERIOD)
                .whereArgs("${FeeTable.IS_ACTIVE} = 1")
                .parseList(rowParser { id: Int, fee: String, period: String ->
                    mapOf(FeeTable.ID to id, FeeTable.FEE to fee + when (period) {
                        "1" -> " " + year.toString()
                        "2" -> " " + semester
                        else -> ""
                    })
                }).sortedBy { x -> x[FeeTable.ID] as Int }
    }

    fun memberHasPaidFee(memberId: Int, feeId: Int) = (dbHelper.use {
        val now = Calendar.getInstance(Locale("sv", "SE"))
        select(PaidFeesTable.NAME, PaidFeesTable.MEMBER).whereArgs(
                "${PaidFeesTable.VALID_UNTIL} >= {today} AND " +
                        "${PaidFeesTable.MEMBER} = {memberId} AND " +
                        "${PaidFeesTable.FEE} = {feeId}",
                "today" to longDateFormat.format(now.time),
                "memberId" to memberId,
                "feeId" to feeId).parseOpt(IntParser)
    } != null)

    fun getActivityTypes() = dbHelper.use {
        select(ActivityTypeTable.NAME, ActivityTypeTable.TYPE, ActivityTypeTable.SHORTHAND,
                ActivityTypeTable.COLOUR, ActivityTypeTable.IS_ACTIVE).parseList(rowParser { a: String, b: String, c: Int, d: Int ->
            listOf(a, b, c, d)
        })
    }

    fun getActivityTypeNames() = dbHelper.use {
        select(ActivityTypeTable.NAME, ActivityTypeTable.ID, ActivityTypeTable.TYPE)
                .whereArgs("${ActivityTypeTable.IS_ACTIVE} = 1")
                .parseList(rowParser { id: Int, activityType: String ->
                    mapOf(ActivityTypeTable.ID to id, ActivityTypeTable.TYPE to activityType)
                }).sortedBy { x -> x[ActivityTypeTable.ID] as Int }
    }

    fun getActivityTypeName(typeId: Int) = dbHelper.use {
        select(ActivityTypeTable.NAME, ActivityTypeTable.TYPE)
                .whereArgs("${ActivityTypeTable.ID} = {typeId}", "typeId" to typeId)
                .parseSingle(StringParser)
    }

    fun getActivityTypeColour(typeId: Int) = dbHelper.use {
        select(ColourTable.NAME, ColourTable.VALUE)
                .whereArgs("${ColourTable.ID} = (SELECT ${ActivityTypeTable.COLOUR} " +
                        "FROM ${ActivityTypeTable.NAME} WHERE ${ActivityTypeTable.ID} = " +
                        "{typeId})", "typeId" to typeId)
                .parseSingle(IntParser)
    }

    fun getActivityDetails(activityId: Int) = dbHelper.use {
        select(ActivityTable.NAME,
                ActivityTable.TYPE,
                ActivityTable.SPORT,
                ActivityTable.GROUP,
                ActivityTable.DATE,
                ActivityTable.START,
                ActivityTable.END,
                ActivityTable.REPLACES).whereArgs("${ActivityTable.ID} = {activityId}",
                "activityId" to activityId).parseSingle(rowParser { a: Int, b: Int, c: Int, d: String, e: String, f: String, g: Int ->
            mapOf(ActivityTable.TYPE to a,
                    ActivityTable.SPORT to b,
                    ActivityTable.GROUP to c,
                    ActivityTable.DATE to d,
                    ActivityTable.START to e,
                    ActivityTable.END to f,
                    ActivityTable.REPLACES to (g == 1))
        })
    }

    private fun nextFreeId(tableName: String, idColumn: String): Int {
        val maxId = dbHelper.use {
            select(tableName, idColumn)
                    .orderBy(idColumn, SqlOrderDirection.DESC)
                    .limit(1)
                    .parseOpt(IntParser)
        }
        return if (maxId == null) {
            0
        } else {
            maxId + 1
        }
    }

    fun memberInGroup(memberId: Int, groupId: Int) = (dbHelper.use {
        select(GroupMemberTable.NAME, GroupMemberTable.ID).whereArgs(
                "${GroupMemberTable.GROUP}={groupId} AND ${GroupMemberTable.MEMBER}={memberId}",
                "groupId" to groupId, "memberId" to memberId).parseOpt(IntParser)
    } != null)

    private fun feesPaid(memberId: Int): String {
        val fees = dbHelper.use {
            val feeNames = select(FeeTable.NAME, FeeTable.ID, FeeTable.KEY).parseList(rowParser { a: Int, b: String ->
                Pair(a, b)
            }).associateBy({ it.first }, { it.second })
            val now = Calendar.getInstance(Locale("sv", "SE"))
            select(PaidFeesTable.NAME, PaidFeesTable.FEE)
                    .whereArgs("${PaidFeesTable.MEMBER} = {memberId} AND " +
                            "${PaidFeesTable.VALID_UNTIL} >= {today}",
                            "memberId" to memberId,
                            "today" to longDateFormat.format(now.time))
                    .parseList(rowParser { a: Int ->
                        if (a in feeNames) {
                            feeNames[a]!!
                        } else {
                            "?"
                        }
                    })
        }
        return fees.joinToString(", ")
    }

    private fun memberGroups(memberId: Int): List<Int> = dbHelper.use {
        select(GroupMemberTable.NAME, GroupMemberTable.GROUP)
                .whereArgs("${GroupMemberTable.MEMBER} = {memberId}", "memberId" to memberId)
                .parseList(IntParser)
    }

    fun memberWithPersonalIdExists(personalId: String) = (dbHelper.use {
        select(MemberTable.NAME, MemberTable.PERSONAL_ID).whereArgs(
                "${MemberTable.PERSONAL_ID} = {personalId}",
                "personalId" to personalId).parseOpt(StringParser)
    } != null)

    fun getMemberList() = dbHelper.use {
        select(MemberTable.NAME, MemberTable.ID, MemberTable.FIRST_NAME, MemberTable.FAMILY_NAME,
                MemberTable.PERSONAL_ID, MemberTable.SIGNED)
                .orderBy(MemberTable.FAMILY_NAME)
                .parseList(rowParser { a: Int, b: String, c: String, d: String, e: Int ->
            mapOf(
                    MemberTable.ID to a,
                    MemberMetaTable.FULL_NAME to b + " " + c,
                    MemberMetaTable.DATE_OF_BIRTH to (d.substring(0..3) + "-" + d.substring(4..5)
                            + "-" + d.substring(6..7)),
                    MemberMetaTable.FEES_PAID to feesPaid(a),
                    MemberMetaTable.GROUPS to memberGroups(a),
                    MemberTable.SIGNED to if (e == 1) {
                        ""
                    } else {
                        noAdaString
                    })
        })
    }

    fun getMemberDetails(memberId: Int) = dbHelper.use {
        select(MemberTable.NAME,
                MemberTable.FIRST_NAME,
                MemberTable.FAMILY_NAME,
                MemberTable.PERSONAL_ID,
                MemberTable.GUARDIAN,
                MemberTable.EMAIL,
                MemberTable.PHONE,
                MemberTable.SIGNED).whereArgs("${MemberTable.ID} = {memberId}",
                "memberId" to memberId).parseSingle(rowParser { a: String, b: String, c: String, d: String, e: String, f: String, g: Int ->
            mapOf(MemberTable.FIRST_NAME to a,
                    MemberTable.FAMILY_NAME to b,
                    MemberTable.PERSONAL_ID to c,
                    MemberTable.GUARDIAN to d,
                    MemberTable.EMAIL to e,
                    MemberTable.PHONE to f,
                    MemberTable.SIGNED to (g == 1))
        })
    }

    fun getLastMemberId() = dbHelper.use {
        select(MemberTable.NAME, MemberTable.ID)
                .orderBy(MemberTable.ID, SqlOrderDirection.DESC).limit(1).parseSingle(IntParser)
    }


    fun addNewMember(
            firstName: String,
            familyName: String,
            personalId: String,
            guardian: String,
            email: String,
            phone: String,
            signedAda: Boolean): Int {
        val nextId = nextFreeId(MemberTable.NAME, MemberTable.ID)
        dbHelper.use {
            insert(MemberTable.NAME,
                    MemberTable.ID to nextId,
                    MemberTable.FIRST_NAME to firstName,
                    MemberTable.FAMILY_NAME to familyName,
                    MemberTable.PERSONAL_ID to personalId,
                    MemberTable.GUARDIAN to guardian,
                    MemberTable.EMAIL to email,
                    MemberTable.PHONE to phone,
                    MemberTable.SIGNED to if (signedAda) {
                        1
                    } else {
                        0
                    })
        }
        return nextId
    }

    fun updateMember(
            memberId: Int,
            firstName: String,
            familyName: String,
            guardian: String,
            email: String,
            phone: String,
            signedAda: Boolean) = dbHelper.use {
        update(MemberTable.NAME,
                MemberTable.FIRST_NAME to firstName,
                MemberTable.FAMILY_NAME to familyName,
                MemberTable.GUARDIAN to guardian,
                MemberTable.EMAIL to email,
                MemberTable.PHONE to phone,
                MemberTable.SIGNED to if (signedAda) {
                    1
                } else {
                    0
                })
                .whereArgs("${MemberTable.ID} = {memberId}", "memberId" to memberId).exec()
    }

    fun superUpdateMember(
            memberId: Int,
            firstName: String,
            familyName: String,
            personalId: String,
            guardian: String,
            email: String,
            phone: String,
            signedAda: Boolean) = dbHelper.use {
        update(MemberTable.NAME,
                MemberTable.FIRST_NAME to firstName,
                MemberTable.FAMILY_NAME to familyName,
                MemberTable.PERSONAL_ID to personalId,
                MemberTable.GUARDIAN to guardian,
                MemberTable.EMAIL to email,
                MemberTable.PHONE to phone,
                MemberTable.SIGNED to if (signedAda) {
                    1
                } else {
                    0
                })
                .whereArgs("${MemberTable.ID} = {memberId}", "memberId" to memberId).exec()
    }

    fun removeMemberFromGroup(memberId: Int, groupId: Int) = dbHelper.use {
        delete(GroupMemberTable.NAME,
                "${GroupMemberTable.MEMBER}={memberId} AND ${GroupMemberTable.GROUP}={groupId}",
                "memberId" to memberId, "groupId" to groupId)
    }

    fun addMemberToGroup(memberId: Int, groupId: Int) {
        if (!memberInGroup(memberId, groupId)) {
            dbHelper.use {
                val nextId = nextFreeId(GroupMemberTable.NAME, GroupMemberTable.ID)
                insert(GroupMemberTable.NAME,
                        GroupMemberTable.ID to nextId,
                        GroupMemberTable.GROUP to groupId,
                        GroupMemberTable.MEMBER to memberId,
                        GroupMemberTable.LEADER to 0)
            }
        }
    }

    fun payFee(memberId: Int, feeId: Int) {
        if (!memberHasPaidFee(memberId, feeId)) {
            dbHelper.use {
                val nextId = nextFreeId(PaidFeesTable.NAME, PaidFeesTable.ID)
                val now = Calendar.getInstance(Locale("sv", "SE"))
                val expiryDate = now.get(Calendar.YEAR).toString() +
                        if (now.get(Calendar.MONTH) > 6) {
                            "-12-31"
                        } else {
                            "-06-30"
                        }
                insert(PaidFeesTable.NAME,
                        PaidFeesTable.ID to nextId,
                        PaidFeesTable.MEMBER to memberId,
                        PaidFeesTable.FEE to feeId,
                        PaidFeesTable.VALID_UNTIL to expiryDate)
            }
        }
    }

    fun removeFee(memberId: Int, feeId: Int) = dbHelper.use {
        val now = Calendar.getInstance(Locale("sv", "SE"))
        delete(PaidFeesTable.NAME,
                "${PaidFeesTable.MEMBER} = {memberId} AND " +
                        "${PaidFeesTable.FEE} = {feeId} AND " +
                        "${PaidFeesTable.VALID_UNTIL} >= {today}",
                "memberId" to memberId,
                "feeId" to feeId,
                "today" to longDateFormat.format(now.time))
    }

    fun getTimetable(year: Int, week: Int):
            Map<Int, Triple<String, String, List<TimetableActivity>>> {
        return listOfDays.map { weekday ->
            val calendar = calendarAt(year, week, weekday)
            val today = longDateFormat.format(calendar.time)
            val timetableId = dbHelper.use {
                select(TimetableTable.NAME, TimetableTable.ID)
                        .whereArgs("${TimetableTable.FROM_DATE} <= {today} AND " +
                                "${TimetableTable.LAST_DATE} >= {today} AND " +
                                "${TimetableTable.WEEKDAY} = {weekday}",
                                "today" to today,
                                "weekday" to weekday)
                        .orderBy(TimetableTable.ID, SqlOrderDirection.DESC)
                        .limit(1)
                        .parseOpt(IntParser)
            }
            val activityList: List<TimetableActivity> = if (timetableId == null) {
                listOf()
            } else {
                getActivityList(timetableId)
            }
            val reportedActivities = getReportedActivities(today)
            Pair(weekday, Triple(longDateFormat.format(calendar.time),
                    shortDateFormat.format(calendar.time),
                    mergeActivityLists(activityList, reportedActivities)))
        }.toMap()
    }

    private fun mergeActivityLists(
            scheduledActivities: List<TimetableActivity>,
            reportedActivities: List<TimetableActivity>): List<TimetableActivity> {
        when {
            scheduledActivities.isEmpty() -> return reportedActivities
            reportedActivities.isEmpty() -> return scheduledActivities
        }

        val mergedActivities: MutableList<TimetableActivity> = mutableListOf()
        val scheduledIterator = scheduledActivities.iterator()

        var moreScheduled = true
        var nextScheduled = scheduledIterator.next()

        for (nextReported in reportedActivities) {
            while (nextScheduled.endTime <= nextReported.startTime) {
                mergedActivities.add(nextScheduled)
                if (scheduledIterator.hasNext()) {
                    nextScheduled = scheduledIterator.next()
                } else {
                    moreScheduled = false
                    break
                }
            }
            if (nextReported.replacesScheduled) {
                while (nextScheduled.startTime < nextReported.endTime) {
                    if (scheduledIterator.hasNext()) {
                        nextScheduled = scheduledIterator.next()
                    } else {
                        moreScheduled = false
                        break
                    }
                }
            } else {
                while (nextScheduled.startTime <= nextReported.startTime) {
                    mergedActivities.add(nextScheduled)
                    if (scheduledIterator.hasNext()) {
                        nextScheduled = scheduledIterator.next()
                    } else {
                        moreScheduled = false
                        break
                    }
                }
            }
            mergedActivities.add(nextReported)
        }
        if (moreScheduled) { mergedActivities.add(nextScheduled) }
        for (remainingScheduled in scheduledIterator) {
            mergedActivities.add(remainingScheduled)
        }
        return mergedActivities.toList()
    }

    private fun getReportedActivities(today: String) = dbHelper.use {
        select(ActivityTable.NAME,
                ActivityTable.ID,
                ActivityTable.TYPE,
                ActivityTable.SPORT,
                ActivityTable.GROUP,
                ActivityTable.START,
                ActivityTable.END,
                ActivityTable.REPLACES)
                .whereArgs("${ActivityTable.DATE} = {today}", "today" to today)
                .orderBy(ActivityTable.START)
                .parseList(rowParser { classId: Int, typeId: Int, sportId: Int, groupId: Int,
                                       startTime: String, endTime: String,
                                       replacesScheduled: Int ->
                    TimetableActivity(classId, typeId, sportId, groupId, startTime, endTime, true,
                            replacesScheduled == 1)
                })
    }

    fun getTimetableEntry(year: Int, week: Int, weekday: Int) = dbHelper.use {

        val calendar = calendarAt(year, week, weekday)
        val today = longDateFormat.format(calendar.time)

        select(TimetableTable.NAME,
                TimetableTable.ID,
                TimetableTable.FROM_DATE,
                TimetableTable.LAST_DATE)
                .whereArgs("${TimetableTable.WEEKDAY} = {weekday} AND " +
                        "${TimetableTable.FROM_DATE} <= {today} AND " +
                        "${TimetableTable.LAST_DATE} >= {today}",
                        "weekday" to weekday, "today" to today)
                .orderBy(TimetableTable.ID, SqlOrderDirection.DESC)
                .limit(1)
                .parseOpt(rowParser { t1: Int, t2: String, t3: String ->
                    mapOf(TimetableTable.ID to t1,
                            TimetableTable.FROM_DATE to t2,
                            TimetableTable.LAST_DATE to t3)
                })
    }

    fun getActivityList(timetableId: Int) = dbHelper.use {
        select(ClassesTable.NAME,
                ClassesTable.ID,
                ClassesTable.TYPE,
                ClassesTable.SPORT,
                ClassesTable.GROUP,
                ClassesTable.START,
                ClassesTable.END)
                .whereArgs("${ClassesTable.ID} in (SELECT ${TimetableJunctionTable.CLASS} FROM " +
                        "${TimetableJunctionTable.NAME} WHERE " +
                        "${TimetableJunctionTable.TIMETABLE} = {timetableId})",
                        "timetableId" to timetableId)
                .orderBy(ClassesTable.START)
                .parseList(rowParser { classId: Int, typeId: Int, sportId: Int, groupId: Int,
                                       startTime: String, endTime: String ->
                    TimetableActivity(
                            classId, typeId, sportId, groupId, startTime, endTime, false, false)
                })
    }

    fun ongoingActivity(): TimetableActivity? {
        val now = Calendar.getInstance(Locale("sv", "SE"))
        val nowTime = timeFormat.format(now.time)
        val timetable = getTimetableEntry(now.get(Calendar.YEAR), now.get(Calendar.WEEK_OF_YEAR),
                now.get(Calendar.DAY_OF_WEEK))
        return if (timetable == null) {
            null
        } else {
            val activityList = getActivityList(timetable[TimetableTable.ID] as Int).filter { activity ->
                activity.startTime <= addTime(nowTime, "00:10") &&
                        addTime(activity.endTime, "00:10") >= nowTime
            }
            val k = activityList.lastIndex
            if (k == -1) {
                null
            } else {
                activityList[k]
            }
        }
    }

    fun getParticipants(activityId: Int): List<Int> = dbHelper.use {
        select(ActivityParticipantsTable.NAME,
                ActivityParticipantsTable.MEMBER)
                .whereArgs("${ActivityParticipantsTable.ACTIVITY} = {activityId}",
                        "activityId" to activityId).parseList(IntParser)
    }

    fun getLeaders(activityId: Int): List<Int> = dbHelper.use {
        select(ActivityParticipantsTable.NAME,
                ActivityParticipantsTable.MEMBER)
                .whereArgs("${ActivityParticipantsTable.ACTIVITY} = {activityId} AND " +
                        "${ActivityParticipantsTable.IS_LEADER} = 1",
                        "activityId" to activityId).parseList(IntParser)
    }

    fun newTimetable(weekday: Int, fromDate: String, toDate: String): Int {
        val nextId = nextFreeId(TimetableTable.NAME, TimetableTable.ID)
        dbHelper.use {
            insert(TimetableTable.NAME,
                    TimetableTable.ID to nextId,
                    TimetableTable.WEEKDAY to weekday,
                    TimetableTable.FROM_DATE to fromDate,
                    TimetableTable.LAST_DATE to toDate)
        }
        return nextId
    }

    fun newClass(activity: TimetableActivity): Int {
        val nextId = nextFreeId(ClassesTable.NAME, ClassesTable.ID)
        dbHelper.use {
            insert(ClassesTable.NAME,
                    ClassesTable.ID to nextId,
                    ClassesTable.TYPE to activity.type,
                    ClassesTable.SPORT to activity.sport,
                    ClassesTable.GROUP to activity.group,
                    ClassesTable.START to activity.startTime,
                    ClassesTable.END to activity.endTime)
        }
        return nextId
    }

    fun updateClass(activity: TimetableActivity) = dbHelper.use {
        update(ClassesTable.NAME,
                ClassesTable.TYPE to activity.type,
                ClassesTable.SPORT to activity.sport,
                ClassesTable.GROUP to activity.group,
                ClassesTable.START to activity.startTime,
                ClassesTable.END to activity.endTime)
                .whereArgs("${ClassesTable.ID} = {classId}", "classId" to activity.id).exec()
    }

    private fun classInTimetable(timetableId: Int, classId: Int) = (dbHelper.use {
        select(TimetableJunctionTable.NAME, TimetableJunctionTable.CLASS)
                .whereArgs("${TimetableJunctionTable.TIMETABLE} = {timetableId} AND " +
                        "${TimetableJunctionTable.CLASS} = {classId}",
                        "timetableId" to timetableId,
                        "classId" to classId).parseOpt(IntParser)
    } != null)

    fun addClassToTimetable(timetableId: Int, classId: Int) {
        if (!classInTimetable(timetableId, classId)) {
            dbHelper.use {
                val nextId = nextFreeId(TimetableJunctionTable.NAME, TimetableJunctionTable.ID)
                insert(TimetableJunctionTable.NAME,
                        TimetableJunctionTable.ID to nextId,
                        TimetableJunctionTable.TIMETABLE to timetableId,
                        TimetableJunctionTable.CLASS to classId)
            }
        }
    }

    fun removeClassFromTimetable(timetableId: Int, classId: Int) = dbHelper.use {
        delete(TimetableJunctionTable.NAME,
                "${TimetableJunctionTable.TIMETABLE} = {timetableId} AND " +
                        "${TimetableJunctionTable.CLASS} = {classId}",
                "timetableId" to timetableId,
                "classId" to classId)
    }

    fun addActivity(typeId: Int,
                    sportId: Int,
                    groupId: Int,
                    date: String,
                    startTime: String,
                    endTime: String,
                    replacesScheduled: Boolean): Int {
        val activityId = nextFreeId(ActivityTable.NAME, ActivityTable.ID)
        dbHelper.use {
            insert(ActivityTable.NAME,
                    ActivityTable.ID to activityId,
                    ActivityTable.TYPE to typeId,
                    ActivityTable.SPORT to sportId,
                    ActivityTable.GROUP to groupId,
                    ActivityTable.DATE to date,
                    ActivityTable.START to startTime,
                    ActivityTable.END to endTime,
                    ActivityTable.REPLACES to if (replacesScheduled) {
                        1
                    } else {
                        0
                    })
        }
        return activityId
    }

    fun updateActivity(activityId: Int,
                       typeId: Int,
                       sportId: Int,
                       groupId: Int,
                       date: String,
                       startTime: String,
                       endTime: String,
                       replacesScheduled: Boolean) = dbHelper.use {
        update(ActivityTable.NAME,
                ActivityTable.TYPE to typeId,
                ActivityTable.SPORT to sportId,
                ActivityTable.GROUP to groupId,
                ActivityTable.DATE to date,
                ActivityTable.START to startTime,
                ActivityTable.END to endTime,
                ActivityTable.REPLACES to if (replacesScheduled) {
                    1
                } else {
                    0
                })
                .whereArgs("${ActivityTable.ID} = {activityId}", "activityId" to activityId).exec()
    }

    fun addParticipantsToActivity(activityId: Int, participants: Set<Int>, leaders: Set<Int>) =
            dbHelper.use {
                var nextId = nextFreeId(ActivityParticipantsTable.NAME,
                        ActivityParticipantsTable.ID)
                for (memberId in participants union leaders) {
                    val isLeader = if (memberId in leaders) {
                        1
                    } else {
                        0
                    }
                    insert(ActivityParticipantsTable.NAME,
                            ActivityParticipantsTable.ID to nextId,
                            ActivityParticipantsTable.ACTIVITY to activityId,
                            ActivityParticipantsTable.MEMBER to memberId,
                            ActivityParticipantsTable.IS_LEADER to isLeader)
                    nextId += 1
                }
            }

    fun removeParticipantsFromActivity(activityId: Int,
                                       participants: Set<Int>,
                                       leaders: Set<Int>) = dbHelper.use {
        for (memberId in leaders.minus(participants)) {
            update(ActivityParticipantsTable.NAME,
                    ActivityParticipantsTable.IS_LEADER to 0)
                    .whereArgs("${ActivityParticipantsTable.ACTIVITY}={activityId} AND " +
                            "${ActivityParticipantsTable.MEMBER}={memberId}",
                            "activityId" to activityId, "memberId" to memberId).exec()
        }
        for (memberId in participants) {
            delete(ActivityParticipantsTable.NAME,
                    "${ActivityParticipantsTable.ACTIVITY}={activityId} AND " +
                            "${ActivityParticipantsTable.MEMBER}={memberId}",
                    "activityId" to activityId, "memberId" to memberId)
        }
    }

    // Consider splitting into table-specific functions with validation
    fun insert(table: String, vararg values: Pair<String, Any?>) = dbHelper.use {
        insert(table, *values)
    }

    fun update(table: String, idColumn: String, rowId: Int,
               vararg values: Pair<String, Any?>) = dbHelper.use {
        update(table, *values).whereArgs("$idColumn = {rowId}", "rowId" to rowId).exec()
    }

    fun activityLabel(typeId: Int, sportId: Int, groupId: Int): String = dbHelper.use {
        val sportString = select(SportTable.NAME, SportTable.SHORTHAND)
                .whereArgs("${SportTable.ID} = {sportId}", "sportId" to sportId)
                .parseSingle(StringParser)
        val groupString = select(GroupTable.NAME, GroupTable.SHORTHAND)
                .whereArgs("${GroupTable.ID} = {groupId}", "groupId" to groupId)
                .parseSingle(StringParser)

        if (sportString != "" && groupString != "") {
            "%s\n%s".format(sportString, groupString)
        } else {
            val typeString = select(ActivityTypeTable.NAME, ActivityTypeTable.SHORTHAND)
                    .whereArgs("${ActivityTypeTable.ID} = {typeId}", "typeId" to typeId)
                    .parseSingle(StringParser)
            when {
                (typeString == "") -> sportString
                (groupString != "") -> "%s\n%s".format(typeString, groupString)
                (sportString != "") -> "%s\n%s".format(typeString, sportString)
                else -> typeString
            }
        }
    }

    fun import(): Int {
        val importPath = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOWNLOADS).path + "/" + importName
        val importedDb = File(importPath)
        dbHelper.close()
        val appDb = File(dbHelper.database_path)
        return if (importedDb.exists()) {
            backup()
            importedDb.copyTo(appDb, true)
            dbHelper.writableDatabase.close()
            0
        } else {
            1
        }
    }

    fun export(): Int {
        val exportPath = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOWNLOADS).path + "/" + exportName
        val appDb = File(dbHelper.database_path)
        val exportedDb = File(exportPath)
        exportedDb.parentFile.mkdirs()
        appDb.copyTo(exportedDb, true)
        return 0
    }

    private fun backup() {
        val backupIndex = nextBackupIndex()
        val backupPath = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOWNLOADS).path + "/" + backupNameFormat.format(backupIndex)
        val appDb = File(dbHelper.database_path)
        val backupDb = File(backupPath)
        backupDb.parentFile.mkdirs()
        appDb.copyTo(backupDb)
    }

    private fun nextBackupIndex(): Int {
        val backupDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOWNLOADS).path
        var k = 0
        while (File(backupDir + "/" + backupNameFormat.format(k)).exists()) {
            k += 1
        }
        return k
    }

    fun memberStats(): HashMap<String, Int> {
        return HashMap()
    }

}