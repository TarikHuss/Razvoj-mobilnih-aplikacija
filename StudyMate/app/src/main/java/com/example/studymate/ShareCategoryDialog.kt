package com.example.studymate

import android.app.Dialog
import android.os.Bundle
import android.widget.EditText
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.studymate.model.Flashcard
import com.example.studymate.model.User
import com.example.studymate.R
import com.google.firebase.firestore.FirebaseFirestore
import android.view.LayoutInflater
import com.google.android.material.button.MaterialButton
import android.util.Log
import com.google.firebase.auth.FirebaseAuth

class ShareCategoryDialog : DialogFragment() {
    private lateinit var searchInput: EditText
    private lateinit var usersList: RecyclerView
    private lateinit var usersAdapter: UsersAdapter
    private lateinit var category: String
    private lateinit var flashcards: List<Flashcard>
    private lateinit var searchButton: MaterialButton // Dodajemo dugme za pretragu

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = layoutInflater.inflate(R.layout.dialog_share_category, null)

        searchInput = view.findViewById(R.id.searchInput)
        usersList = view.findViewById(R.id.usersList)
        searchButton = view.findViewById(R.id.searchButton) // Dodajemo referencu na dugme

        usersAdapter = UsersAdapter { user ->
            shareWithUser(user.id)
        }

        usersList.layoutManager = LinearLayoutManager(context)
        usersList.adapter = usersAdapter

        // Dodajemo onClick listener za dugme pretrage
        searchButton.setOnClickListener {
            val email = searchInput.text.toString().trim()
            if (email.isNotEmpty()) {
                searchUsers(email)
            }
        }

        return AlertDialog.Builder(requireContext())
            .setTitle("Share Category")
            .setView(view)
            .setNegativeButton("Cancel") { _, _ -> dismiss() }
            .create()
    }

    private fun searchUsers(email: String) {
        val db = FirebaseFirestore.getInstance()
        db.collection("users")
            .whereEqualTo("email", email)
            .get()
            .addOnSuccessListener { documents ->
                Log.d("ShareCategory", "Search results: ${documents.size()} documents")
                if (documents.isEmpty) {
                    Toast.makeText(context, "No user found with that email", Toast.LENGTH_SHORT).show()
                } else {
                    val users = documents.mapNotNull {
                        try {
                            it.toObject(User::class.java)
                        } catch (e: Exception) {
                            Log.e("ShareCategory", "Error converting document: ${e.message}")
                            null
                        }
                    }
                    Log.d("ShareCategory", "Mapped users: ${users.size}")
                    usersAdapter.submitList(users)
                }
            }
            .addOnFailureListener { e ->
                Log.e("ShareCategory", "Error searching users: ${e.message}")
                Toast.makeText(context, "Error searching for users: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun shareWithUser(userId: String) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        val db = FirebaseFirestore.getInstance()

        // Prvo dohvati sve flashcards iz odabrane kategorije
        db.collection("users")
            .document(currentUser?.uid ?: "")
            .collection("flashcards")
            .whereEqualTo("category", category)  // category je kategorija koju dijelimo
            .get()
            .addOnSuccessListener { documents ->
                // Kreiraj batch za grupno dodavanje kartica
                val batch = db.batch()

                documents.forEach { document ->
                    val flashcard = document.toObject(Flashcard::class.java)
                    // Kreiraj novi dokument u kolekciji primatelja
                    val newDocRef = db.collection("users")
                        .document(userId)
                        .collection("flashcards")
                        .document()

                    // Kreiraj novu karticu sa novim ID-em ali istim sadržajem
                    val sharedFlashcard = Flashcard(
                        id = newDocRef.id,
                        question = flashcard.question,
                        answer = flashcard.answer,
                        category = flashcard.category
                    )

                    // Dodaj operaciju u batch
                    batch.set(newDocRef, sharedFlashcard)
                }

                // Izvrši sve operacije odjednom
                batch.commit()
                    .addOnSuccessListener {
                        Toast.makeText(context, "Category shared successfully!", Toast.LENGTH_SHORT).show()
                        dismiss()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(context, "Error sharing category: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Error getting flashcards: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    companion object {
        fun newInstance(category: String, flashcards: List<Flashcard>): ShareCategoryDialog {
            return ShareCategoryDialog().apply {
                this.category = category
                this.flashcards = flashcards
            }
        }
    }
}