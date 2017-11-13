package se.agslulea.app.ui.activities

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.toast

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

        if (adminLevel == 2) {
            val exportButton = findViewById(R.id.export_db_button) as Button
            val importButton = findViewById(R.id.import_db_button) as Button
            val db = AppDb()

            exportButton.visibility = View.VISIBLE
            importButton.visibility = View.VISIBLE

            exportButton.setOnClickListener {
                toast(when (db.export()) {
                    0 -> getString(R.string.exported)
                    else -> getString(R.string.export_failed)
                })
            }

            importButton.setOnClickListener {
                toast(when (db.import()) {
                    0 -> getString(R.string.imported)
                    1 -> getString(R.string.import_failed_no_file)
                    else -> getString(R.string.import_failed)
                })
            }

        }
    }
}
