package se.agslulea.app.data.db

import org.jetbrains.anko.db.*

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

    fun getGroups() = dbHelper.use {
        select(GroupTable.NAME, GroupTable.GROUP, GroupTable.IS_ACTIVE).parseList(rowParser {
            a: String, b: Int -> listOf(a, b)
        })
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

    // Consider splitting into table-specific functions with validation
    fun insert(table: String, vararg values: Pair<String, Any?>) = dbHelper.use {
        insert(table, *values)
    }

    fun update(table: String, idColumn: String, rowId: Int,
               vararg values: Pair<String, Any?>) = dbHelper.use {
        update(table, *values).whereArgs("$idColumn = {rowId}", "rowId" to rowId).exec()
    }

}
