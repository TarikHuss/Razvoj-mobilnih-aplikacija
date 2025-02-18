package com.example.studymate.repositories

import com.example.studymate.model.Flashcard
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.FirebaseAuth

class FlashcardsRepository {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private fun getUserFlashcardsCollection() = db.collection("users")
        .document(auth.currentUser?.uid ?: "")
        .collection("flashcards")

    fun getFlashcards(onSuccess: (List<Flashcard>) -> Unit, onFailure: (Exception) -> Unit) {
        getUserFlashcardsCollection()
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    onFailure(e)
                    return@addSnapshotListener
                }

                val flashcards = snapshot?.documents?.mapNotNull {
                    it.toObject(Flashcard::class.java)
                } ?: emptyList()
                onSuccess(flashcards)
            }
    }

    fun addFlashcard(
        question: String,
        answer: String,
        category: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val flashcardId = getUserFlashcardsCollection().document().id
        val flashcard = Flashcard(flashcardId, question, answer, category)

        getUserFlashcardsCollection().document(flashcardId)
            .set(flashcard)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it) }
    }

    fun updateFlashcard(
        flashcard: Flashcard,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        getUserFlashcardsCollection().document(flashcard.id)
            .set(flashcard)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it) }
    }

    fun deleteFlashcard(
        flashcardId: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        getUserFlashcardsCollection().document(flashcardId)
            .delete()
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it) }
    }
}