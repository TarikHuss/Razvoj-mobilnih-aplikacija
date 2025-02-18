package com.example.studymate

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.studymate.R
import com.example.studymate.model.Flashcard

class FlashcardsAdapter(
    private var flashcards: List<Flashcard>,
    private val onDeleteClick: (String) -> Unit,
    private val onEditClick: (Flashcard) -> Unit,
    private val onShareCategory: (String) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val HEADER_TYPE = 0
    private val FLASHCARD_TYPE = 1
    private var expandedCategories = mutableSetOf<String>()
    private var groupedFlashcards: List<Any> = groupFlashcards(flashcards)

    inner class CategoryHeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val categoryText: TextView = itemView.findViewById(R.id.categoryText)
        private val expandIcon: ImageView = itemView.findViewById(R.id.expandIcon)
        private val shareButton: ImageButton = itemView.findViewById(R.id.shareButton)

        fun bind(category: String) {
            categoryText.text = category
            val isExpanded = expandedCategories.contains(category)
            expandIcon.setImageResource(
                if (isExpanded) R.drawable.ic_expand_less
                else R.drawable.ic_expand_more
            )

            shareButton.setOnClickListener {
                onShareCategory(category)
            }

            itemView.setOnClickListener {
                if (isExpanded) {
                    expandedCategories.remove(category)
                } else {
                    expandedCategories.add(category)
                }
                groupedFlashcards = groupFlashcards(flashcards)
                notifyDataSetChanged()
            }
        }
    }

    inner class FlashcardViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val questionText: TextView = itemView.findViewById(R.id.textQuestion)
        private val answerText: TextView = itemView.findViewById(R.id.textAnswer)
        private val deleteButton: ImageButton = itemView.findViewById(R.id.btnDelete)
        private val editButton: ImageButton = itemView.findViewById(R.id.btnEdit)

        fun bind(flashcard: Flashcard) {
            questionText.text = flashcard.question
            answerText.text = flashcard.answer
            answerText.visibility = View.GONE

            itemView.setOnClickListener {
                answerText.visibility = if (answerText.visibility == View.VISIBLE)
                    View.GONE else View.VISIBLE
            }

            deleteButton.setOnClickListener {
                onDeleteClick(flashcard.id)
            }

            editButton.setOnClickListener {
                onEditClick(flashcard)
            }
        }
    }

    private fun groupFlashcards(cards: List<Flashcard>): List<Any> {
        val result = mutableListOf<Any>()
        val grouped = cards.groupBy { it.category }

        grouped.forEach { (category, categoryCards) ->
            result.add(category) // Header
            if (expandedCategories.contains(category)) {
                result.addAll(categoryCards) // Cards only if category is expanded
            }
        }
        return result
    }

    override fun getItemViewType(position: Int): Int {
        return if (groupedFlashcards[position] is String) HEADER_TYPE else FLASHCARD_TYPE
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            HEADER_TYPE -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_category_header, parent, false)
                CategoryHeaderViewHolder(view)
            }
            else -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_flashcard, parent, false)
                FlashcardViewHolder(view)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is CategoryHeaderViewHolder -> {
                val category = groupedFlashcards[position] as String
                holder.bind(category)
            }
            is FlashcardViewHolder -> {
                val flashcard = groupedFlashcards[position] as Flashcard
                holder.bind(flashcard)
            }
        }
    }

    fun getFlashcardsForCategory(category: String): List<Flashcard> {
        return flashcards.filter { it.category == category }
    }

    override fun getItemCount(): Int = groupedFlashcards.size

    fun updateData(newFlashcards: List<Flashcard>) {
        flashcards = newFlashcards
        groupedFlashcards = groupFlashcards(newFlashcards)
        notifyDataSetChanged()
    }
}