package se.agslulea.app.data.db

object PasswordsTable {
    val NAME = "Passwords"
    val LEVEL = "Level"
    val KEY = "Passphrase"
}

object ActivityTable {
    val NAME = "Activities"
    val ID = "_id"
    val TYPE = "TypeId"
    val SPORT = "SportId"
    val GROUP = "GroupId"
    val DATE = "Date"
    val START = "StartTime"
    val END = "EndTime"
}

object ActivityTypeTable {
    val NAME = "ActivityTypes"
    val ID ="_id"
    val TYPE = "Type"
    val IS_ACTIVE = "IsActive"
}

object ActivityParticipantsTable {
    val NAME = "ActivityParticipants"
    val ID = "_id"
    val ACTIVITY = "ActivityId"
    val MEMBER = "MemberId"
    val IS_LEADER = "IsLeader"
}

object ClassesTable {
    val NAME = "Classes"
    val ID = "_id"
    val SPORT = "SportId"
    val GROUP = "GroupId"
    val WEEKDAY = "Weekday"
    val START = "StartTime"
    val END = "EndTime"
    val FROM_DATE = "FirstDate"
    val TO_DATE = "LastDate"
}

object SportTable {
    val NAME = "Sports"
    val ID = "_id"
    val SPORT = "Sport"
    val SHORTHAND = "Shorthand"
    val IS_ACTIVE = "IsActive"
}

object GroupTable {
    val NAME = "Groups"
    val ID = "_id"
    val GROUP = "GroupName"
    val IS_ACTIVE = "IsActive"
}

object MemberTable {
    val NAME = "Members"
    val ID = "_id"
    val FIRST_NAME = "FirstName"
    val FAMILY_NAME = "FamilyName"
    val PERSONAL_ID = "PersonalIdNumber"
    val EMAIL = "Email"
    val PHONE = "PhoneNumber"
    val GUARDIAN = "Guardian"
    val SIGNED = "SignedAntiDoping"
}

object GroupMemberTable {
    val NAME = "GroupMembers"
    val ID = "_id"
    val MEMBER = "MemberId"
    val GROUP = "GroupId"
    val LEADER = "IsLeader"
}

object FeeTable {
    val NAME = "Fees"
    val ID = "_id"
    val KEY = "Key"
    val FEE = "Fee"
    val PERIOD = "PERIOD"
    val IS_ACTIVE = "IsActive"
}

object PaidFeesTable {
    val NAME = "FeesPaid"
    val ID = "_id"
    val MEMBER = "MemberId"
    val FEE = "FeeId"
    val VALID_UNTIL = "ValidUntil"
}