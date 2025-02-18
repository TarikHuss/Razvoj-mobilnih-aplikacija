package com.example.studymate

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.studymate.R
import com.example.studymate.model.CategoryStats

class CategoryStatsAdapter(private val stats: List<CategoryStats>) :
    RecyclerView.Adapter<CategoryStatsAdapter.StatsViewHolder>() {

    class StatsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val categoryText: TextView = itemView.findViewById(R.id.categoryText)
        private val statsText: TextView = itemView.findViewById(R.id.statsText)
        private val progressBar: ProgressBar = itemView.findViewById(R.id.progressBar)

        fun bind(stats: CategoryStats) {
            categoryText.text = stats.category
            if (stats.quizzesTaken > 0) {
                statsText.text = "Quizzes: ${stats.quizzesTaken} | Avg: ${String.format("%.1f", stats.averageScore)}% | Best: ${stats.bestScore}%"
                progressBar.progress = stats.averageScore.toInt()
                progressBar.visibility = View.VISIBLE
            } else {
                statsText.text = "No quizzes taken yet"
                progressBar.visibility = View.GONE
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StatsViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_category_stats, parent, false)
        return StatsViewHolder(view)
    }

    override fun onBindViewHolder(holder: StatsViewHolder, position: Int) {
        holder.bind(stats[position])
    }

    override fun getItemCount() = stats.size
}