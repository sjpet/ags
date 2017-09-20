package se.agslulea.app.ui.activities

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.*

import se.agslulea.app.R
import se.agslulea.app.data.db.AppDb
import se.agslulea.app.data.db.MemberMetaTable
import se.agslulea.app.data.db.MemberTable
import se.agslulea.app.filterMemberList

class PickMembersActivity : AppCompatActivity() {

    private lateinit var members: List<Map<String, Any>>
    private lateinit var memberList: ListView

    private var searchQuery: String? = null

    private  val db = AppDb()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pick_members)

        val what = intent.getStringExtra("what")
        val which = intent.getIntExtra("which", -1)

        memberList = findViewById(R.id.pick_member_list_view) as ListView
        val searchBar = findViewById(R.id.pick_members_search_bar) as SearchView
        val saveButton = findViewById(R.id.pick_members_save_button) as Button

        members = db.getMemberList().map {
            member -> member + Pair("inGroup",
                (which in member[MemberMetaTable.GROUPS] as List<Int>)) }
        showMembers()
    }

    private fun showMembers() {
        memberList.adapter = SimpleAdapter(
                this,
                filterMemberList(members, 0, searchQuery),
                R.layout.pick_member_list_item,
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
