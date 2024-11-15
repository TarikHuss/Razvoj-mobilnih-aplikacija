package com.example.colors

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var colorView: View
    private lateinit var redSeekBar: SeekBar
    private lateinit var greenSeekBar: SeekBar
    private lateinit var blueSeekBar: SeekBar
    private lateinit var redValueText: TextView
    private lateinit var greenValueText: TextView
    private lateinit var blueValueText: TextView
    private lateinit var hexColorText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        colorView = findViewById(R.id.colorView)
        redSeekBar = findViewById(R.id.redSeekBar)
        greenSeekBar = findViewById(R.id.greenSeekBar)
        blueSeekBar = findViewById(R.id.blueSeekBar)
        redValueText = findViewById(R.id.redValueText)
        greenValueText = findViewById(R.id.greenValueText)
        blueValueText = findViewById(R.id.blueValueText)
        hexColorText = findViewById(R.id.hexColorText)

        val seekBarChangeListener = object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                updateColor()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        }

        redSeekBar.setOnSeekBarChangeListener(seekBarChangeListener)
        greenSeekBar.setOnSeekBarChangeListener(seekBarChangeListener)
        blueSeekBar.setOnSeekBarChangeListener(seekBarChangeListener)
    }

    private fun updateColor() {
        val red = redSeekBar.progress
        val green = greenSeekBar.progress
        val blue = blueSeekBar.progress

        colorView.setBackgroundColor(Color.rgb(red, green, blue))

        redValueText.text = "Crvena: $red"
        greenValueText.text = "Zelena: $green"
        blueValueText.text = "Plava: $blue"

        val hexColor = String.format("#%02X%02X%02X", red, green, blue)
        hexColorText.text = "Izabrana boja je: $hexColor"
    }
}
