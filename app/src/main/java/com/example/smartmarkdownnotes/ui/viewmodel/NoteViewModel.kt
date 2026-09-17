package com.example.smartmarkdownnotes.ui.viewmodel

import android.app.Application
import androidx.lifecycle.*
import com.example.smartmarkdownnotes.data.db.NoteDatabase
import com.example.smartmarkdownnotes.data.model.Note
import com.example.smartmarkdownnotes.data.repository.AIRepository
import com.example.smartmarkdownnotes.data.repository.NoteRepository
import com.example.smartmarkdownnotes.util.AISettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class NoteViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: NoteRepository
    private val aiRepository: AIRepository
    
    val allNotes: LiveData<List<Note>>
    
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery
    
    private val _aiLoading = MutableLiveData(false)
    val aiLoading: LiveData<Boolean> = _aiLoading
    
    private val _aiError = MutableLiveData<String?>(null)
    val aiError: LiveData<String?> = _aiError
    
    private val _aiResult = MutableLiveData<String?>(null)
    val aiResult: LiveData<String?> = _aiResult
    
    init {
        val noteDao = NoteDatabase.getDatabase(application).noteDao()
        repository = NoteRepository(noteDao)
        aiRepository = AIRepository(application)
        allNotes = repository.allNotes.asLiveData()
    }
    
    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }
    
    fun searchNotes(query: String): LiveData<List<Note>> {
        return repository.searchNotes(query).asLiveData()
    }
    
    fun insertNote(note: Note, callback: (Long) -> Unit) = viewModelScope.launch {
        val id = repository.insertNote(note)
        callback(id)
    }
    
    fun updateNote(note: Note) = viewModelScope.launch {
        repository.updateNote(note)
    }
    
    fun deleteNote(note: Note) = viewModelScope.launch {
        repository.deleteNote(note)
    }
    
    fun getNoteById(id: Long, callback: (Note?) -> Unit) = viewModelScope.launch {
        val note = repository.getNoteById(id)
        callback(note)
    }
    
    // AI Settings Functions
    fun getAISettings(): AISettings = aiRepository.getSettings()
    
    fun saveAISettings(settings: AISettings) = aiRepository.saveSettings(settings)
    
    fun hasAISettings(): Boolean {
        val settings = aiRepository.getSettings()
        return settings.apiKey.isNotBlank() && settings.apiUrl.isNotBlank()
    }
    
    // AI Functions
    fun continueWriting(text: String) = viewModelScope.launch {
        _aiLoading.value = true
        _aiError.value = null
        _aiResult.value = null
        
        val settings = aiRepository.getSettings()
        if (settings.apiKey.isBlank() || settings.apiUrl.isBlank()) {
            _aiLoading.value = false
            _aiError.value = "请先设置AI服务参数"
            return@launch
        }
        
        val result = aiRepository.continueWriting(text, settings)
        _aiLoading.value = false
        
        result.onSuccess { content ->
            _aiResult.value = content
        }.onFailure { error ->
            _aiError.value = error.message ?: "AI生成失败"
        }
    }
    
    fun polishText(text: String) = viewModelScope.launch {
        _aiLoading.value = true
        _aiError.value = null
        _aiResult.value = null
        
        val settings = aiRepository.getSettings()
        if (settings.apiKey.isBlank() || settings.apiUrl.isBlank()) {
            _aiLoading.value = false
            _aiError.value = "请先设置AI服务参数"
            return@launch
        }
        
        val result = aiRepository.polishText(text, settings)
        _aiLoading.value = false
        
        result.onSuccess { content ->
            _aiResult.value = content
        }.onFailure { error ->
            _aiError.value = error.message ?: "AI润色失败"
        }
    }
    
    fun summarizeText(text: String) = viewModelScope.launch {
        _aiLoading.value = true
        _aiError.value = null
        _aiResult.value = null
        
        val settings = aiRepository.getSettings()
        if (settings.apiKey.isBlank() || settings.apiUrl.isBlank()) {
            _aiLoading.value = false
            _aiError.value = "请先设置AI服务参数"
            return@launch
        }
        
        val result = aiRepository.summarizeText(text, settings)
        _aiLoading.value = false
        
        result.onSuccess { content ->
            _aiResult.value = content
        }.onFailure { error ->
            _aiError.value = error.message ?: "AI总结失败"
        }
    }
    
    fun clearAIResult() {
        _aiResult.value = null
        _aiError.value = null
    }
}
