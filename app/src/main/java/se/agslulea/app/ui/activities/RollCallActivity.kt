package se.agslulea.app.ui.activities

import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import org.jetbrains.anko.toast

import se.agslulea.app.R
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

    private var group: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_roll_call)

        val type = intent.getIntExtra("type", -1)
        val sport = intent.getIntExtra("sport", -1)
        group = intent.getIntExtra("group", -1)
        val date = intent.getStringExtra("date")
        val startTime = intent.getStringExtra("startTime")
        val endTime = intent.getStringExtra("endTime")

        title = getString(R.string.roll_call_title_template).format(date, startTime, endTime,
                makeTitle(db.getActivityTypeName(type),
                        db.getSportShorthand(sport),
                        db.getGroupName(group),
                        group))

        memberList = findViewById(R.id.roll_call_member_list_view) as ListView
        val searchBar = findViewById(R.id.roll_call_search_bar) as SearchView
        val saveButton = findViewById(R.id.roll_call_save_button) as Button
        rollCallCounter = findViewById(R.id.roll_call_counter) as TextView

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
            /*val baselineSelected = members.filter {
                which in it[MemberMetaTable.GROUPS] as List<Int>
            }
                    .map { it[MemberTable.ID] as Int }.toSet()
            baselineSelected.filterNot { it in selectedMembers }.map {
                db.removeMemberFromGroup(it, which)
            }
            selectedMembers.filterNot { it in baselineSelected }.map {
                db.addMemberToGroup(it, which)
            }*/

            //finish()
        }
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
                                             val dateOfBirth: TextView)

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
                        thisView.findViewById(R.id.roll_call_member_list_date_of_birth) as TextView)

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
                    ctx.toast(selectedMembers.toString())
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
                    ctx.toast(selectedLeaders.toString())
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
                        MemberMetaTable.DATE_OF_BIRTH),
                intArrayOf(R.id.pick_member_list_id,
                        R.id.pick_member_list_check_box,
                        R.id.pick_member_list_name,
                        R.id.pick_member_list_date_of_birth)
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