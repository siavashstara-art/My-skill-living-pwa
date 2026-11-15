package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "book_projects")
data class BookProject(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val author: String = "آفرینا",
    val originalText: String,
    val compactSummary: String = "",
    val standardSummary: String = "",
    val detailedSummary: String = "",
    val narrationText: String = "",
    val bgMusicType: String = "calm", // "calm", "rain", "none"
    val musicVolume: Float = 0.25f,
    val narrationVolume: Float = 1.0f,
    val duckingEnabled: Boolean = true,
    val introChimeEnabled: Boolean = true,
    val audioPath: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
