package com.example.smartmarkdownnotes.ui

import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.smartmarkdownnotes.R
import com.example.smartmarkdownnotes.ui.viewmodel.NoteViewModel
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText

class AISettingsActivity : AppCompatActivity() {
    private val viewModel: NoteViewModel by viewModels()
    private lateinit var etApiKey: TextInputEditText
    private lateinit var btnSave: MaterialButton
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ai_settings)
        
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "AI设置"
        
        etApiKey = findViewById(R.id.etApiKey)
        btnSave = findViewById(R.id.btnSave)
        
        // Load saved API key
        val savedKey = viewModel.getApiKey()
        savedKey?.let {
            etApiKey.setText(it)
        }
        
        btnSave.setOnClickListener {
            saveSettings()
        }
    }
    
    private fun saveSettings() {
        val apiKey = etApiKey.text?.toString()?.trim() ?: ""
        
        if (apiKey.isEmpty()) {
            Toast.makeText(this, "请输入API Key", Toast.LENGTH_SHORT).show()
            return
        }
        
        if (!apiKey.startsWith("sk-")) {
            Toast.makeText(this, "API Key格式不正确，应以sk-开头", Toast.LENGTH_LONG).show()
            return
        }
        
        viewModel.saveApiKey(apiKey)
        Toast.makeText(this, "设置已保存", Toast.LENGTH_SHORT).show()
        finish()
    }
    
    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
