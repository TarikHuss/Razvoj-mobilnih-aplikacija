package com.example.studymate

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ReminderAdapter(private val reminders: List<Pair<String, String>>) :
    RecyclerView.Adapter<ReminderAdapter.ReminderViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReminderViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_reminder, parent, false)
        return ReminderViewHolder(view)
    }

    override fun onBindViewHolder(holder: ReminderViewHolder, position: Int) {
        val (text, deadline) = reminders[position]
        holder.bind(text, deadline)
    }

    override fun getItemCount(): Int = reminders.size

    class ReminderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val reminderText: TextView = itemView.findViewById(R.id.reminderText)
        private val reminderDeadline: TextView = itemView.findViewById(R.id.reminderDeadline)

        fun bind(text: String, deadline: String) {
            reminderText.text = text
            reminderDeadline.text = "Due: $deadline"
        }
    }
}
