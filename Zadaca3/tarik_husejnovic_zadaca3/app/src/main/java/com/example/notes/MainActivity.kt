package com.example.notes

import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity(), EditFragment.OnNoteSavedListener {

    private lateinit var mainFragment: MainFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (savedInstanceState == null) {
            mainFragment = MainFragment()
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, mainFragment)
                .commit()
        }
        val infoIcon = findViewById<ImageView>(R.id.infoIcon)
        infoIcon.setOnClickListener {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, AboutFragment())
                .addToBackStack(null)
                .commit()
        }
    }

    override fun onNoteSaved(note: Note, isEdit: Boolean) {
        mainFragment.saveNote(note, isEdit)
    }


}