package com.example.studymate.viewmodels

import android.os.CountDownTimer
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.studymate.model.Flashcard
import com.example.studymate.model.QuizQuestion
import com.example.studymate.model.QuestionType
import com.example.studymate.repositories.FlashcardsRepository
import com.example.studymate.model.CategoryStats
import android.content.Context
import com.google.gson.Gson
import java.lang.ref.WeakReference
import com.google.firebase.auth.FirebaseAuth

class QuizViewModel : ViewModel() {
    private val repository = FlashcardsRepository()
    private var contextRef: WeakReference<Context>? = null

    private val _currentQuestion = MutableLiveData<QuizQuestion>()
    val currentQuestion: LiveData<QuizQuestion> = _currentQuestion

    private val _score = MutableLiveData(0)
    val score: LiveData<Int> = _score

    private val _isQuizFinished = MutableLiveData<Boolean>(false)
    val isQuizFinished: LiveData<Boolean> = _isQuizFinished

    private val _questionNumber = MutableLiveData(0)
    val questionNumber: LiveData<Int> = _questionNumber

    private val _notEnoughCards = MutableLiveData<Boolean>()
    val notEnoughCards: LiveData<Boolean> = _notEnoughCards

    private val _timeRemaining = MutableLiveData<Int>()
    val timeRemaining: LiveData<Int> = _timeRemaining

    private var timer: CountDownTimer? = null

    private var flashcards = listOf<Flashcard>()
    private var currentQuestions = mutableListOf<QuizQuestion>()
    private var currentCategory: String? = null

    fun setContext(context: Context) {
        contextRef = WeakReference(context)
    }

    fun startNewQuiz(category: String?) {
        currentCategory = category
        _questionNumber.value = 0
        _score.value = 0
        _isQuizFinished.value = false
        loadFlashcardsByCategory(category)
    }

    private fun startTimer() {
        timer?.cancel()

        timer = object : CountDownTimer(10000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                _timeRemaining.value = (millisUntilFinished / 1000).toInt()
            }

            override fun onFinish() {

                checkAnswer("")
            }
        }.start()
    }

    private fun stopTimer() {
        timer?.cancel()
        timer = null
    }

    fun getCurrentCategory(): String {
        return currentCategory ?: "All Categories"
    }

    fun saveQuizResult(category: String, score: Int) {
        val context = contextRef?.get() ?: return
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        val prefs = context.getSharedPreferences(
            "quiz_stats_$userId",
            Context.MODE_PRIVATE
        )
        val gson = Gson()

        val categoryStats = prefs.getString(category, null)?.let {
            try {
                gson.fromJson(it, CategoryStats::class.java)
            } catch (e: Exception) {
                null
            }
        } ?: CategoryStats(category, 0, 0f, 0)

        val newStats = categoryStats.copy(
            quizzesTaken = categoryStats.quizzesTaken + 1,
            averageScore = (categoryStats.averageScore * categoryStats.quizzesTaken + score) / (categoryStats.quizzesTaken + 1),
            bestScore = maxOf(categoryStats.bestScore, score)
        )

        prefs.edit().putString(category, gson.toJson(newStats)).apply()
    }

    fun loadFlashcardsByCategory(category: String?) {
        repository.getFlashcards(
            onSuccess = { allFlashcards ->
                val filteredCards = if (category != null) {
                    allFlashcards.filter { it.category == category }
                } else {
                    allFlashcards
                }

                if (filteredCards.size < 10) {
                    _notEnoughCards.postValue(true)
                    return@getFlashcards
                }

                flashcards = filteredCards
                generateQuiz()
                _notEnoughCards.postValue(false)
            },
            onFailure = { /* Handle error */ }
        )
    }

    fun loadCategories(onCategoriesLoaded: (List<String>) -> Unit) {
        repository.getFlashcards(
            onSuccess = { flashcards ->
                val categories = flashcards.map { it.category }.distinct().sorted()
                onCategoriesLoaded(categories)
            },
            onFailure = { /* Handle error */ }
        )
    }

    private fun generateQuiz() {
        if (flashcards.size < 4) return

        currentQuestions.clear()
        val selectedFlashcards = flashcards.shuffled().take(10)

        selectedFlashcards.forEach { flashcard ->

            when ((0..2).random()) {
                0 -> generateMultipleChoice(flashcard)
                1 -> generateTrueFalse(flashcard)
                2 -> generateWrittenQuestion(flashcard)
            }
        }

        nextQuestion()
    }

    private fun generateMultipleChoice(flashcard: Flashcard) {
        val wrongAnswers = flashcards
            .filter { it.id != flashcard.id }
            .map { it.answer }
            .shuffled()
            .take(3)

        val options = (wrongAnswers + flashcard.answer).shuffled()

        currentQuestions.add(
            QuizQuestion(
                question = flashcard.question,
                correctAnswer = flashcard.answer,
                options = options,
                questionType = QuestionType.MULTIPLE_CHOICE
            )
        )
    }

    private fun generateTrueFalse(flashcard: Flashcard) {
        val isTrue = (0..1).random() == 1
        val displayedAnswer = if (isTrue) {
            flashcard.answer
        } else {
            flashcards.filter { it.id != flashcard.id }.random().answer
        }

        currentQuestions.add(
            QuizQuestion(
                question = "${flashcard.question}\nAnswer: $displayedAnswer",
                correctAnswer = isTrue.toString(),
                options = listOf("true", "false"),
                questionType = QuestionType.TRUE_FALSE
            )
        )
    }

    private fun generateWrittenQuestion(flashcard: Flashcard) {
        currentQuestions.add(
            QuizQuestion(
                question = flashcard.question,
                correctAnswer = flashcard.answer,
                options = emptyList(),
                questionType = QuestionType.WRITTEN
            )
        )
    }

    fun checkAnswer(answer: String) {
        stopTimer()

        val currentQ = currentQuestion.value ?: return

        if (answer.equals(currentQ.correctAnswer, ignoreCase = true)) {
            _score.value = (_score.value ?: 0) + 1
        }

        nextQuestion()
    }


    private fun nextQuestion() {
        if ((_questionNumber.value ?: 0) >= currentQuestions.size) {
            stopTimer()
            _isQuizFinished.value = true
            return
        }

        val nextQuestionNumber = (_questionNumber.value ?: 0) + 1
        _questionNumber.value = nextQuestionNumber
        _currentQuestion.value = currentQuestions[nextQuestionNumber - 1]
        startTimer()
    }

    override fun onCleared() {
        super.onCleared()
        stopTimer()
    }

    fun getFinalScore(): Int {
        return _score.value ?: 0
    }

}