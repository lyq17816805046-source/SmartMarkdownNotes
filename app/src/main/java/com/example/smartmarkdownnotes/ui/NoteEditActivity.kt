package com.example.smartmarkdownnotes.ui

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.lifecycleScope
import com.example.smartmarkdownnotes.R
import com.example.smartmarkdownnotes.data.model.Note
import com.example.smartmarkdownnotes.ui.viewmodel.NoteViewModel
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import io.noties.markwon.Markwon
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Date

class NoteEditActivity : AppCompatActivity() {
    private val viewModel: NoteViewModel by viewModels()
    private lateinit var etTitle: EditText
    private lateinit var etContent: EditText
    private lateinit var progressBar: ProgressBar
    private lateinit var btnContinue: MaterialButton
    private lateinit var btnPolish: MaterialButton
    private lateinit var btnSummarize: MaterialButton
    
    private var noteId: Long = 0
    private var currentNote: Note? = null
    private var saveJob: Job? = null
    private lateinit var markwon: Markwon
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_note_edit)
        
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "编辑笔记"
        
        markwon = Markwon.create(this)
        
        initViews()
        setupAIControls()
        setupObservers()
        
        noteId = intent.getLongExtra("note_id", 0)
        if (noteId > 0) {
            loadNote()
        }
    }
    
    private fun initViews() {
        etTitle = findViewById(R.id.etTitle)
        etContent = findViewById(R.id.etContent)
        progressBar = findViewById(R.id.progressBar)
        btnContinue = findViewById(R.id.btnContinue)
        btnPolish = findViewById(R.id.btnPolish)
        btnSummarize = findViewById(R.id.btnSummarize)
        
        etTitle.doAfterTextChanged { scheduleSave() }
        etContent.doAfterTextChanged { scheduleSave() }
    }
    
    private fun setupAIControls() {
        btnContinue.setOnClickListener {
            if (checkApiKey()) {
                val text = etContent.text.toString()
                if (text.isNotBlank()) {
                    viewModel.continueWriting(text)
                } else {
                    Toast.makeText(this, "请先输入一些内容", Toast.LENGTH_SHORT).show()
                }
            }
        }
        
        btnPolish.setOnClickListener {
            if (checkApiKey()) {
                val text = etContent.text.toString()
                if (text.isNotBlank()) {
                    viewModel.polishText(text)
                } else {
                    Toast.makeText(this, "请先输入一些内容", Toast.LENGTH_SHORT).show()
                }
            }
        }
        
        btnSummarize.setOnClickListener {
            if (checkApiKey()) {
                val text = etContent.text.toString()
                if (text.isNotBlank()) {
                    viewModel.summarizeText(text)
                } else {
                    Toast.makeText(this, "请先输入一些内容", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    
    private fun checkApiKey(): Boolean {
        if (!viewModel.hasApiKey()) {
            MaterialAlertDialogBuilder(this)
                .setTitle("需要API Key")
                .setMessage("请先设置OpenAI API Key才能使用AI功能")
                .setPositiveButton("去设置") { _, _ ->
                    startActivity(Intent(this, AISettingsActivity::class.java))
                }
                .setNegativeButton("取消", null)
                .show()
            return false
        }
        return true
    }
    
    private fun setupObservers() {
        viewModel.aiLoading.observe(this) { isLoading ->
            progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            btnContinue.isEnabled = !isLoading
            btnPolish.isEnabled = !isLoading
            btnSummarize.isEnabled = !isLoading
        }
        
        viewModel.aiError.observe(this) { error ->
            error?.let {
                Toast.makeText(this, it, Toast.LENGTH_LONG).show()
                viewModel.clearAIResult()
            }
        }
        
        viewModel.aiResult.observe(this) { result ->
            result?.let { content ->
                showAIResultDialog(content)
                viewModel.clearAIResult()
            }
        }
    }
    
    private fun showAIResultDialog(content: String) {
        MaterialAlertDialogBuilder(this)
            .setTitle("AI生成结果")
            .setMessage(content)
            .setPositiveButton("插入") { _, _ ->
                etContent.append("\n\n$content")
                scheduleSave()
            }
            .setNegativeButton("替换") { _, _ ->
                etContent.setText(content)
                scheduleSave()
            }
            .setNeutralButton("取消", null)
            .show()
    }
    
    private fun loadNote() {
        viewModel.getNoteById(noteId) { note ->
            note?.let {
                currentNote = it
                etTitle.setText(it.title)
                etContent.setText(it.content)
            }
        }
    }
    
    private fun scheduleSave() {
        saveJob?.cancel()
        saveJob = lifecycleScope.launch {
            delay(1000)
            saveNote()
        }
    }
    
    private fun saveNote() {
        val title = etTitle.text.toString()
        val content = etContent.text.toString()
        
        val note = currentNote?.copy(
            title = title,
            content = content,
            updatedAt = Date()
        ) ?: Note(
            id = noteId,
            title = title,
            content = content
        )
        
        if (note.id > 0) {
            viewModel.updateNote(note)
        } else {
            viewModel.insertNote(note) {}
        }
    }
    
    override fun onPause() {
        super.onPause()
        saveJob?.cancel()
        saveNote()
    }
    
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
    
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.edit_menu, menu)
        return true
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            R.id.action_preview -> {
                showPreview()
                true
            }
            R.id.action_settings -> {
                startActivity(Intent(this, AISettingsActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    
    private fun showPreview() {
        val markdown = "# ${etTitle.text}\n\n${etContent.text}"
        MaterialAlertDialogBuilder(this)
            .setTitle("预览")
            .setMessage(markdown)
            .setPositiveButton("确定", null)
            .show()
    }
}
