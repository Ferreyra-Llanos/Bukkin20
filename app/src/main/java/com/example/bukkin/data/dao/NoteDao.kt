package com.example.bukkin.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.bukkin.data.entities.NoteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {
    // 1. Guardar una nueva nota (frase, opinión, etc.)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: NoteEntity)

    // 2. Obtener todas las notas de un libro específico, de la más nueva a la más antigua
    @Query("SELECT * FROM table_notes WHERE bookId = :bookId ORDER BY createdAt DESC")
    fun getNotesForBook(bookId: Int): Flow<List<NoteEntity>>

    // 3. Eliminar una nota específica (por si el usuario se arrepiente)
    @Delete
    suspend fun deleteNote(note: NoteEntity)

    @Query("SELECT * FROM table_notes")
    fun getAllNotes(): Flow<List<NoteEntity>>
}