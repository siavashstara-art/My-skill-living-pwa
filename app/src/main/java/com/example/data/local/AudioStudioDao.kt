package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface AudioStudioDao {
    // Audio Items
    @Query("SELECT * FROM audio_items ORDER BY createdAt DESC")
    fun getAllAudioItems(): Flow<List<AudioItem>>

    @Query("SELECT * FROM audio_items WHERE category = :category ORDER BY createdAt DESC")
    fun getAudioItemsByCategory(category: String): Flow<List<AudioItem>>

    @Query("SELECT * FROM audio_items WHERE id = :id")
    suspend fun getAudioItemById(id: Long): AudioItem?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAudioItem(item: AudioItem): Long

    @Update
    suspend fun updateAudioItem(item: AudioItem)

    @Delete
    suspend fun deleteAudioItem(item: AudioItem)

    // Book Projects
    @Query("SELECT * FROM book_projects ORDER BY createdAt DESC")
    fun getAllBookProjects(): Flow<List<BookProject>>

    @Query("SELECT * FROM book_projects WHERE id = :id")
    suspend fun getBookProjectById(id: Long): BookProject?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBookProject(project: BookProject): Long

    @Update
    suspend fun updateBookProject(project: BookProject)

    @Delete
    suspend fun deleteBookProject(project: BookProject)
}
