package se.agslulea.app.data.db

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import org.jetbrains.anko.db.*
import se.agslulea.app.ui.App

class DbHelper(ctx: Context = App.instance) : ManagedSQLiteOpenHelper(ctx, DbHelper.DB_NAME, null,
        DbHelper.DB_VERSION) {

    companion object {
        val DB_NAME = "ags.db"
        val DB_VERSION = 3
        val instance by lazy { DbHelper() }
    }

    override fun onCreate(db: SQLiteDatabase) {

        db.createTable(PasswordsTable.NAME, true,
                PasswordsTable.LEVEL to INTEGER + PRIMARY_KEY + UNIQUE,
                PasswordsTable.KEY to TEXT)

        db.createTable(ActivityTable.NAME, true,
                ActivityTable.ID to INTEGER + PRIMARY_KEY + UNIQUE,
                ActivityTable.TYPE to INTEGER,
                ActivityTable.SPORT to INTEGER,
                ActivityTable.GROUP to INTEGER,
                ActivityTable.DATE to TEXT, //
                ActivityTable.START to TEXT,
                ActivityTable.END to TEXT)

        db.createTable(ActivityTypeTable.NAME, true,
                ActivityTypeTable.ID to INTEGER + PRIMARY_KEY + UNIQUE,
                ActivityTypeTable.TYPE to TEXT + UNIQUE,
                ActivityTypeTable.IS_ACTIVE to INTEGER)

        db.createTable(ActivityParticipantsTable.NAME, true,
                ActivityParticipantsTable.ID to INTEGER + PRIMARY_KEY + UNIQUE,
                ActivityParticipantsTable.ACTIVITY to INTEGER,
                ActivityParticipantsTable.MEMBER to INTEGER,
                ActivityParticipantsTable.IS_LEADER to INTEGER)

        db.createTable(SportTable.NAME, true,
                SportTable.ID to INTEGER + PRIMARY_KEY + UNIQUE,
                SportTable.SPORT to TEXT + UNIQUE,
                SportTable.SHORTHAND to TEXT + UNIQUE,
                SportTable.IS_ACTIVE to INTEGER)

        db.createTable(GroupTable.NAME, true,
                GroupTable.ID to INTEGER + PRIMARY_KEY + UNIQUE,
                GroupTable.GROUP to TEXT + UNIQUE,
                GroupTable.IS_ACTIVE to INTEGER)

        db.createTable(MemberTable.NAME, true,
                MemberTable.ID to INTEGER + PRIMARY_KEY + UNIQUE,
                MemberTable.FIRST_NAME to TEXT,
                MemberTable.FAMILY_NAME to TEXT,
                MemberTable.PERSONAL_ID to TEXT,
                MemberTable.EMAIL to TEXT,
                MemberTable.PHONE to TEXT,
                MemberTable.GUARDIAN to TEXT,
                MemberTable.SIGNED to INTEGER)

        db.createTable(GroupMemberTable.NAME, true,
                GroupMemberTable.ID to INTEGER + PRIMARY_KEY + UNIQUE,
                GroupMemberTable.MEMBER to INTEGER,
                GroupMemberTable.GROUP to INTEGER,
                GroupMemberTable.LEADER to INTEGER)

        db.createTable(FeeTable.NAME, true,
                FeeTable.ID to INTEGER + PRIMARY_KEY + UNIQUE,
                FeeTable.KEY to TEXT + UNIQUE,
                FeeTable.FEE to TEXT + UNIQUE,
                FeeTable.PERIOD to TEXT,
                FeeTable.IS_ACTIVE to INTEGER)

        db.createTable(PaidFeesTable.NAME, true,
                PaidFeesTable.ID to INTEGER + PRIMARY_KEY + UNIQUE,
                PaidFeesTable.MEMBER to INTEGER,
                PaidFeesTable.FEE to INTEGER,
                PaidFeesTable.VALID_UNTIL to TEXT)

        db.createTable(ClassesTable.NAME, true,
                ClassesTable.ID to INTEGER + PRIMARY_KEY + UNIQUE,
                ClassesTable.SPORT to INTEGER,
                ClassesTable.GROUP to INTEGER,
                ClassesTable.WEEKDAY to TEXT,
                ClassesTable.START to TEXT,
                ClassesTable.END to TEXT,
                ClassesTable.FROM_DATE to TEXT,
                ClassesTable.TO_DATE to TEXT)

    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {

        db.dropTable(PasswordsTable.NAME, true)
        db.dropTable(ActivityTable.NAME, true)
        db.dropTable(ActivityTypeTable.NAME, true)
        db.dropTable(ActivityParticipantsTable.NAME, true)
        db.dropTable(SportTable.NAME, true)
        db.dropTable(GroupTable.NAME, true)
        db.dropTable(MemberTable.NAME, true)
        db.dropTable(GroupMemberTable.NAME, true)
        db.dropTable(FeeTable.NAME, true)
        db.dropTable(PaidFeesTable.NAME, true)
        db.dropTable(ClassesTable.NAME, true)

        onCreate(db)
    }
}