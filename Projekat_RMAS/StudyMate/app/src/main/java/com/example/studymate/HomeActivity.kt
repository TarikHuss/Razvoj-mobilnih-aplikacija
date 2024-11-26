package com.example.studymate

import android.annotation.SuppressLint
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView

class HomeActivity : AppCompatActivity() {

    private lateinit var welcomeMessage: TextView
    private lateinit var achievementSummary: TextView
    private lateinit var progressSummary: TextView
    private lateinit var remindersList: RecyclerView
    private lateinit var createFlashcardsButton: Button
    private lateinit var startQuizButton: Button
    private lateinit var startPomodoroButton: Button

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        val sharedPreferences: SharedPreferences = getSharedPreferences("userPrefs", MODE_PRIVATE)
        val isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false)

        if (!isLoggedIn) {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }

        welcomeMessage = findViewById(R.id.welcomeMessage)
        achievementSummary = findViewById(R.id.achievementSummary)
        progressSummary = findViewById(R.id.progressSummary)
        remindersList = findViewById(R.id.remindersList)
        createFlashcardsButton = findViewById(R.id.createFlashcardsButton)
        startQuizButton = findViewById(R.id.startQuizButton)
        startPomodoroButton = findViewById(R.id.startPomodoroButton)

        val email = sharedPreferences.getString("email", "Guest")
        welcomeMessage.text = "Welcome, $email!"

        val achievements = "Achievements: 50 Flashcards | 10 Quizzes"
        achievementSummary.text = achievements
        progressSummary.text = "75% Progress"

        remindersList.layoutManager = LinearLayoutManager(this)
        val reminders = listOf(
            "Review Math Flashcards" to "2 hours",
            "Complete Physics Quiz" to "3 hours",
            "Practice Chemistry" to "1 day"
        )
        remindersList.adapter = ReminderAdapter(reminders)

        createFlashcardsButton.setOnClickListener {
            startActivity(Intent(this, FlashcardsActivity::class.java))
        }

        startQuizButton.setOnClickListener {
            startActivity(Intent(this, QuizActivity::class.java))
        }

        startPomodoroButton.setOnClickListener {
            startActivity(Intent(this, PomodoroActivity::class.java))
        }

        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        bottomNavigation.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_profile -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                    true
                }
                R.id.nav_flashcards -> {
                    startActivity(Intent(this, FlashcardsActivity::class.java))
                    true
                }
                R.id.nav_quizzes -> {
                    startActivity(Intent(this, QuizActivity::class.java))
                    true
                }
                R.id.nav_settings -> {
                    startActivity(Intent(this, SettingsActivity::class.java))
                    true
                }
                else -> false
            }
        }
    }
}
