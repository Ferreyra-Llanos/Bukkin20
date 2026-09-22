package com.example.bukkin.ViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.bukkin.data.dao.BookDao
import com.example.bukkin.data.dao.NoteDao
import com.example.bukkin.data.entities.BookEntity
import com.example.bukkin.data.entities.NoteEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class BookViewModel (
    private val bookDao: BookDao,
    private val noteDao: NoteDao
) : ViewModel() {

    val allBooks: StateFlow<List<BookEntity>> = bookDao.getAllBooks()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Creamos un ID de libro seleccionado activo
    private val _currentBookId = MutableStateFlow(-1)

    // Escuchamos dinámicamente las notas del libro seleccionado actualmente
    val currentBookNotes = _currentBookId.flatMapLatest { bookId ->
        noteDao.getNotesForBook(bookId)
    }

    // 2. OPERACIONES DE LIBROS (Usando corrutinas en segundo plano con viewModelScope)

    // Agregar un nuevo libro
    fun addBook(title: String, author: String, coverColor: Int) {
        viewModelScope.launch {
            val newBook = BookEntity(
                title = title,
                author = author,
                coverColor = coverColor,
                status = "PENDING"
            )
            bookDao.insertBook(newBook)
        }
    }

    // Cambiar estado (por ejemplo, a 'READING' cuando el usuario lo empieza a leer)
    fun startReadingBook(bookId: Int) {
        viewModelScope.launch {
            bookDao.updateBookStatus(bookId, "READING")
        }
    }

    // Terminar libro guardando la calificación final (0-10) y la reseña final
    fun finishBook(bookId: Int, review: String, rating: Int) {
        viewModelScope.launch {
            bookDao.completeBook(bookId, review, rating)
        }
    }

    // Eliminar un libro
    fun deleteBook(book: BookEntity) {
        viewModelScope.launch {
            bookDao.deleteBook(book)
        }
    }

    fun updateBook(book: BookEntity) {
        viewModelScope.launch {
            bookDao.updateBook(book)
        }
    }

    // 3. OPERACIONES DE NOTAS

    fun setSelectedBook(bookId: Int) {
        _currentBookId.value = bookId
    }

    // Obtener las notas de un libro específico en tiempo real
    fun getNotesForBook(bookId: Int): Flow<List<NoteEntity>> {
        return noteDao.getNotesForBook(bookId)
    }

    // Agregar una nota rápida asociada a un libro
    fun addNoteToBook(bookId: Int, content: String, pageNumber: Int? = null) {
        viewModelScope.launch {
            val newNote = NoteEntity(
                bookId = bookId,
                content = content,
                pageNumber = pageNumber
            )
            noteDao.insertNote(newNote)
        }
    }

    // Eliminar una nota específica
    fun deleteNote(note: NoteEntity) {
        viewModelScope.launch {
            noteDao.deleteNote(note)
        }
    }

    // 📊 ESTADÍSTICAS EN TIEMPO REAL (Mapeadas directamente desde el Flow de libros)

    // Total de libros guardados
    val totalBooksCount: StateFlow<Int> = allBooks
        .map { it.size }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // Libros con estado "PENDING"
    val pendingBooksCount: StateFlow<Int> = allBooks
        .map { lista -> lista.count { it.status == "PENDING" } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // Libros con estado "READING"
    val readingBooksCount: StateFlow<Int> = allBooks
        .map { lista -> lista.count { it.status == "READING" } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // Libros con estado "COMPLETED"
    val completedBooksCount: StateFlow<Int> = allBooks
        .map { lista -> lista.count { it.status == "COMPLETED" } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // Conteo total de notas globales de la aplicación
    // Nota: Si tu noteDao no tiene getAllNotes(), puedes reemplazar noteDao.getAllNotes()
    // provisionalmente por un flujo vacío como: kotlinx.coroutines.flow.flowOf(emptyList<NoteEntity>())
    val totalNotesCount: StateFlow<Int> = noteDao.getAllNotes()
        .map { it.size }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)
}

// 4. FACTORY (Fábrica) DEL VIEWMODEL
class BookViewModelFactory(
    private val bookDao: BookDao,
    private val noteDao: NoteDao
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BookViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return BookViewModel(bookDao, noteDao) as T
        }
        throw IllegalArgumentException("Clase ViewModel desconocida")
    }
}