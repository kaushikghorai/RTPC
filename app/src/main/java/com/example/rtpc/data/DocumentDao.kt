package com.example.rtpc.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface DocumentDao {
    @Query("SELECT * FROM scanned_documents ORDER BY timestamp DESC")
    fun getAllDocuments(): Flow<List<ScannedDocument>>

    @Insert
    suspend fun insertDocument(document: ScannedDocument)

    @Query("DELETE FROM scanned_documents WHERE id = :id")
    suspend fun deleteDocument(id: Int)
}
