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

        val admin_button = findViewById(R.id.admin_button) as Button

        admin_button.setOnClickListener{
            startActivity<AdminLoginActivity>()
        }
    }
}
