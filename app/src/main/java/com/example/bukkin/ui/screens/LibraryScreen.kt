package com.example.bukkin.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.bukkin.ViewModel.BookViewModel
import com.example.bukkin.ui.components.AddBookDialog
import com.example.bukkin.ui.components.AddBookOnlineDialog
import com.example.bukkin.ui.components.BookSection

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    viewModel: BookViewModel,
    onBookClick: (Int) -> Unit,
    onProfileClick: () -> Unit
) {
    val books by viewModel.allBooks.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var isSearchActive by remember { mutableStateOf(false) }

    // Estados para controlar los dos diálogos
    var showAddDialog by remember { mutableStateOf(false) }
    var showAddOnlineDialog by remember { mutableStateOf(false) } // <-- NUEVO

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
                            placeholder = { Text("Buscar en biblioteca...") },
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
                            text = "BookMind",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { isSearchActive = !isSearchActive }) {
                        Icon(Icons.Default.Search, contentDescription = "Buscar local")
                    }
                },
                actions = {
                    // NUEVO: Botón para abrir la búsqueda en Google Books API
                    IconButton(onClick = { showAddOnlineDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Language,
                            contentDescription = "Buscar en internet",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    // Botón existente para ir al Perfil
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
                Icon(Icons.Default.Add, contentDescription = "Agregar Libro Manual")
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

        // 1. Diálogo para agregar libro manualmente
        if (showAddDialog) {
            AddBookDialog(
                onDismiss = { showAddDialog = false },
                onConfirm = { title, author, colorArgb ->
                    viewModel.addBook(title, author, colorArgb)
                    showAddDialog = false
                }
            )
        }

        // 2. NUEVO: Diálogo para buscar en Google Books API
        if (showAddOnlineDialog) {
            AddBookOnlineDialog(
                viewModel = viewModel,
                onDismiss = { showAddOnlineDialog = false }
            )
        }
    }
}