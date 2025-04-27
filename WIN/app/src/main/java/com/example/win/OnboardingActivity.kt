package com.example.win

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class OnboardingActivity : AppCompatActivity() {

    private lateinit var nameEditText: EditText
    private lateinit var ageEditText: EditText
    private lateinit var genderRadioGroup: RadioGroup
    private lateinit var continueButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboarding)

        nameEditText = findViewById(R.id.editTextName)
        ageEditText = findViewById(R.id.editTextAge)
        genderRadioGroup = findViewById(R.id.radioGroupGender)
        continueButton = findViewById(R.id.buttonContinue)

        continueButton.setOnClickListener {
            val name = nameEditText.text.toString().trim()
            val age = ageEditText.text.toString().trim()
            val genderId = genderRadioGroup.checkedRadioButtonId

            if (name.isEmpty() || age.isEmpty() || genderId == -1) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Save user info to shared preferences or pass to next activity
            val intent = Intent(this, ChatActivity::class.java)
            intent.putExtra("name", name)
            intent.putExtra("age", age)
            intent.putExtra("gender", when (genderId) {
                R.id.radioMale -> "Male"
                R.id.radioFemale -> "Female"
                else -> "Other"
            })
            startActivity(intent)
            finish()
        }
    }
}
