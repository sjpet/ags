package se.agslulea.app.ui.activities

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.*
import org.jetbrains.anko.startActivity

import se.agslulea.app.R
import se.agslulea.app.data.db.AppDb
import se.agslulea.app.data.db.GroupTable
import se.agslulea.app.data.db.MemberMetaTable
import se.agslulea.app.data.db.MemberTable

class ListMembersActivity : AppCompatActivity() {

    private lateinit var members: List<Map<String, Any>>
    private lateinit var memberList: ListView

    private  val db = AppDb()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list_members)

        val adminLevel = intent.getIntExtra("adminLevel", 0)

        val groupSpinner = findViewById(R.id.member_list_group_spinner) as Spinner
        memberList = findViewById(R.id.member_list_view) as ListView
        val newMemberButton = findViewById(R.id.new_member_button) as Button

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

        newMemberButton.setOnClickListener {
            val groupSpinnerSelected = groupSpinner.selectedItem as Map<*, *>
            val preSelectedGroup = groupSpinnerSelected[GroupTable.ID] as Int
            startActivity<AddOrEditMemberActivity>("preSelectedGroup" to preSelectedGroup,
                    "adminLevel" to adminLevel)
        }
        
        memberList.setOnItemClickListener { _, view, _, _ ->
            val idTextView = view.findViewById(R.id.member_list_id) as TextView
            val memberId = idTextView.text.toString().toInt()
            startActivity<AddOrEditMemberActivity>("memberId" to memberId,
                    "adminLevel" to adminLevel)
        }
    }

    override fun onRestart() {
        super.onRestart()
        members = db.getMemberList()
        showMembers()
    }

    private fun showMembers() {
        memberList.adapter = SimpleAdapter(
                this,
                members,
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
