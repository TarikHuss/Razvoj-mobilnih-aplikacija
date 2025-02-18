package com.example.studymate

import android.os.Bundle
import android.view.View
import android.content.Intent
import com.google.android.material.bottomnavigation.BottomNavigationView
import android.view.LayoutInflater
import android.widget.*
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.studymate.model.QuestionType
import com.example.studymate.model.QuizQuestion
import com.example.studymate.viewmodels.QuizViewModel
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText


class QuizActivity : AppCompatActivity() {
    private val viewModel: QuizViewModel by viewModels()
    private lateinit var questionTextView: TextView
    private lateinit var bottomNavigation: BottomNavigationView
    private lateinit var multipleChoiceContainer: LinearLayout
    private lateinit var trueFalseContainer: LinearLayout
    private lateinit var writtenAnswerContainer: LinearLayout
    private lateinit var writtenAnswerInput: TextInputEditText
    private lateinit var submitButton: MaterialButton
    private lateinit var progressText: TextView
    private lateinit var scoreText: TextView
    private lateinit var timerText: TextView
    private lateinit var dialog: AlertDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_quiz)
        bottomNavigation = findViewById(R.id.bottomNavigation)

        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, HomeActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_flashcards -> {
                    startActivity(Intent(this, FlashcardsActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_quizzes -> true // Već smo ovdje
                else -> false
            }
        }

        viewModel.setContext(this)
        initializeViews()
        setupObservers()

        showCategorySelectionDialog()

        viewModel.notEnoughCards.observe(this) { notEnough ->
            if (notEnough) {
                Toast.makeText(this, "Need at least 10 cards in this category to start a quiz", Toast.LENGTH_LONG).show()
                startActivity(Intent(this, FlashcardsActivity::class.java))
                finish()
            }
        }
    }

    private fun initializeViews() {
        questionTextView = findViewById(R.id.questionText)
        multipleChoiceContainer = findViewById(R.id.multipleChoiceContainer)
        trueFalseContainer = findViewById(R.id.trueFalseContainer)
        writtenAnswerContainer = findViewById(R.id.writtenAnswerContainer)
        writtenAnswerInput = findViewById(R.id.writtenAnswerInput)
        submitButton = findViewById(R.id.submitButton)
        progressText = findViewById(R.id.progressText)
        scoreText = findViewById(R.id.scoreText)
        timerText = findViewById(R.id.timerText)
    }

    private fun showCategorySelectionDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_category_selection, null)
        val recyclerView = dialogView.findViewById<RecyclerView>(R.id.categoryRecyclerView)

        viewModel.loadCategories { categories ->
            val adapter = CategoryAdapter(categories) { selectedCategory ->
                viewModel.startNewQuiz(selectedCategory)
                dialog.dismiss()
            }

            recyclerView.layoutManager = LinearLayoutManager(this)
            recyclerView.adapter = adapter
        }

        dialog = AlertDialog.Builder(this)  // Koristi klasnu varijablu dialog umjesto lokalne
            .setView(dialogView)
            .setCancelable(false)
            .create()

        // Provjeri da li postoji dugme prije nego što postaviš listener
        dialogView.findViewById<MaterialButton>(R.id.allCategoriesButton)?.setOnClickListener {
            viewModel.startNewQuiz(null)
            dialog.dismiss()
        }

        dialog.show()
    }


    private fun setupObservers() {

        viewModel.notEnoughCards.observe(this) { notEnough ->
            if (notEnough) {
                Toast.makeText(
                    this,
                    "Need at least 10 cards in this category to start a quiz",
                    Toast.LENGTH_LONG
                ).show()
                finish()
            }
        }

        viewModel.currentQuestion.observe(this) { question ->
            question?.let { showQuestion(it) }
        }

        viewModel.questionNumber.observe(this) { number ->
            progressText.text = "Question $number/10"
        }

        viewModel.timeRemaining.observe(this) { timeLeft ->
            timerText.text = "Time: ${timeLeft}s"

            // Opciono: promijeni boju kada je malo vremena ostalo
            if (timeLeft <= 3) {
                timerText.setTextColor(getColor(R.color.red))
            } else {
                timerText.setTextColor(getColor(R.color.purple_500))
            }
        }

        viewModel.score.observe(this) { score ->
            scoreText.text = "Score: $score"
        }

        viewModel.isQuizFinished.observe(this) { isFinished ->
            if (isFinished) {
                showQuizCompleted()
            }
        }
    }

    private fun showQuizCompleted() {
        // Sakrij sve containere za pitanja
        multipleChoiceContainer.visibility = View.GONE
        trueFalseContainer.visibility = View.GONE
        writtenAnswerContainer.visibility = View.GONE

        // Prikaži rezultat
        questionTextView.text = "Quiz Completed!"
        val finalScore = viewModel.getFinalScore()
        val currentCategory = viewModel.getCurrentCategory() // Dobijamo trenutnu kategoriju

        // Sačuvaj rezultat kviza
        viewModel.saveQuizResult(currentCategory, finalScore * 10) // Množimo sa 10 da dobijemo postotak (0-100)

        val message = when {
            finalScore == 10 -> "Perfect Score! 🎉"
            finalScore >= 7 -> "Great Job! 👏"
            finalScore >= 5 -> "Good Effort! 👍"
            else -> "Keep Practicing! 💪"
        }

        questionTextView.apply {
            text = "Quiz Completed!\n$message"
            textAlignment = View.TEXT_ALIGNMENT_CENTER
            setTextColor(getColor(R.color.purple_500))
        }

        scoreText.text = "Final Score: $finalScore/10"

        // Dodaj dugme za povratak
        val returnButton = MaterialButton(this).apply {
            text = "Return to Home"
            setOnClickListener {
                val intent = Intent(this@QuizActivity, HomeActivity::class.java)
                startActivity(intent)

                finish()
            }
        }

        // Pronađi container gdje ćemo staviti dugme
        val container = findViewById<LinearLayout>(R.id.multipleChoiceContainer)
        container.visibility = View.VISIBLE
        container.removeAllViews()
        container.addView(returnButton)

        // Možemo dodati i Toast za dodatnu potvrdu
        Toast.makeText(
            this,
            "Quiz completed! You scored $finalScore out of 10",
            Toast.LENGTH_LONG
        ).show()
        val fadeIn = android.view.animation.AlphaAnimation(0f, 1f)
        fadeIn.duration = 1000

        questionTextView.startAnimation(fadeIn)
        scoreText.startAnimation(fadeIn)

    }

    private fun showQuestion(question: QuizQuestion) {
        questionTextView.text = question.question

        // Prvo sakrijemo sve kontejnere
        multipleChoiceContainer.visibility = View.GONE
        trueFalseContainer.visibility = View.GONE
        writtenAnswerContainer.visibility = View.GONE

        // Zatim prikažemo i postavimo odgovarajući kontejner prema tipu pitanja
        when (question.questionType) {
            QuestionType.MULTIPLE_CHOICE -> {
                multipleChoiceContainer.visibility = View.VISIBLE
                multipleChoiceContainer.removeAllViews()

                question.options.forEach { option ->
                    val button = MaterialButton(this).apply {
                        text = option
                        layoutParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                        ).apply {
                            setMargins(0, 0, 0, 16) // Dodajemo malo razmaka između dugmadi
                        }
                        setOnClickListener {
                            viewModel.checkAnswer(option)
                        }
                    }
                    multipleChoiceContainer.addView(button)
                }
            }
            QuestionType.TRUE_FALSE -> {
                trueFalseContainer.visibility = View.VISIBLE

                findViewById<MaterialButton>(R.id.trueButton).setOnClickListener {
                    viewModel.checkAnswer("true")
                }
                findViewById<MaterialButton>(R.id.falseButton).setOnClickListener {
                    viewModel.checkAnswer("false")
                }
            }
            QuestionType.WRITTEN -> {
                writtenAnswerContainer.visibility = View.VISIBLE
                writtenAnswerInput.text?.clear()

                submitButton.setOnClickListener {
                    val answer = writtenAnswerInput.text.toString()
                    if (answer.isNotEmpty()) {
                        viewModel.checkAnswer(answer)
                    } else {
                        Toast.makeText(this, "Please enter your answer", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun showMultipleChoice(question: QuizQuestion) {
        multipleChoiceContainer.visibility = View.VISIBLE
        multipleChoiceContainer.removeAllViews()

        question.options.forEach { option ->
            val button = MaterialButton(this).apply {
                text = option
                setOnClickListener {
                    viewModel.checkAnswer(option)
                }
            }
            multipleChoiceContainer.addView(button)
        }
    }

    private fun showTrueFalse(question: QuizQuestion) {
        trueFalseContainer.visibility = View.VISIBLE

        findViewById<MaterialButton>(R.id.trueButton).setOnClickListener {
            viewModel.checkAnswer("true")
        }
        findViewById<MaterialButton>(R.id.falseButton).setOnClickListener {
            viewModel.checkAnswer("false")
        }
    }

    private fun showWrittenAnswer() {
        writtenAnswerContainer.visibility = View.VISIBLE
        writtenAnswerInput.text?.clear()

        submitButton.setOnClickListener {
            val answer = writtenAnswerInput.text.toString()
            if (answer.isNotEmpty()) {
                viewModel.checkAnswer(answer)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        bottomNavigation.selectedItemId = R.id.nav_quizzes
    }


}