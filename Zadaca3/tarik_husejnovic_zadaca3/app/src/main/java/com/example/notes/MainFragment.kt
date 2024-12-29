package com.example.notes

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainFragment() : Fragment(){

    private lateinit var recyclerView: RecyclerView
    private lateinit var noteAdapter: NoteAdapter
    private val notes = mutableListOf<Note>()

    fun deleteNote(note: Note) {
        val index = notes.indexOfFirst { it.id == note.id }
        if (index != -1) {
            notes.removeAt(index)
            noteAdapter.notifyItemRemoved(index)
        }
    }


    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_main, container, false)

        recyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(context)
        noteAdapter = NoteAdapter(
            notes,
            onNoteClick = { note ->
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragmentContainer, EditFragment.newInstance(note))
                    .addToBackStack(null)
                    .commit()
            },

            onNoteDelete = { note ->
                deleteNote(note)
            }
        )

        recyclerView.adapter = noteAdapter


        view.findViewById<FloatingActionButton>(R.id.addNoteButton).setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, EditFragment.newInstance(null))
                .addToBackStack(null)
                .commit()
        }
        return view
    }

    fun saveNote(note: Note, isEdit: Boolean) {
        if (isEdit) {
            val index = notes.indexOfFirst { it.id == note.id }
            if (index != -1) {
                notes[index] = note
                noteAdapter.notifyItemChanged(index)
            }
        } else {
            notes.add(0, note)
            noteAdapter.notifyItemInserted(0)
        }
    }


    fun updateNote(note: Note) {
        val index = notes.indexOfFirst { it.date == note.date }
        if (index != -1) {
            notes[index] = note
            noteAdapter.notifyItemChanged(index)
        }
    }

}
