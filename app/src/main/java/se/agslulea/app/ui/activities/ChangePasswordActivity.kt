package se.agslulea.app.ui.activities

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import org.jetbrains.anko.toast

import se.agslulea.app.R
import se.agslulea.app.data.db.AppDb

class ChangePasswordActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_change_password)

        val level = intent.getIntExtra("level", 0)
        if (level < 1) {
            toast(R.string.action_not_allowed)
            finish()
        }

        val currentPasswordText = findViewById(R.id.current_password_text) as EditText
        val currentPasswordLabel = findViewById(R.id.current_password_label) as TextView
        val newPasswordText = findViewById(R.id.new_password_text) as EditText
        val repeatPasswordText = findViewById(R.id.repeat_password_text) as EditText
        val button = findViewById(R.id.change_password_button) as Button

        val db = AppDb()

        val actualCurrentPassword = db.getPassword(level)

        if (actualCurrentPassword == null) {
            currentPasswordText.visibility = View.GONE
            currentPasswordText.isClickable = false
            currentPasswordLabel.visibility = View.GONE
        }

        // Set an appropriate title
        val sb = StringBuilder(if (actualCurrentPassword == null) {
            getString(R.string.choose)
        } else {
            getString(R.string.change)
        })
        sb.append(" ${getString(R.string.title_infix)} ")
        sb.append(if (level == 1) {
            getString(R.string.super_admin)
        } else {
            getString(R.string.regular_admin)
        })
        title = sb.toString()

        button.setOnClickListener {
            val currentPassword = currentPasswordText.text.toString()
            val newPassword = newPasswordText.text.toString()
            val repeatedPassword = repeatPasswordText.text.toString()

            if (currentPassword != actualCurrentPassword && actualCurrentPassword != null) {
                toast(R.string.current_password_mismatch)
            } else if (newPassword != repeatedPassword) {
                toast(R.string.new_password_mismatch)
            } else {
                db.setPassword(level, newPassword)
                toast(R.string.password_changed)
                finish()
            }

        }
    }
}
