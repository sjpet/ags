package se.agslulea.app.ui.activities

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import org.jetbrains.anko.db.*
import org.jetbrains.anko.intentFor
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.toast
import se.agslulea.app.R
import se.agslulea.app.data.db.AppDb

class AdminLoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_login)

        val db = AppDb()

        val passwordText = findViewById(R.id.password_text) as EditText
        val loginButton = findViewById(R.id.login_button) as Button

        val current_super_password = db.getPassword(1)
        val current_regular_password = db.getPassword(2)

        if (current_regular_password == null) {
            startActivity<ChangePasswordActivity>("level" to 2)
        }

        if (current_super_password == null) {
            startActivity<ChangePasswordActivity>("level" to 1)
        }

        loginButton.setOnClickListener {
            val super_password = db.getPassword(1)
            val regular_password = db.getPassword(2)
            when (passwordText.text.toString()) {
                super_password -> startActivity<AdminMainActivity>("level" to 1)
                regular_password -> startActivity<AdminMainActivity>("level" to 2)
                else -> toast(R.string.invalid_password)
            }
        }
    }
}
