package com.example.studymate.repositories

import com.example.studymate.model.Reminder
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.FirebaseAuth

class ReminderRepository {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private fun getUserRemindersCollection() = db.collection("users")
        .document(auth.currentUser?.uid ?: "")
        .collection("reminders")

    fun getReminders(onSuccess: (List<Reminder>) -> Unit, onFailure: (Exception) -> Unit) {
        getUserRemindersCollection()
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    onFailure(e)
                    return@addSnapshotListener
                }

                val reminders = snapshot?.documents?.mapNotNull {
                    it.toObject(Reminder::class.java)
                } ?: emptyList()
                onSuccess(reminders)
            }
    }

    fun addReminder(title: String, dueDate: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        val reminderId = getUserRemindersCollection().document().id
        val reminder = Reminder(reminderId, title, dueDate, auth.currentUser?.uid ?: "")

        getUserRemindersCollection()
            .document(reminderId)
            .set(reminder)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it) }
    }

    fun deleteReminder(reminderId: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        getUserRemindersCollection()
            .document(reminderId)
            .delete()
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it) }
    }
}