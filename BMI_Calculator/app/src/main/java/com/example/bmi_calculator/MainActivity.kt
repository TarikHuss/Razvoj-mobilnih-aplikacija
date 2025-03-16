package com.example.bmi_calculator

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.RadioGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var ageInput: EditText
    private lateinit var radioGroupGender: RadioGroup
    private lateinit var heightInput: EditText
    private lateinit var weightInput: EditText
    private lateinit var calculateButton: Button
    private lateinit var resetButton: Button
    private lateinit var resultDisplay: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        ageInput = findViewById(R.id.getAge)
        radioGroupGender = findViewById(R.id.radioGroupGender)
        heightInput = findViewById(R.id.getHeight)
        weightInput = findViewById(R.id.getWeight)
        calculateButton = findViewById(R.id.btnCalculate)
        resetButton = findViewById(R.id.btnReset)
        resultDisplay = findViewById(R.id.tvResult)

        calculateButton.setOnClickListener {
            performBMICalculation()
        }

        resetButton.setOnClickListener {
            clearFields()
        }
    }

    private fun performBMICalculation() {
        val heightStr = heightInput.text.toString()
        val weightStr = weightInput.text.toString()

        if (heightStr.isNotEmpty() && weightStr.isNotEmpty()) {
            val heightInMeters = heightStr.toFloat() / 100
            val weight = weightStr.toFloat()
            val bmiValue = weight / (heightInMeters * heightInMeters)

            val selectedGenderId = radioGroupGender.checkedRadioButtonId
            val gender = when (selectedGenderId) {
                R.id.radioMale -> "Muško"
                R.id.radioFemale -> "Žensko"
                else -> "Nepoznato"
            }

            val bmiCategory = when {
                bmiValue < 18.5 -> "Premalo težine"
                bmiValue < 24.9 -> "Zdravi raspon"
                bmiValue < 29.9 -> "Prekomjerna težina"
                bmiValue < 39.9 -> "Gojaznost"
                else -> "Teška gojaznost"
            }

            resultDisplay.text = "Tvoj BMI: %.2f\nKategorija: %s\nPol: %s".format(bmiValue, bmiCategory, gender)
        }
    }

    private fun clearFields() {
        ageInput.text.clear()
        heightInput.text.clear()
        weightInput.text.clear()
        radioGroupGender.clearCheck()
        resultDisplay.text = "Tvoj BMI: "
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("bmi_result", resultDisplay.text.toString())
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        val savedResult = savedInstanceState.getString("bmi_result")
        if (savedResult != null) {
            resultDisplay.text = savedResult
        }
    }
}
