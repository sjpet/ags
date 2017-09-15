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

        val members_button = findViewById(R.id.members_button) as Button
        val admin_button = findViewById(R.id.admin_button) as Button

        members_button.setOnClickListener {
            startActivity<ListMembersActivity>("adminLevel" to 0)
        }
        admin_button.setOnClickListener{
            startActivity<AdminLoginActivity>()
        }
    }
}
