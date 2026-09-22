package com.example.bukkin.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "table_notes")
data class NoteEntity (
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val bookId: Int, // Enlace directo con el id del libro
    val content: String,
    val pageNumber: Int? = null,
    val createdAt: Long = System.currentTimeMillis()
)