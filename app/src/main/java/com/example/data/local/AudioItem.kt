package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "audio_items")
data class AudioItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val filePath: String,
    val originalFilePath: String? = null,
    val durationMs: Long = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val category: String = "VOICE", // "VOICE", "TTS", "ENHANCED", "AUDIOBOOK"
    val isEnhanced: Boolean = false,
    val notes: String = ""
)
