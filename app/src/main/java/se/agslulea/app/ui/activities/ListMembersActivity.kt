package se.agslulea.app.ui.activities

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.SimpleAdapter
import android.widget.Spinner

import se.agslulea.app.R

class ListMembersActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list_members)

        // val adminLevel = intent.getIntExtra("adminLevel", 0)

        val groupSpinner = findViewById(R.id.member_list_group_spinner) as Spinner

        val groupList = listOf(
                mapOf("id" to 0, "name" to "Item 1"),
                mapOf("id" to 1, "name" to "Item 2"))
        groupSpinner.adapter = SimpleAdapter(
                this,
                groupList, 
                R.layout.id_string_item,
                arrayOf("id", "name"),
                intArrayOf(R.id.item_id, R.id.item_name))

    }
}
