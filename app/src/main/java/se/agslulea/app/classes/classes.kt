package se.agslulea.app.classes

class Activity(val type: Int,
               val sport: Int,
               val group: Int,
               val label: String,
               val date: String,
               val startTime: String,
               val endTime: String)

class TimetableActivity(val id: Int,
                        var type: Int,
                        var sport: Int,
                        var group: Int,
                        var startTime: String,
                        var endTime: String,
                        var isReported: Boolean,
                        var replacesScheduled: Boolean)