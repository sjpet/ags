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

        val level = intent.getIntExtra("level", 0)

        val members_button = findViewById(R.id.admin_members_button) as Button
        val database_button = findViewById(R.id.database_button) as Button
        val change_password_button = findViewById(R.id.change_password_button) as Button
        val log_out_button = findViewById(R.id.log_out_button) as Button

        database_button.setOnClickListener {
            startActivity<ModifyDatabaseActivity>("level" to level)
        }

        change_password_button.setOnClickListener {
            startActivity<ChangePasswordActivity>("level" to level)
        }

        log_out_button.setOnClickListener {
            startActivity(intentFor<MainActivity>().clearTop())
        }
    }
}
