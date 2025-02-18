package com.example.studymate

import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import org.json.JSONArray
import org.json.JSONException
import java.util.Calendar
import com.example.studymate.model.CategoryStats
import com.google.gson.Gson
import androidx.lifecycle.ViewModelProvider
import android.view.ViewGroup
import com.example.studymate.viewmodels.FlashcardsViewModel
import android.widget.LinearLayout
import android.view.View
import com.google.firebase.auth.FirebaseAuth
import com.example.studymate.model.Reminder
import com.example.studymate.repositories.ReminderRepository
import com.google.android.material.bottomnavigation.BottomNavigationView

class HomeActivity : AppCompatActivity() {

    private lateinit var welcomeMessage: TextView
    private lateinit var bottomNavigation: BottomNavigationView
    private lateinit var categoryStatsRecyclerView: RecyclerView
    private lateinit var remindersList: RecyclerView
    private lateinit var addReminderButton: FloatingActionButton
    private lateinit var reminderRepository: ReminderRepository
    private var reminders = mutableListOf<Reminder>()

    private val sharedPrefs by lazy { getSharedPreferences("StudyMatePrefs", Context.MODE_PRIVATE) }
    private lateinit var reminderAdapter: ReminderAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        reminderRepository = ReminderRepository()

        val user = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
        if (user == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        setContentView(R.layout.activity_home)

        welcomeMessage = findViewById(R.id.welcomeMessage)
        bottomNavigation = findViewById(R.id.bottomNavigation)
        remindersList = findViewById(R.id.remindersList)
        addReminderButton = findViewById(R.id.addReminderButton)
        categoryStatsRecyclerView = findViewById(R.id.categoryStatsRecyclerView)
        categoryStatsRecyclerView.layoutManager = LinearLayoutManager(this)

        // Postavi personalizovanu poruku dobrodošlice

        welcomeMessage.text = "Welcome!"

        loadQuizStatistics()

        // Postavi adapter za podsjetnike
        reminderAdapter = ReminderAdapter(reminders) { reminder ->
            removeReminder(reminder)
        }
        remindersList.layoutManager = LinearLayoutManager(this)
        remindersList.adapter = reminderAdapter
        remindersList.itemAnimator = DefaultItemAnimator() // Animacija za dodavanje i brisanje

        // Omogućavanje swipe-to-delete
        setupSwipeToDelete()

        // Dugme za dodavanje podsjetnika
        addReminderButton.setOnClickListener {
            showAddReminderDialog()
        }

        val bottomNavigation = findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(R.id.bottomNavigation)
        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> true  // Već smo u HomeActivity, ne radimo ništa
                R.id.nav_flashcards -> {
                    startActivity(Intent(this@HomeActivity, FlashcardsActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_quizzes -> {
                    startActivity(Intent(this@HomeActivity, QuizActivity::class.java))
                    finish()
                    true
                }
                else -> false
            }
        }

        setupReminders()

    }

    private fun setupReminders() {
        reminderRepository.getReminders(
            onSuccess = { remindersList ->
                reminders.clear()
                reminders.addAll(remindersList)
                reminderAdapter.updateReminders(reminders)
            },
            onFailure = { exception ->
                Toast.makeText(this, "Error loading reminders: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
        )
    }

    private fun loadQuizStatistics() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val prefs = getSharedPreferences("quiz_stats_$userId", Context.MODE_PRIVATE)
        val gson = Gson()

        val statsViewModel = ViewModelProvider(this).get(FlashcardsViewModel::class.java)
        statsViewModel.loadCategories { categories ->
            val stats = categories.mapNotNull { category ->
                prefs.getString(category, null)?.let {
                    try {
                        gson.fromJson(it, CategoryStats::class.java)
                    } catch (e: Exception) {
                        CategoryStats(category, 0, 0f, 0)
                    }
                } ?: CategoryStats(category, 0, 0f, 0)
            }

            if (stats.isNotEmpty()) {
                categoryStatsRecyclerView.adapter = CategoryStatsAdapter(stats)
            } else {
                showNoStatsMessage()
            }
        }
    }

    private fun showNoStatsMessage() {
        val statsContainer = findViewById<LinearLayout>(R.id.statsContainer)  // Promijeni ViewGroup u LinearLayout

        val messageView = TextView(this).apply {
            text = "Complete some quizzes to see statistics"
            textAlignment = View.TEXT_ALIGNMENT_CENTER
            setPadding(16, 16, 16, 16)
            textSize = 16f
            setTextColor(getColor(R.color.purple_500))  // ili neka druga boja koja odgovara tvom dizajnu
        }

        statsContainer.removeAllViews()
        statsContainer.addView(messageView)
    }

    private fun showAddReminderDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_task, null)
        val editTextReminder = dialogView.findViewById<EditText>(R.id.editTextTask)
        val buttonSetDeadline = dialogView.findViewById<Button>(R.id.buttonSetDeadline)
        val buttonAdd = dialogView.findViewById<Button>(R.id.buttonAddTask)
        val selectedDate = arrayOf("No deadline")

        buttonSetDeadline.setOnClickListener {
            val calendar = Calendar.getInstance()
            val datePicker = DatePickerDialog(
                this,
                { _, year, month, day ->
                    selectedDate[0] = "$day/${month + 1}/$year"
                    buttonSetDeadline.text = selectedDate[0]
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )
            datePicker.show()
        }

        val alertDialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setTitle("Add New Reminder")
            .create()

        buttonAdd.setOnClickListener {
            val reminderText = editTextReminder.text.toString().trim()
            if (reminderText.isEmpty()) {
                Toast.makeText(this, "Reminder cannot be empty!", Toast.LENGTH_SHORT).show()
            } else {
                addReminder(reminderText, selectedDate[0]) // Dodavanje sa rokom
                alertDialog.dismiss()
            }
        }

        alertDialog.show()
    }

    override fun onResume() {
        super.onResume()
        findViewById<BottomNavigationView>(R.id.bottomNavigation).selectedItemId = R.id.nav_home
        setupReminders()
        loadQuizStatistics()
    }

    private fun setupSwipeToDelete() {
        val itemTouchHelperCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val removedReminder = reminders[position]
                removeReminder(removedReminder)
                Toast.makeText(this@HomeActivity, "Reminder deleted", Toast.LENGTH_SHORT).show()
            }
        }
        ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(remindersList)
    }

    private fun addReminder(title: String, dueDate: String) {
        reminderRepository.addReminder(
            title,
            dueDate,
            onSuccess = {
                setupReminders()  // Osvježi listu nakon dodavanja
                Toast.makeText(this, "Reminder added", Toast.LENGTH_SHORT).show()
            },
            onFailure = { exception ->
                Toast.makeText(this, "Error adding reminder: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
        )
    }

    private fun removeReminder(reminder: Reminder) {
        reminderRepository.deleteReminder(
            reminder.id,
            onSuccess = {
                Toast.makeText(this, "Reminder deleted", Toast.LENGTH_SHORT).show()
            },
            onFailure = { exception ->
                Toast.makeText(this, "Error deleting reminder: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
        )
    }
}
