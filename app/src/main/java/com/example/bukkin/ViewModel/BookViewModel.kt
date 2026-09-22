package com.example.bukkin.ViewModel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.bukkin.data.dao.BookDao
import com.example.bukkin.data.dao.NoteDao
import com.example.bukkin.data.entities.BookEntity
import com.example.bukkin.data.entities.NoteEntity
import com.example.bukkin.data.network.BookItem
import com.example.bukkin.data.network.RetrofitClient
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class BookViewModel(
    private val bookDao: BookDao,
    private val noteDao: NoteDao
) : ViewModel() {

    // --- COMPANION OBJECT PARA CONSTANTES ---
    companion object {
        private const val GOOGLE_BOOKS_API_KEY: String = "AIzaSyD5Q9pn2nqVB17MEa9MAHsiKL-vaPsGwVc"
    }

    val allBooks: StateFlow<List<BookEntity>> = bookDao.getAllBooks()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _currentBookId = MutableStateFlow(-1)

    val currentBookNotes = _currentBookId.flatMapLatest { bookId ->
        noteDao.getNotesForBook(bookId)
    }

    // 2. OPERACIONES DE LIBROS

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

    fun startReadingBook(bookId: Int) {
        viewModelScope.launch {
            bookDao.updateBookStatus(bookId, "READING")
        }
    }

    fun finishBook(bookId: Int, review: String, rating: Int) {
        viewModelScope.launch {
            bookDao.completeBook(bookId, review, rating)
        }
    }

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

    fun getNotesForBook(bookId: Int): Flow<List<NoteEntity>> {
        return noteDao.getNotesForBook(bookId)
    }

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

    fun deleteNote(note: NoteEntity) {
        viewModelScope.launch {
            noteDao.deleteNote(note)
        }
    }

    // 📊 ESTADÍSTICAS EN TIEMPO REAL

    val totalBooksCount: StateFlow<Int> = allBooks
        .map { it.size }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val pendingBooksCount: StateFlow<Int> = allBooks
        .map { lista -> lista.count { it.status == "PENDING" } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val readingBooksCount: StateFlow<Int> = allBooks
        .map { lista -> lista.count { it.status == "READING" } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val completedBooksCount: StateFlow<Int> = allBooks
        .map { lista -> lista.count { it.status == "COMPLETED" } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val totalNotesCount: StateFlow<Int> = noteDao.getAllNotes()
        .map { it.size }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // --- BÚSQUEDA EN GOOGLE BOOKS API ---
    private val _onlineSearchResults = MutableStateFlow<List<BookItem>>(emptyList())
    val onlineSearchResults: StateFlow<List<BookItem>> = _onlineSearchResults

    private val _isSearchingOnline = MutableStateFlow(false)
    val isSearchingOnline: StateFlow<Boolean> = _isSearchingOnline

    private var searchJob: Job? = null

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    fun searchBooksOnline(query: String) {
        val cleanQuery = query.trim()
        searchJob?.cancel()

        if (cleanQuery.isBlank()) {
            _onlineSearchResults.value = emptyList()
            _isSearchingOnline.value = false
            _errorMessage.value = null
            return
        }

        searchJob = viewModelScope.launch {
            _isSearchingOnline.value = true
            _errorMessage.value = null

            // Esperamos medio segundo antes de disparar la llamada HTTP
            delay(500)

            try {
                val response = RetrofitClient.apiService.searchBooks(
                    query = cleanQuery,
                    apiKey = GOOGLE_BOOKS_API_KEY
                )
                _onlineSearchResults.value = response.items ?: emptyList()
            } catch (e: retrofit2.HttpException) {
                if (e.code() == 429) {
                    _errorMessage.value = "Límite de peticiones alcanzado. Espera un momento e intenta de nuevo."
                } else {
                    _errorMessage.value = "Error al conectar con el servidor (${e.code()})"
                }
                _onlineSearchResults.value = emptyList()
            } catch (e: Exception) {
                Log.e("API_ERROR", "Error buscando libros: ${e.message}", e)
                _errorMessage.value = "Revisa tu conexión a internet."
                _onlineSearchResults.value = emptyList()
            } finally {
                _isSearchingOnline.value = false
            }
        }
    }

    fun clearOnlineSearch() {
        searchJob?.cancel()
        _onlineSearchResults.value = emptyList()
        _isSearchingOnline.value = false
        _errorMessage.value = null
    }

    fun addBookFromApi(bookItem: BookItem, coverColorArgb: Int) {
        viewModelScope.launch {
            val volumeInfo = bookItem.volumeInfo
            val title = volumeInfo.title ?: "Sin título"
            val author = volumeInfo.authors?.joinToString(", ") ?: "Autor desconocido"
            val secureImageUrl = volumeInfo.imageLinks?.thumbnail?.replace("http://", "https://")

            val newBook = BookEntity(
                title = title,
                author = author,
                coverColor = coverColorArgb,
                imageUrl = secureImageUrl,
                status = "PENDING"
            )
            bookDao.insertBook(newBook)
        }
    }
}

// 4. FACTORY DEL VIEWMODEL
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