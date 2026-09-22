package com.example.bukkin.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.bukkin.data.entities.BookEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BookDao {
    // 1. Insertar un nuevo libro a la biblioteca
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBook(book: BookEntity): Long

    // 2. Obtener todos los libros ordenados por título (en tiempo real con Flow)
    @Query("SELECT * FROM table_books ORDER BY title ASC")
    fun getAllBooks(): Flow<List<BookEntity>>

    // 3. Obtener un libro específico por su ID
    @Query("SELECT * FROM table_books WHERE id = :bookId")
    fun getBookById(bookId: Int): Flow<BookEntity?>

    // 4. Cambiar el estado del libro (PENDING, READING, COMPLETED)
    @Query("UPDATE table_books SET status = :newStatus WHERE id = :bookId")
    suspend fun updateBookStatus(bookId: Int, newStatus: String)

    // 5. Finalizar la lectura: Guardar opinión final, calificación de 0 a 10 y cambiar estado a COMPLETED
    @Query("UPDATE table_books SET finalReview = :review, rating = :rating, status = 'COMPLETED' WHERE id = :bookId")
    suspend fun completeBook(bookId: Int, review: String, rating: Int)

    // 6. Eliminar un libro
    @Delete
    suspend fun deleteBook(book: BookEntity)

    @Update
    suspend fun  updateBook(book: BookEntity)
}