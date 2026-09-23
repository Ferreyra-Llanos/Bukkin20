package com.example.bukkin.ui.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bukkin.ViewModel.BookViewModel
import com.example.bukkin.ui.components.AddBookTabbedDialog
import com.example.bukkin.ui.components.BookSection

// Enum para controlar las opciones de ordenamiento
enum class SortOption(val label: String) {
    RECENT("Más recientes"),
    TITLE("Título (A-Z)"),
    RATING("Mejor calificados")
}

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

    // Estados de Filtrado y Ordenamiento
    var selectedStatusFilter by remember { mutableStateOf("ALL") } // "ALL", "READING", "PENDING", "COMPLETED"
    var selectedSortOption by remember { mutableStateOf(SortOption.RECENT) }
    var showSortMenu by remember { mutableStateOf(false) }

    // Diálogo unificado con Pestañas
    var showAddBookDialog by remember { mutableStateOf(false) }

    val isSortActive = selectedSortOption != SortOption.RECENT

    // --- LÓGICA DE FILTRADO Y ORDENAMIENTO ---
    val processedBooks = remember(books, searchQuery, selectedStatusFilter, selectedSortOption) {
        books
            // 1. Filtrar por texto de búsqueda
            .filter {
                it.title.contains(searchQuery, ignoreCase = true) ||
                        it.author.contains(searchQuery, ignoreCase = true)
            }
            // 2. Filtrar por estado seleccionado
            .filter { book ->
                when (selectedStatusFilter) {
                    "READING" -> book.status == "READING"
                    "PENDING" -> book.status == "PENDING"
                    "COMPLETED" -> book.status == "COMPLETED"
                    else -> true
                }
            }
            // 3. Aplicar ordenamiento
            .let { list ->
                when (selectedSortOption) {
                    SortOption.RECENT -> list.sortedByDescending { it.lastActive }
                    SortOption.TITLE -> list.sortedBy { it.title.lowercase() }
                    SortOption.RATING -> list.sortedByDescending { it.rating ?: 0 }
                }
            }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    if (isSearchActive) {
                        TextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("Buscar por título o autor...") },
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
                    IconButton(onClick = {
                        isSearchActive = !isSearchActive
                        if (!isSearchActive) searchQuery = ""
                    }) {
                        Icon(Icons.Default.Search, contentDescription = "Buscar")
                    }
                },
                actions = {
                    // Menú desplegable para Cambiar el Orden
                    Box {
                        BadgedBox(
                            badge = {
                                if (isSortActive) {
                                    Badge(
                                        containerColor = MaterialTheme.colorScheme.secondary,
                                        modifier = Modifier.offset(x = (-4).dp, y = 4.dp)
                                    )
                                }
                            }
                        ) {
                            IconButton(onClick = { showSortMenu = true }) {
                                Icon(
                                    imageVector = Icons.Default.Sort,
                                    contentDescription = "Ordenar",
                                    tint = if (isSortActive) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        DropdownMenu(
                            expanded = showSortMenu,
                            onDismissRequest = { showSortMenu = false }
                        ) {
                            SortOption.entries.forEach { option ->
                                DropdownMenuItem(
                                    text = {
                                        Row(
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text(
                                                text = option.label,
                                                fontWeight = if (option == selectedSortOption) FontWeight.Bold else FontWeight.Normal
                                            )
                                            if (option == selectedSortOption) {
                                                Text("✓", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                            }
                                        }
                                    },
                                    onClick = {
                                        selectedSortOption = option
                                        showSortMenu = false
                                    }
                                )
                            }
                        }
                    }

                    // Botón de Perfil
                    IconButton(onClick = { onProfileClick() }) {
                        Icon(Icons.Default.AccountCircle, contentDescription = "Perfil")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddBookDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Agregar Libro")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // --- BARRA DE CHIPS DE FILTRADO ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedStatusFilter == "ALL",
                    onClick = { selectedStatusFilter = "ALL" },
                    label = { Text("Todos (${books.size})") }
                )
                FilterChip(
                    selected = selectedStatusFilter == "READING",
                    onClick = { selectedStatusFilter = "READING" },
                    label = { Text("Leyendo (${books.count { it.status == "READING" }})") }
                )
                FilterChip(
                    selected = selectedStatusFilter == "PENDING",
                    onClick = { selectedStatusFilter = "PENDING" },
                    label = { Text("Pendientes (${books.count { it.status == "PENDING" }})") }
                )
                FilterChip(
                    selected = selectedStatusFilter == "COMPLETED",
                    onClick = { selectedStatusFilter = "COMPLETED" },
                    label = { Text("Terminados (${books.count { it.status == "COMPLETED" }})") }
                )
            }

            // --- VISTA DE LIBROS ---
            if (processedBooks.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (searchQuery.isNotBlank()) "No hay resultados para '$searchQuery'" else "No hay libros en esta categoría.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 15.sp
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(20.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    if (selectedStatusFilter == "ALL" && searchQuery.isBlank()) {
                        // Modo Vista General por Secciones
                        val recent = processedBooks.sortedByDescending { it.lastActive }
                        val reading = processedBooks.filter { it.status == "READING" }
                        val pending = processedBooks.filter { it.status == "PENDING" }
                        val completed = processedBooks.filter { it.status == "COMPLETED" }

                        if (reading.isNotEmpty()) item { BookSection("Leyendo actualmente", reading, onBookClick) }
                        if (pending.isNotEmpty()) item { BookSection("Pendientes", pending, onBookClick) }
                        if (completed.isNotEmpty()) item { BookSection("Terminados", completed, onBookClick) }
                        if (recent.isNotEmpty()) item { BookSection("Todos los libros", recent, onBookClick) }
                    } else {
                        // Modo Filtro Específico o Búsqueda Activa
                        val categoryTitle = when (selectedStatusFilter) {
                            "READING" -> "Libros en lectura"
                            "PENDING" -> "Libros pendientes"
                            "COMPLETED" -> "Libros terminados"
                            else -> "Resultados"
                        }
                        item {
                            BookSection(
                                title = "$categoryTitle (${processedBooks.size})",
                                books = processedBooks,
                                onBookClick = onBookClick
                            )
                        }
                    }
                }
            }
        }

        // Diálogo con Pestañas para Agregar Libro (Web/Manual)
        if (showAddBookDialog) {
            AddBookTabbedDialog(
                viewModel = viewModel,
                onDismiss = { showAddBookDialog = false }
            )
        }
    }
}