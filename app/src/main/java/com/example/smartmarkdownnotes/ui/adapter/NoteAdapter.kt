package com.example.smartmarkdownnotes.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.smartmarkdownnotes.R
import com.example.smartmarkdownnotes.data.model.Note
import java.text.SimpleDateFormat
import java.util.Locale

class NoteAdapter(
    private val onItemClick: (Note) -> Unit,
    private val onItemLongClick: (Note, View) -> Boolean
) : ListAdapter<Note, NoteAdapter.NoteViewHolder>(NoteDiffCallback()) {
    
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
    
    inner class NoteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleText: TextView = itemView.findViewById(R.id.tvTitle)
        private val previewText: TextView = itemView.findViewById(R.id.tvPreview)
        private val dateText: TextView = itemView.findViewById(R.id.tvDate)
        
        fun bind(note: Note) {
            titleText.text = note.title.ifEmpty { "未命名笔记" }
            previewText.text = note.content.take(100).replace("\n", " ").ifEmpty { "无内容" }
            dateText.text = dateFormat.format(note.updatedAt)
            
            itemView.setOnClickListener { onItemClick(note) }
            itemView.setOnLongClickListener { view ->
                onItemLongClick(note, view)
                true
            }
        }
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_note, parent, false)
        return NoteViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
    
    class NoteDiffCallback : DiffUtil.ItemCallback<Note>() {
        override fun areItemsTheSame(oldItem: Note, newItem: Note): Boolean {
            return oldItem.id == newItem.id
        }
        
        override fun areContentsTheSame(oldItem: Note, newItem: Note): Boolean {
            return oldItem == newItem
        }
    }
}
