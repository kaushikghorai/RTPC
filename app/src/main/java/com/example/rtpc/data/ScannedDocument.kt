package com.example.rtpc.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "scanned_documents")
data class ScannedDocument(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val uri: String,
    val timestamp: Long = System.currentTimeMillis(),
    val type: String // "SCAN", "MERGE", "OCR"
)
