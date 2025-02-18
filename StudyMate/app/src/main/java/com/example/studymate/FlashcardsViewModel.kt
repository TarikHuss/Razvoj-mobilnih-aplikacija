package com.example.studymate.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.studymate.model.Flashcard
import com.example.studymate.repositories.FlashcardsRepository

class FlashcardsViewModel : ViewModel() {
    private val repository = FlashcardsRepository()

    private val _flashcards = MutableLiveData<List<Flashcard>>()
    val flashcards: LiveData<List<Flashcard>> = _flashcards

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    init {
        loadFlashcards()
    }

    fun loadCategories(onCategoriesLoaded: (List<String>) -> Unit) {
        repository.getFlashcards(
            onSuccess = { flashcards ->
                val categories = flashcards
                    .map { it.category }
                    .distinct()
                    .sorted()
                    .filter { it.isNotEmpty() }
                onCategoriesLoaded(categories)
            },
            onFailure = { /* Handle error */ }
        )
    }

    private fun loadFlashcards() {
        repository.getFlashcards(
            onSuccess = { list -> _flashcards.postValue(list) },
            onFailure = { e -> _error.postValue(e.message) }
        )
    }

    fun addFlashcard(question: String, answer: String, category: String) {
        repository.addFlashcard(
            question = question,
            answer = answer,
            category = category,
            onSuccess = { loadFlashcards() },
            onFailure = { e -> _error.postValue(e.message) }
        )
    }

    fun updateFlashcard(flashcard: Flashcard) {
        repository.updateFlashcard(
            flashcard,
            onSuccess = { /* Nije potrebno ručno osvježavanje */ },
            onFailure = { exception -> _error.postValue(exception.message) }
        )
    }

    fun deleteFlashcard(flashcardId: String) {
        repository.deleteFlashcard(
            flashcardId = flashcardId,
            onSuccess = { loadFlashcards() },
            onFailure = { e -> _error.postValue(e.message) }
        )
    }
}