package com.example.smartmarkdownnotes.ui

import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.smartmarkdownnotes.R
import com.example.smartmarkdownnotes.ui.viewmodel.NoteViewModel
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.android.material.switchmaterial.SwitchMaterial

class AISettingsActivity : AppCompatActivity() {
    private val viewModel: NoteViewModel by viewModels()
    private lateinit var etApiKey: TextInputEditText
    private lateinit var etApiUrl: TextInputEditText
    private lateinit var etModel: TextInputEditText
    private lateinit var btnSave: MaterialButton
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ai_settings)
        
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "AI设置"
        
        etApiKey = findViewById(R.id.etApiKey)
        etApiUrl = findViewById(R.id.etApiUrl)
        etModel = findViewById(R.id.etModel)
        btnSave = findViewById(R.id.btnSave)
        
        // Load saved settings
        val settings = viewModel.getAISettings()
        etApiKey.setText(settings.apiKey)
        etApiUrl.setText(settings.apiUrl)
        etModel.setText(settings.model)
        
        btnSave.setOnClickListener {
            saveSettings()
        }
    }
    
    private fun saveSettings() {
        val apiKey = etApiKey.text?.toString()?.trim() ?: ""
        val apiUrl = etApiUrl.text?.toString()?.trim() ?: ""
        val model = etModel.text?.toString()?.trim() ?: ""
        
        if (apiKey.isEmpty()) {
            Toast.makeText(this, "请输入API Key", Toast.LENGTH_SHORT).show()
            return
        }
        
        if (!apiKey.startsWith("sk-") && !apiKey.startsWith("ak-")) {
            Toast.makeText(this, "API Key格式可能不正确（通常以sk-或ak-开头）", Toast.LENGTH_LONG).show()
        }
        
        if (apiUrl.isEmpty()) {
            Toast.makeText(this, "请输入API URL", Toast.LENGTH_SHORT).show()
            return
        }
        
        if (model.isEmpty()) {
            Toast.makeText(this, "请输入模型名称", Toast.LENGTH_SHORT).show()
            return
        }
        
        val settings = com.example.smartmarkdownnotes.util.AISettings(
            apiKey = apiKey,
            apiUrl = apiUrl,
            model = model
        )
        
        viewModel.saveAISettings(settings)
        Toast.makeText(this, "设置已保存", Toast.LENGTH_SHORT).show()
        finish()
    }
    
    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
