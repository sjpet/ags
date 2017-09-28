package se.agslulea.app.ui.activities

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import org.jetbrains.anko.clearTop
import org.jetbrains.anko.intentFor
import org.jetbrains.anko.startActivity
import se.agslulea.app.R

class AdminMainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_main)

        val adminLevel = intent.getIntExtra("adminLevel", 0)

        val membersButton = findViewById(R.id.admin_members_button) as Button
        val timetableButton = findViewById(R.id.admin_timetable_button) as Button
        val databaseButton = findViewById(R.id.database_button) as Button
        val changePasswordButton = findViewById(R.id.change_password_button) as Button
        val logOutButton = findViewById(R.id.log_out_button) as Button

        membersButton.setOnClickListener {
            startActivity<ListMembersActivity>("adminLevel" to adminLevel)
        }

        timetableButton.setOnClickListener {
            startActivity<TimetableActivity>("adminLevel" to adminLevel)
        }

        databaseButton.setOnClickListener {
            startActivity<ModifyDatabaseActivity>("adminLevel" to adminLevel)
        }

        changePasswordButton.setOnClickListener {
            startActivity<ChangePasswordActivity>("adminLevel" to adminLevel)
        }

        logOutButton.setOnClickListener {
            startActivity(intentFor<MainActivity>().clearTop())
        }
    }
}
