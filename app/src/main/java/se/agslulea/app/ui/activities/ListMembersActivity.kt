package se.agslulea.app.ui.activities

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.*
import org.jetbrains.anko.startActivity
import se.agslulea.app.*

import se.agslulea.app.data.db.AppDb
import se.agslulea.app.data.db.GroupTable
import se.agslulea.app.data.db.MemberMetaTable
import se.agslulea.app.data.db.MemberTable
import se.agslulea.app.helpers.filterMemberList

class ListMembersActivity : AppCompatActivity() {

    private lateinit var members: List<Map<String, Any>>
    private lateinit var memberList: ListView
    private var searchQuery: String? = null

    private  val db = AppDb()

    private lateinit var groupSpinner: Spinner

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list_members)

        val adminLevel = intent.getIntExtra("adminLevel", 0)

        groupSpinner = findViewById(R.id.member_list_group_spinner) as Spinner
        memberList = findViewById(R.id.member_list_view) as ListView
        val newMemberButton = findViewById(R.id.new_member_button) as Button
        val pickMembersButton = findViewById(R.id.pick_members_button) as Button
        val searchBar = findViewById(R.id.member_list_search_bar) as SearchView

        val groups = db.getGroupNames()
        groupSpinner.adapter = SimpleAdapter(
                this,
                groups,
                R.layout.id_string_item,
                arrayOf(GroupTable.ID, GroupTable.GROUP),
                intArrayOf(R.id.item_id, R.id.item_name))

        // Populate member list
        members = db.getMemberList()
        showMembers()

        // Update filter on selection of group
        groupSpinner.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, pos: Int, id: Long) {
                val groupSpinnerSelected = groupSpinner.selectedItem as Map<*, *>
                val selectedGroup = groupSpinnerSelected[GroupTable.ID] as Int
                pickMembersButton.visibility = if (selectedGroup > 0) { View.VISIBLE } else { View.GONE }
                pickMembersButton.isClickable = true
                showMembers()
            }
            override fun onNothingSelected(parent: AdapterView<out Adapter>?) {
                pickMembersButton.visibility = View.GONE
                pickMembersButton.isClickable = false
                showMembers()
            }
        }

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

        // Set listeners for adding or editing members
        newMemberButton.setOnClickListener {
            val groupSpinnerSelected = groupSpinner.selectedItem as Map<*, *>
            val preSelectedGroup = groupSpinnerSelected[GroupTable.ID] as Int
            startActivity<AddOrEditMemberActivity>("preSelectedGroup" to preSelectedGroup,
                    "adminLevel" to adminLevel)
        }

        pickMembersButton.setOnClickListener {
            val groupSpinnerSelected = groupSpinner.selectedItem as Map<*, *>
            val groupId = groupSpinnerSelected[GroupTable.ID] as Int
            startActivity<PickMembersActivity>("what" to GroupTable.NAME, "which" to groupId)
        }
        
        memberList.setOnItemClickListener { _, view, _, _ ->
            val idTextView = view.findViewById(R.id.member_list_id) as TextView
            val memberId = idTextView.text.toString().toInt()
            startActivity<AddOrEditMemberActivity>("memberId" to memberId,
                    "adminLevel" to adminLevel)
        }
    }

    override fun onResume() {
        super.onRestart()
        members = db.getMemberList()
        showMembers()
    }

    private fun showMembers() {
        val groupSpinnerSelected = groupSpinner.selectedItem as Map <*, *>
        val selectedGroup = groupSpinnerSelected[GroupTable.ID] as Int
        memberList.adapter = SimpleAdapter(
                this,
                filterMemberList(members, selectedGroup, searchQuery),
                R.layout.member_list_item,
                arrayOf(MemberTable.ID,
                        MemberMetaTable.FULL_NAME,
                        MemberMetaTable.DATE_OF_BIRTH,
                        MemberMetaTable.FEES_PAID,
                        MemberTable.SIGNED),
                intArrayOf(R.id.member_list_id,
                        R.id.member_list_name,
                        R.id.member_list_date_of_birth,
                        R.id.member_list_fees_paid,
                        R.id.member_list_signed)
        )
    }
}