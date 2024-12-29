package com.example.notes

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import android.widget.Button
import androidx.fragment.app.Fragment
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class EditFragment : Fragment() {

    private var listener: OnNoteSavedListener? = null
    private var note: Note? = null

    interface OnNoteSavedListener {
        fun onNoteSaved(note: Note, isEdit: Boolean)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnNoteSavedListener) {
            listener = context
        } else {
            throw RuntimeException("$context must implement OnNoteSavedListener")
        }
    }

    companion object {
        fun newInstance(note: Note?): EditFragment {
            val fragment = EditFragment()
            fragment.note = note
            return fragment
        }
    }


    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_edit, container, false)

        val titleEditText = view.findViewById<EditText>(R.id.editTitle)
        val textEditText = view.findViewById<EditText>(R.id.editText)
        val saveButton = view.findViewById<Button>(R.id.saveButton)

        note?.let {
            titleEditText.setText(it.title)
            textEditText.setText(it.text)
        }

        saveButton.setOnClickListener {
            val title = titleEditText.text.toString()
            val text = textEditText.text.toString()

            if (title.isBlank()) {
                Toast.makeText(context, "Zabilješka bez naslova nije sačuvana", Toast.LENGTH_SHORT).show()
            } else {
                val newNote = Note(
                    id = note?.id ?: java.util.UUID.randomUUID().toString(),
                    title = title,
                    text = text,
                    date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                )
                listener?.onNoteSaved(newNote, note != null)
                parentFragmentManager.popBackStack()
            }
        }
        return view
    }
}
