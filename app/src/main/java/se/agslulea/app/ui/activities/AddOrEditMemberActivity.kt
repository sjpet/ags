package se.agslulea.app.ui.activities

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.*
import org.jetbrains.anko.toast
import se.agslulea.app.*

import se.agslulea.app.data.db.AppDb
import se.agslulea.app.data.db.GroupTable
import se.agslulea.app.data.db.MemberTable

class AddOrEditMemberActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_or_edit_member)

        val db = AppDb()

        val memberId = intent.getIntExtra("memberId", -1)
        val adminLevel = intent.getIntExtra("adminLevel", 0)
        val preSelectedGroup = intent.getIntExtra("preSelectedGroup", 0)

        val firstNameText = findViewById(R.id.first_name_text) as EditText
        val familyNameText = findViewById(R.id.family_name_text) as EditText
        val personalIdText = findViewById(R.id.personal_id_text) as EditText
        val guardianText = findViewById(R.id.guardian_text) as EditText
        val emailText = findViewById(R.id.email_text) as EditText
        val phoneText = findViewById(R.id.phone_text) as EditText
        val checkBoxesLayout = findViewById(R.id.check_boxes_layout) as LinearLayout
        val adaCheckBox = findViewById(R.id.ada_box) as CheckBox
        val acceptButton = findViewById(R.id.accept_button) as Button

        // Set title according to action
        if (memberId < 0) {
            title = getString(R.string.add_member_title)
        } else {
            title = getString(R.string.edit_member_title)
            acceptButton.text = getString(R.string.save_changes)
        }

        // Populate group membership check boxes
        val groups = db.getGroupNames().drop(1)
        var groupCheckBoxes: Array<CheckBox> = arrayOf()
        for (group in groups) {
            val checkBox = CheckBox(this)
            val label = TextView(this)
            checkBox.isChecked = group[GroupTable.ID] == preSelectedGroup
            label.text = group[GroupTable.GROUP] as String
            groupCheckBoxes += checkBox
            val pairLayout = LinearLayout(this)
            pairLayout.addView(checkBox)
            pairLayout.addView(label)
            checkBoxesLayout.addView(pairLayout)
        }

        // Get current values if action is edit
        if (memberId >= 0) {

            val thisMember = db.getMemberDetails(memberId)
            firstNameText.setText(thisMember[MemberTable.FIRST_NAME] as String)
            familyNameText.setText(thisMember[MemberTable.FAMILY_NAME] as String)
            personalIdText.setText(thisMember[MemberTable.PERSONAL_ID] as String)
            guardianText.setText(thisMember[MemberTable.GUARDIAN] as String)
            emailText.setText(thisMember[MemberTable.EMAIL] as String)
            phoneText.setText(thisMember[MemberTable.PHONE] as String)
            adaCheckBox.isChecked = thisMember[MemberTable.SIGNED] as Boolean
            for (groupId in 1..groups.size) {
                groupCheckBoxes[groupId - 1].isChecked = db.memberInGroup(memberId, groupId)
            }

            // Lock personalId unless super admin
            if (adminLevel < 2) {
                val secretPersonalId =
                        (thisMember[MemberTable.PERSONAL_ID] as String).substring(0..8) + "####"
                personalIdText.setText(secretPersonalId)
                personalIdText.isFocusable = false
                personalIdText.isClickable = false
            }

            if (adminLevel > 0) {
                // Show fee payment checkboxes
            }
        }

        acceptButton.setOnClickListener {
            val firstName = capitalizeName(firstNameText.text.toString())
            val familyName = capitalizeName(familyNameText.text.toString())
            val personalId = formatPersonalId(personalIdText.text.toString())
            val guardian = capitalizeName(guardianText.text.toString())
            val email = emailText.text.toString()
            val phone = phoneText.text.toString()
            val signedAda = adaCheckBox.isChecked

            var allIsWell = true

            if (firstName == "") {
                toast(getString(R.string.first_name_missing))
                allIsWell = false
            }

            if (familyName == "") {
                toast(getString(R.string.family_name_missing))
                allIsWell = false
            }

            if ((memberId < 0 || adminLevel == 2) && !isValidPersonalId(personalId)) {
                toast(getString(R.string.invalid_personal_id))
                allIsWell = false
            }

            if (email != "" && !isValidEmail(email)) {
                toast(getString(R.string.invalid_email))
                allIsWell = false
            }

            if (!allIsWell) { finish() }

            if (memberId >= 0 || db.memberWithPersonalIdExists(personalId)) {
                // Update editable entries
                if (adminLevel < 2) {
                    db.updateMember(memberId, firstName, familyName, guardian, email, phone,
                            signedAda)
                } else {
                    db.superUpdateMember(memberId, firstName, familyName, personalId, guardian,
                            email, phone, signedAda)
                }
                for (group in 1..groups.size) {
                    if (groupCheckBoxes[group - 1].isChecked) {
                        db.addMemberToGroup(memberId, group)
                    } else {
                        db.removeMemberFromGroup(memberId, group)
                    }
                }
                finish()
            } else {
                val newId = db.addNewMember(firstName, familyName, personalId, guardian, email,
                        phone, signedAda)
                for (group in 1..groups.size) {
                    if (groupCheckBoxes[group - 1].isChecked) {
                        db.addMemberToGroup(newId, group)
                    } else {
                        db.removeMemberFromGroup(newId, group)
                    }
                }
                finish()
            }
        }
    }
}
