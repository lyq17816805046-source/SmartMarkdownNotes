package com.example.smartmarkdownnotes.ui

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.PopupMenu
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.smartmarkdownnotes.R
import com.example.smartmarkdownnotes.data.model.Note
import com.example.smartmarkdownnotes.ui.adapter.NoteAdapter
import com.example.smartmarkdownnotes.ui.viewmodel.NoteViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private val viewModel: NoteViewModel by viewModels()
    private lateinit var noteAdapter: NoteAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var fab: FloatingActionButton
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        setupRecyclerView()
        setupFab()
        setupSearch()
        observeNotes()
    }
    
    private fun setupRecyclerView() {
        recyclerView = findViewById(R.id.recyclerView)
        noteAdapter = NoteAdapter(
            onItemClick = { note -> openNoteEdit(note) },
            onItemLongClick = { note, view -> showNoteOptions(note, view) }
        )
        recyclerView.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = noteAdapter
        }
    }
    
    private fun setupFab() {
        fab = findViewById(R.id.fabAdd)
        fab.setOnClickListener {
            createNewNote()
        }
    }
    
    private fun setupSearch() {
        val searchView = findViewById<SearchView>(R.id.searchView)
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = false
            
            override fun onQueryTextChange(newText: String?): Boolean {
                viewModel.setSearchQuery(newText ?: "")
                return true
            }
        })
    }
    
    private fun observeNotes() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.searchQuery.collectLatest { query ->
                    if (query.isEmpty()) {
                        viewModel.allNotes.observe(this@MainActivity) { notes ->
                            noteAdapter.submitList(notes)
                        }
                    } else {
                        viewModel.searchNotes(query).observe(this@MainActivity) { notes ->
                            noteAdapter.submitList(notes)
                        }
                    }
                }
            }
        }
    }
    
    private fun createNewNote() {
        val newNote = Note(title = "", content = "")
        viewModel.insertNote(newNote) { noteId ->
            val intent = Intent(this, NoteEditActivity::class.java)
            intent.putExtra("note_id", noteId)
            startActivity(intent)
        }
    }
    
    private fun openNoteEdit(note: Note) {
        val intent = Intent(this, NoteEditActivity::class.java)
        intent.putExtra("note_id", note.id)
        startActivity(intent)
    }
    
    private fun showNoteOptions(note: Note, anchorView: View) {
        PopupMenu(this, anchorView).apply {
            menuInflater.inflate(R.menu.note_options, menu)
            setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.action_delete -> {
                        showDeleteConfirmDialog(note)
                        true
                    }
                    else -> false
                }
            }
            show()
        }
    }
    
    private fun showDeleteConfirmDialog(note: Note) {
        MaterialAlertDialogBuilder(this)
            .setTitle("删除笔记")
            .setMessage("确定要删除 \"${note.title.ifEmpty { "未命名笔记" }}\" 吗？")
            .setPositiveButton("删除") { _, _ ->
                viewModel.deleteNote(note)
            }
            .setNegativeButton("取消", null)
            .show()
    }
    
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> {
                startActivity(Intent(this, AISettingsActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    
    override fun onResume() {
        super.onResume()
        // Refresh list when returning from edit
        viewModel.allNotes.observe(this) { notes ->
            noteAdapter.submitList(notes)
        }
    }
}
