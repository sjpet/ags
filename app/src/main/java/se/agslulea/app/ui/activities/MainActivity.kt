package se.agslulea.app.ui.activities

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import org.jetbrains.anko.startActivity
import se.agslulea.app.R

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val membersButton = findViewById(R.id.members_button) as Button
        val activityButton = findViewById(R.id.activity_button) as Button
        val adminButton = findViewById(R.id.admin_button) as Button
        val statsButton = findViewById(R.id.stats_button) as Button

        membersButton.setOnClickListener {
            startActivity<ListMembersActivity>("adminLevel" to 0)
        }

        activityButton.setOnClickListener {
            startActivity<TimetableActivity>()
        }

        adminButton.setOnClickListener{
            startActivity<AdminLoginActivity>()
        }

        statsButton.setOnClickListener {
            startActivity<MyActivity>()
        }

    }
}
