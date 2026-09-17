package com.example.smartmarkdownnotes

import android.app.Application
import com.example.smartmarkdownnotes.data.db.NoteDatabase

class NotesApplication : Application() {
    val database by lazy { NoteDatabase.getDatabase(this) }
}
