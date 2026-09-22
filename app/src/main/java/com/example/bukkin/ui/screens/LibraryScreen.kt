package com.example.bukkin.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.bukkin.ViewModel.BookViewModel
import com.example.bukkin.ui.components.AddBookDialog
import com.example.bukkin.ui.components.BookSection

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    viewModel: BookViewModel,
    onBookClick: (Int) -> Unit,
    onProfileClick: () -> Unit // <-- NUEVO: Agregamos el callback para ir al perfil
) {
    val books by viewModel.allBooks.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var isSearchActive by remember { mutableStateOf(false) }

    var showAddDialog by remember { mutableStateOf(false)}

    val filteredBooks = books.filter {
        it.title.contains(searchQuery, ignoreCase = true) ||
                it.author.contains(searchQuery, ignoreCase = true)
    }

    val recentBooks = filteredBooks.sortedByDescending { it.lastActive }
    val readingBooks = filteredBooks.filter { it.status == "READING" }
    val pendingBooks = filteredBooks.filter { it.status == "PENDING" }
    val completedBooks = filteredBooks.filter { it.status == "COMPLETED" }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    if (isSearchActive) {
                        TextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("Buscar libro...") },
                            singleLine = true,
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                disabledContainerColor = Color.Transparent
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        Text(
                            text = "Bukkin",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { isSearchActive = !isSearchActive }) {
                        Icon(Icons.Default.Search, contentDescription = "Buscar")
                    }
                },
                actions = {
                    // MODIFICADO: Ahora ejecuta la acción de ir al perfil
                    IconButton(onClick = { onProfileClick() }) {
                        Icon(Icons.Default.AccountCircle, contentDescription = "Perfil")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Agregar Libro")
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            if (recentBooks.isNotEmpty()) {
                item { BookSection("Últimos libros", recentBooks, onBookClick) }
            }
            if (readingBooks.isNotEmpty()) {
                item { BookSection("Leyendo actualmente", readingBooks, onBookClick) }
            }
            if (pendingBooks.isNotEmpty()) {
                item { BookSection("Pendientes", pendingBooks, onBookClick) }
            }
            if (completedBooks.isNotEmpty()) {
                item { BookSection("Terminados", completedBooks, onBookClick) }
            }
        }
        if (showAddDialog) {
            AddBookDialog(
                onDismiss = { showAddDialog = false },
                onConfirm = { title, author, colorArgb ->
                    viewModel.addBook(title, author, colorArgb)
                    showAddDialog = false
                }
            )
        }
    }
}