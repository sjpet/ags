package se.agslulea.app.ui.activities

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*

import se.agslulea.app.R
import se.agslulea.app.data.db.AppDb
import se.agslulea.app.data.db.GroupTable
import se.agslulea.app.data.db.MemberMetaTable
import se.agslulea.app.data.db.MemberTable
import se.agslulea.app.helpers.filterMemberList

class PickMembersActivity : AppCompatActivity() {

    private lateinit var members: List<Map<String, Any>>
    private lateinit var memberList: ListView
    private var selectedMembers: MutableSet<Int> = mutableSetOf()

    private var searchQuery: String? = null

    private val db = AppDb()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pick_members)

        val what = intent.getStringExtra("what")
        val which = intent.getIntExtra("which", -1)

        title = when (what) {
            GroupTable.NAME -> {
                getString(R.string.edit_group) + " " + db.getGroupName(which)
            }
            else -> { getString(R.string.app_name) }
        }

        memberList = findViewById(R.id.pick_member_list_view) as ListView
        val searchBar = findViewById(R.id.pick_members_search_bar) as SearchView
        val saveButton = findViewById(R.id.pick_members_save_button) as Button

        members = db.getMemberList()

        members.filter { which in it[MemberMetaTable.GROUPS] as List<Int> }
                .map { selectedMembers.add(it[MemberTable.ID] as Int) }

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
            val baselineSelected = members.filter {
                which in it[MemberMetaTable.GROUPS] as List<Int>
            }
                    .map { it[MemberTable.ID] as Int }.toSet()
            baselineSelected.filterNot { it in selectedMembers }.map {
                db.removeMemberFromGroup(it, which)
            }
            selectedMembers.filterNot { it in baselineSelected }.map {
                db.addMemberToGroup(it, which)
            }
            finish()
        }
    }

    private class PickerAdapter(
            val ctx: Context,
            val selectedMembers: MutableSet<Int>,
            val memberArrayList: List<Map<String, Any>>,
            keys: Array<String>,
            viewResourceIds: IntArray) : SimpleAdapter(
                    ctx,
                    memberArrayList,
                    R.layout.pick_member_list_item,
                    keys,
                    viewResourceIds) {

        private inner class MemberItemHolder(val checkBox: CheckBox,
                val memberId: TextView,
                val memberName: TextView,
                val dateOfBirth: TextView)

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {

            val thisView: View
            val holder: MemberItemHolder

            if (convertView == null) {
                val vi = ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                thisView = vi.inflate(R.layout.pick_member_list_item, parent, false)

                holder = MemberItemHolder(
                        thisView.findViewById(R.id.pick_member_list_check_box) as CheckBox,
                        thisView.findViewById(R.id.pick_member_list_id) as TextView,
                        thisView.findViewById(R.id.pick_member_list_name) as TextView,
                        thisView.findViewById(R.id.pick_member_list_date_of_birth) as TextView)

                thisView.tag = holder

                holder.checkBox.setOnClickListener { v ->
                    val cb = v as CheckBox
                    val memberId = cb.tag as Int
                    //checkedMap[memberId] = !checkedMap[memberId]!!
                    if (cb.isChecked) {
                        selectedMembers.add(memberId)
                    } else {
                        selectedMembers.remove(memberId)
                    }
                }
            } else {
                thisView = convertView
                holder = thisView.tag as MemberItemHolder
            }

            val thisMember = memberArrayList[position]
            val memberId = thisMember[MemberTable.ID] as Int
            holder.checkBox.isChecked = (memberId in selectedMembers) //checkedMap[memberId]!!
            holder.memberId.text = memberId.toString()
            holder.memberName.text = thisMember[MemberMetaTable.FULL_NAME] as String
            holder.dateOfBirth.text = thisMember[MemberMetaTable.DATE_OF_BIRTH] as String
            holder.checkBox.tag = memberId

            return thisView
        }
    }

    private fun showMembers() {
        memberList.adapter = PickerAdapter(
                this,
                selectedMembers,
                filterMemberList(members, 0, searchQuery),
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
}
