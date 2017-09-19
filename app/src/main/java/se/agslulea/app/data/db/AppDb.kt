package se.agslulea.app.data.db

import org.jetbrains.anko.db.*
import java.lang.reflect.Member

class AppDb(private val dbHelper: DbHelper = DbHelper.instance) {

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

    fun getGroups() = dbHelper.use {
        select(GroupTable.NAME, GroupTable.GROUP, GroupTable.IS_ACTIVE).parseList(rowParser {
            a: String, b: Int -> listOf(a, b)
        })
    }

    fun getGroupNames() = dbHelper.use {
        select(GroupTable.NAME, GroupTable.ID, GroupTable.GROUP).parseList(rowParser {
            id: Int, group: String -> mapOf(GroupTable.ID to id, GroupTable.GROUP to group)
        }).sortedBy { x -> x[GroupTable.ID] as Int}
    }

    fun getSports() = dbHelper.use {
        select(SportTable.NAME, SportTable.SPORT, SportTable.SHORTHAND,
                SportTable.IS_ACTIVE).parseList(rowParser {
            a: String, b: String, c: Int -> listOf(a, b, c)
        })
    }

    fun getFees() = dbHelper.use {
        // delete(FeeTable.NAME, "${FeeTable.KEY} = 'V'")
        // delete(FeeTable.NAME, "${FeeTable.KEY} = 'M'")
        select(FeeTable.NAME, FeeTable.FEE, FeeTable.KEY, FeeTable.PERIOD,
                FeeTable.IS_ACTIVE).parseList(rowParser {
            a: String, b: String, c: String, d: Int -> listOf(a, b, c, d)
        })
    }

    fun getActivityTypes() = dbHelper.use {
        select(ActivityTypeTable.NAME, ActivityTypeTable.TYPE,
                ActivityTypeTable.IS_ACTIVE).parseList(rowParser {
            a: String, b: Int -> listOf(a, b)
        })
    }

    fun nextFreeId(tableName: String, idColumn: String): Int {
        val maxId = dbHelper.use {
            select(tableName, idColumn)
                    .orderBy(idColumn, SqlOrderDirection.DESC)
                    .limit(1)
                    .parseOpt(IntParser)
        }
        return if (maxId == null) { 0 } else { maxId + 1 }
    }

    fun memberInGroup(memberId: Int, groupId: Int) = (dbHelper.use {
        select(GroupMemberTable.NAME, GroupMemberTable.ID).whereArgs(
                "${GroupMemberTable.GROUP}={groupId} AND ${GroupMemberTable.MEMBER}={memberId}",
                "groupId" to groupId, "memberId" to memberId).parseOpt(IntParser) } != null)

    private fun feesPaid(memberId: Int): String {
        val fees = dbHelper.use {
            val feeNames = select(FeeTable.NAME, FeeTable.ID, FeeTable.KEY).parseList(rowParser {
                a: Int, b: String -> Pair(a, b)
            }).associateBy({it.first}, {it.second})
            select(PaidFeesTable.NAME, PaidFeesTable.FEE)
                    .whereArgs("${PaidFeesTable.MEMBER} = {memberId}", "memberId" to memberId)
                    .parseList(rowParser{
                        a: Int -> if (a in feeNames) {
                            feeNames[a]!!
                        } else {
                            "?"
                        }
                    })
        }
        return fees.joinToString(", ")
    }

    fun memberWithPersonalIdExists(personalId: String) = (dbHelper.use {
        select(MemberTable.NAME, MemberTable.PERSONAL_ID).whereArgs(
                "${MemberTable.PERSONAL_ID} = {personalId}",
                "personalId" to personalId).parseOpt(StringParser) } != null)

    fun getMemberList() = dbHelper.use {
        select(MemberTable.NAME, MemberTable.ID, MemberTable.FIRST_NAME, MemberTable.FAMILY_NAME,
                MemberTable.PERSONAL_ID).parseList(rowParser {
            a: Int, b: String, c: String, d: String -> mapOf(
                MemberTable.ID to a,
                MemberMetaTable.FULL_NAME to b + " " + c,
                MemberMetaTable.DATE_OF_BIRTH to (d.substring(0..3) + "-" + d.substring(4..5)
                        + "-" + d.substring(6..7)),
                MemberMetaTable.FEES_PAID to feesPaid(a))
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
                "memberId" to memberId).parseSingle(rowParser{
            a: String, b: String, c: String, d: String, e: String, f: String, g: Int ->
            mapOf(MemberTable.FIRST_NAME to a,
                    MemberTable.FAMILY_NAME to b,
                    MemberTable.PERSONAL_ID to c,
                    MemberTable.GUARDIAN to d,
                    MemberTable.EMAIL to e,
                    MemberTable.PHONE to f,
                    MemberTable.SIGNED to (g == 1))
        })
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
                    MemberTable.SIGNED to if (signedAda) { 1 } else { 0 })
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
                MemberTable.SIGNED to if (signedAda) { 1 } else { 0 })
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
                MemberTable.SIGNED to if (signedAda) { 1 } else { 0 })
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

    // Consider splitting into table-specific functions with validation
    fun insert(table: String, vararg values: Pair<String, Any?>) = dbHelper.use {
        insert(table, *values)
    }

    fun update(table: String, idColumn: String, rowId: Int,
               vararg values: Pair<String, Any?>) = dbHelper.use {
        update(table, *values).whereArgs("$idColumn = {rowId}", "rowId" to rowId).exec()
    }
}
