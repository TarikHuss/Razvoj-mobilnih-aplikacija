package com.example.studymate

import android.os.Bundle
import android.util.Log
import android.content.Intent
import android.view.View
import android.view.LayoutInflater
import android.widget.Button
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.Spinner
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.studymate.model.Flashcard
import com.example.studymate.viewmodels.FlashcardsViewModel
import android.widget.Toast
import android.view.MenuItem
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.bottomnavigation.BottomNavigationView

class FlashcardsActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var bottomNavigation: BottomNavigationView
    private lateinit var editCategory: TextInputEditText
    private lateinit var adapter: FlashcardsAdapter
    private lateinit var editQuestion: TextInputEditText
    private lateinit var editAnswer: TextInputEditText
    private lateinit var btnAddFlashcard: Button
    private lateinit var categorySpinner: Spinner
    private lateinit var newCategoryLayout: LinearLayout
    private var categories = mutableSetOf<String>()

    private val viewModel: FlashcardsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_flashcards)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        bottomNavigation = findViewById(R.id.bottomNavigation)

        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, HomeActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_flashcards -> true // Već smo ovdje
                R.id.nav_quizzes -> {
                    startActivity(Intent(this, QuizActivity::class.java))
                    finish()
                    true
                }
                else -> false
            }
        }

        initializeViews()
        setupRecyclerView()
        setupObservers()
        setupListeners()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun initializeViews() {
        editCategory = findViewById(R.id.editCategory)
        editQuestion = findViewById(R.id.editQuestion)
        editAnswer = findViewById(R.id.editAnswer)
        btnAddFlashcard = findViewById(R.id.btnAddFlashcard)
        recyclerView = findViewById(R.id.recyclerViewFlashcards)
        categorySpinner = findViewById(R.id.categorySpinner)
        newCategoryLayout = findViewById(R.id.newCategoryLayout)
        editCategory = findViewById(R.id.editCategory)

        setupCategorySelection()
    }

    private fun setupRecyclerView() {
        adapter = FlashcardsAdapter(
            emptyList(),
            onDeleteClick = { flashcardId -> viewModel.deleteFlashcard(flashcardId) },
            onEditClick = { flashcard -> showEditDialog(flashcard) },
            onShareCategory = { category -> showShareDialog(category) }
        )
        recyclerView.apply {
            layoutManager = LinearLayoutManager(this@FlashcardsActivity)
            adapter = this@FlashcardsActivity.adapter
        }
    }

    private fun setupObservers() {
        viewModel.flashcards.observe(this) { flashcards ->
            adapter.updateData(flashcards)
        }

        viewModel.error.observe(this) { errorMessage ->
            Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupListeners() {
        btnAddFlashcard.setOnClickListener {
            val question = editQuestion.text.toString().trim()
            val answer = editAnswer.text.toString().trim()
            val category = editCategory.text.toString().trim()

            if (question.isEmpty() || answer.isEmpty() || category.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            viewModel.addFlashcard(question, answer, category)
            clearInputFields()
        }
    }

    private fun setupCategorySelection() {
        viewModel.loadCategories { loadedCategories ->
            categories.clear()
            categories.addAll(loadedCategories)
            categories.add("+ Add New Category")

            ArrayAdapter(
                this,
                android.R.layout.simple_spinner_item,
                categories.toList()
            ).also { adapter ->
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                categorySpinner.adapter = adapter
            }

            categorySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    val selected = parent?.getItemAtPosition(position).toString()
                    if (selected == "+ Add New Category") {
                        newCategoryLayout.visibility = View.VISIBLE
                        editCategory.text?.clear()
                    } else {
                        newCategoryLayout.visibility = View.GONE
                        editCategory.setText(selected)
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                    newCategoryLayout.visibility = View.GONE
                }
            }
        }
    }

    private fun showEditDialog(flashcard: Flashcard) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_edit_flashcard, null)

        val editQuestion = dialogView.findViewById<TextInputEditText>(R.id.editQuestion)
        val editAnswer = dialogView.findViewById<TextInputEditText>(R.id.editAnswer)
        val editCategory = dialogView.findViewById<TextInputEditText>(R.id.editCategory)

        editQuestion.setText(flashcard.question)
        editAnswer.setText(flashcard.answer)
        editCategory.setText(flashcard.category)

        AlertDialog.Builder(this)
            .setTitle("Edit Flashcard")
            .setView(dialogView)
            .setPositiveButton("Save") { dialog, _ ->
                val updatedFlashcard = Flashcard(
                    id = flashcard.id,
                    question = editQuestion.text.toString().trim(),
                    answer = editAnswer.text.toString().trim(),
                    category = editCategory.text.toString().trim()
                )
                viewModel.updateFlashcard(updatedFlashcard)
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.cancel()
            }
            .show()
    }

    private fun showShareDialog(category: String) {
        val flashcardsInCategory = adapter.getFlashcardsForCategory(category)
        ShareCategoryDialog.newInstance(category, flashcardsInCategory)
            .show(supportFragmentManager, "ShareDialog")
    }

    private fun clearInputFields() {
        editQuestion.text?.clear()
        editAnswer.text?.clear()
        editCategory.text?.clear()
        editQuestion.requestFocus()
    }

    override fun onResume() {
        super.onResume()
        bottomNavigation.selectedItemId = R.id.nav_flashcards
    }
}