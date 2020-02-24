#!/usr/bin/env python3

import sqlite3

YEAR = 2019
SEMESTER = 1    # 0: VT, 2: HT
START_DATE = "{}-0{}-01".format(YEAR, SEMESTER*6+1)


def lok_eligible(student):
    return YEAR - int(student["PersonalIdNumber"][:4]) < 26


def print_activity(activity, leaders, students):
    print("\n{} {}-{}:".format(activity["Date"],
                               activity["StartTime"],
                               activity["EndTime"]))
    for leader in leaders:
        print("  {} {}, {}".format(leader["FirstName"],
                                   leader["FamilyName"],
                                   leader["PersonalIdNumber"]))
    print("  ----------------------------------")
    for student in students:
        print("  {} {}, {}".format(student["FirstName"],
                                   student["FamilyName"],
                                   student["PersonalIdNumber"]))



if __name__ == "__main__":
    with sqlite3.connect("ags_export.db") as conn:
        conn.row_factory = sqlite3.Row
        crs = conn.cursor()

        crs.execute("SELECT * FROM Activities WHERE Date > '{}'".format(START_DATE))
        activities = crs.fetchall()

        for activity in activities:
            crs.execute("SELECT * FROM ActivityParticipants WHERE ActivityId=?",
                        (activity["_id"],))
            participants = crs.fetchall()

            leader_ids = [participant["Memberid"] for participant in participants
                          if participant["IsLeader"]]
            crs.execute("SELECT * FROM Members WHERE _id IN ({})"
                        .format(", ".join(map(str, leader_ids))))
            leaders = crs.fetchall() 

            student_ids = [participant["MemberId"] for participant in participants
                           if not participant["IsLeader"]]
            crs.execute("SELECT * FROM Members WHERE _id IN ({})"
                        .format(", ".join(map(str, student_ids))))
            students = crs.fetchall() 

            if sum(map(lok_eligible, students)) > 2:
                print_activity(activity, leaders, students)
