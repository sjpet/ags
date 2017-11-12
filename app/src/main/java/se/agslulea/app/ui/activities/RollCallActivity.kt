package se.agslulea.app.ui.activities

import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import org.jetbrains.anko.startActivity

import se.agslulea.app.R
import se.agslulea.app.data.db.ActivityTable
import se.agslulea.app.data.db.AppDb
import se.agslulea.app.data.db.MemberMetaTable
import se.agslulea.app.data.db.MemberTable
import se.agslulea.app.helpers.filterMemberList

class RollCallActivity : AppCompatActivity() {

    private lateinit var members: List<Map<String, Any>>
    private lateinit var memberList: ListView
    private val selectedMembers: MutableSet<Int> = mutableSetOf()
    private val selectedLeaders: MutableSet<Int> = mutableSetOf()

    private var rollCallCounter: TextView? = null
    private var searchQuery: String? = null

    private val db = AppDb()

    private var group: Int = -1
    private var existingActivityId: Int = -1
    private var type: Int = -1
    private var sport: Int = -1
    private var date: String = ""
    private var startTime: String = ""
    private var endTime: String = ""
    private var replacesScheduled: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_roll_call)

        type = intent.getIntExtra("type", -1)
        sport = intent.getIntExtra("sport", -1)
        group = intent.getIntExtra("group", -1)
        date = intent.getStringExtra("date")
        startTime = intent.getStringExtra("startTime")
        endTime = intent.getStringExtra("endTime")
        replacesScheduled = intent.getBooleanExtra("replacesScheduled", false)
        existingActivityId = intent.getIntExtra("activityId", -1)

        val (existingParticipants, existingLeaders) = if (existingActivityId > -1) {
            Pair(db.getParticipants(existingActivityId).toSet(),
                 db.getLeaders(existingActivityId).toSet())
        } else {
            Pair(setOf(), setOf())
        }

        selectedMembers.addAll(existingParticipants)
        selectedLeaders.addAll(existingLeaders)

        setTitle()

        memberList = findViewById(R.id.roll_call_member_list_view) as ListView
        val searchBar = findViewById(R.id.roll_call_search_bar) as SearchView
        val saveButton = findViewById(R.id.roll_call_save_button) as Button
        rollCallCounter = findViewById(R.id.roll_call_counter) as TextView

        if (existingActivityId > -1) {
            val modifyButton = findViewById(R.id.modify_activity_button) as Button
            modifyButton.visibility = View.VISIBLE
            modifyButton.setOnClickListener {
                startActivity<NewActivityActivity>(
                        "activityId" to existingActivityId,
                        "type" to type,
                        "sport" to sport,
                        "group" to group,
                        "date" to date,
                        "startTime" to startTime,
                        "endTime" to endTime,
                        "replacesScheduled" to replacesScheduled)
            }
        }

        updateCounter(rollCallCounter!!, getString(R.string.number_picked), selectedMembers,
                selectedLeaders)

        members = db.getMemberList()

        showMembers()

        // Update filter on search bar activity
        searchBar.setOnQueryTextListener(object: SearchView.OnQueryTextListener {
            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText == "") {
                    searchQuery = null
                }
                showMembers()
                return false
            }
            override fun onQueryTextSubmit(query: String?): Boolean {
                searchQuery = query
                showMembers()
                return false
            }
        })

        saveButton.setOnClickListener {
            val activityId = if (existingActivityId == -1) {
                db.addActivity(type, sport, group, date, startTime, endTime, replacesScheduled)
            } else {
                existingActivityId
            }
            db.removeParticipantsFromActivity(activityId,
                    existingParticipants.minus(selectedMembers),
                    existingLeaders.minus(selectedLeaders))
            db.addParticipantsToActivity(activityId,
                    selectedMembers.minus(existingParticipants),
                    selectedLeaders.minus(existingLeaders))
            finish()
        }
    }

    override fun onResume() {
        super.onResume()
        if (existingActivityId > -1) {
            val existingActivity = db.getActivityDetails(existingActivityId)
            type = existingActivity[ActivityTable.TYPE] as Int
            sport = existingActivity[ActivityTable.SPORT] as Int
            group = existingActivity[ActivityTable.GROUP] as Int
            date = existingActivity[ActivityTable.DATE] as String
            startTime = existingActivity[ActivityTable.START] as String
            endTime = existingActivity[ActivityTable.END] as String
            replacesScheduled = existingActivity[ActivityTable.REPLACES] as Boolean
            setTitle()
        }
    }

    private fun setTitle() {
        title = getString(R.string.roll_call_title_template).format(date, startTime, endTime,
                makeTitle(db.getActivityTypeName(type),
                        db.getSportShorthand(sport),
                        db.getGroupName(group),
                        group))
    }

    private class PickerAdapter(
            val ctx: Context,
            val selectedMembers: MutableSet<Int>,
            val selectedLeaders: MutableSet<Int>,
            val rollCallCounter: TextView,
            val memberArrayList: List<Map<String, Any>>,
            keys: Array<String>,
            viewResourceIds: IntArray) : SimpleAdapter(
            ctx,
            memberArrayList,
            R.layout.pick_member_list_item,
            keys,
            viewResourceIds) {

        private inner class MemberItemHolder(val checkBox: CheckBox,
                                             val leaderCheckBox: CheckBox,
                                             val memberId: TextView,
                                             val memberName: TextView,
                                             val dateOfBirth: TextView,
                                             val feesPaid: TextView,
                                             val signed: TextView)

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {

            val thisView: View
            val holder: MemberItemHolder

            if (convertView == null) {
                val vi = ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                thisView = vi.inflate(R.layout.roll_call_member_list_item, parent, false)

                holder = MemberItemHolder(
                        thisView.findViewById(R.id.roll_call_member_list_check_box) as CheckBox,
                        thisView.findViewById(R.id.roll_call_member_list_leader_box) as CheckBox,
                        thisView.findViewById(R.id.roll_call_member_list_id) as TextView,
                        thisView.findViewById(R.id.roll_call_member_list_name) as TextView,
                        thisView.findViewById(R.id.roll_call_member_list_date_of_birth) as TextView,
                        thisView.findViewById(R.id.roll_call_member_list_fees) as TextView,
                        thisView.findViewById(R.id.roll_call_member_list_signed) as TextView)

                thisView.tag = holder

                holder.checkBox.setOnClickListener { v ->
                    val cb = v as CheckBox
                    val memberId = cb.tag as Int
                    if (cb.isChecked) {
                        selectedMembers.add(memberId)
                    } else {
                        selectedMembers.remove(memberId)
                    }
                    updateCounter(rollCallCounter, ctx.getString(R.string.number_picked),
                            selectedMembers, selectedLeaders)
                }

                holder.leaderCheckBox.setOnClickListener { v ->
                    val cb = v as CheckBox
                    val memberId = cb.tag as Int
                    if (cb.isChecked) {
                        selectedLeaders.add(memberId)
                    } else {
                        selectedLeaders.remove(memberId)
                    }
                    updateCounter(rollCallCounter, ctx.getString(R.string.number_picked),
                            selectedMembers, selectedLeaders)
                }

            } else {
                thisView = convertView
                holder = thisView.tag as MemberItemHolder
            }

            val thisMember = memberArrayList[position]
            val memberId = thisMember[MemberTable.ID] as Int
            holder.checkBox.isChecked = (memberId in selectedMembers)
            holder.leaderCheckBox.isChecked = (memberId in selectedLeaders)
            holder.memberId.text = memberId.toString()
            holder.memberName.text = thisMember[MemberMetaTable.FULL_NAME] as String
            holder.dateOfBirth.text = thisMember[MemberMetaTable.DATE_OF_BIRTH] as String
            holder.feesPaid.text = thisMember[MemberMetaTable.FEES_PAID] as String
            holder.signed.text = thisMember[MemberTable.SIGNED] as String
            holder.checkBox.tag = memberId
            holder.leaderCheckBox.tag = memberId

            return thisView
        }
    }

    private fun showMembers() {
        memberList.adapter = PickerAdapter(
                this,
                selectedMembers,
                selectedLeaders,
                rollCallCounter!!,
                filterMemberList(members, group, searchQuery,
                        selectedMembers union selectedLeaders),
                arrayOf(MemberTable.ID,
                        "inGroup",
                        MemberMetaTable.FULL_NAME,
                        MemberMetaTable.DATE_OF_BIRTH,
                        MemberMetaTable.FEES_PAID),
                intArrayOf(R.id.pick_member_list_id,
                        R.id.pick_member_list_check_box,
                        R.id.pick_member_list_name,
                        R.id.pick_member_list_date_of_birth,
                        R.id.pick_member_list_fees)
        )
    }

    private fun makeTitle(type: String, sport: String, group: String, groupId: Int) =
            type + if (sport == "") { "" } else { ", %s".format(sport) } +
                    if (groupId == 0) { "" } else { ", %s".format(group) }
}

private fun updateCounter(textView: TextView,
                          counterString: String,
                          participants: MutableSet<Int>,
                          leaders: MutableSet<Int>) {
    val totalCount = (participants union leaders).size
    textView.text = counterString.format(totalCount, if (totalCount == 1) { "" } else { "a" },
            leaders.size)
}