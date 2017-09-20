package se.agslulea.app.ui.activities

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import org.jetbrains.anko.startActivity

import se.agslulea.app.R
import se.agslulea.app.data.db.*

class ModifyDatabaseActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_modify_database)

        val adminLevel = intent.getIntExtra("adminLevel", 0)

        val groups_button = findViewById(R.id.groups_button) as Button
        val sports_button = findViewById(R.id.sports_button) as Button
        val fees_button = findViewById(R.id.fees_button) as Button
        val activity_types_button = findViewById(R.id.activity_types_button) as Button

        groups_button.setOnClickListener {
            startActivity<ModifyTableActivity>("table" to GroupTable.NAME,
                    "adminLevel" to adminLevel)
        }

        sports_button.setOnClickListener {
            startActivity<ModifyTableActivity>("table" to SportTable.NAME,
                    "adminLevel" to adminLevel)
        }

        fees_button.setOnClickListener {
            startActivity<ModifyTableActivity>("table" to FeeTable.NAME,
                    "adminLevel" to adminLevel)
        }

        activity_types_button.setOnClickListener {
            startActivity<ModifyTableActivity>("table" to ActivityTypeTable.NAME,
                    "adminLevel" to adminLevel)
        }
    }
}
