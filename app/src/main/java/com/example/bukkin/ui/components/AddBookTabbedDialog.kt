package com.example.bukkin.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.bukkin.ViewModel.BookViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddBookTabbedDialog(
    viewModel: BookViewModel,
    onDismiss: () -> Unit
) {
    // 0 = Buscar en la Web (API), 1 = Registro Manual
    var selectedTabIndex by remember { mutableStateOf(0) }

    Dialog(
        onDismissRequest = {
            viewModel.clearOnlineSearch()
            onDismiss()
        },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .fillMaxHeight(0.85f),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Selector de Pestañas (Tabs)
                TabRow(
                    selectedTabIndex = selectedTabIndex,
                    containerColor = Color.Transparent,
                    contentColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clip(RoundedCornerShape(12.dp))
                ) {
                    Tab(
                        selected = selectedTabIndex == 0,
                        onClick = { selectedTabIndex = 0 },
                        text = { Text("Buscar Online", fontWeight = FontWeight.SemiBold) },
                        icon = { Icon(Icons.Default.Language, contentDescription = null) }
                    )
                    Tab(
                        selected = selectedTabIndex == 1,
                        onClick = { selectedTabIndex = 1 },
                        text = { Text("Manual", fontWeight = FontWeight.SemiBold) },
                        icon = { Icon(Icons.Default.Edit, contentDescription = null) }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Contenido según la pestaña seleccionada
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    if (selectedTabIndex == 0) {
                        OnlineSearchTabContent(
                            viewModel = viewModel,
                            onBookAdded = {
                                viewModel.clearOnlineSearch()
                                onDismiss()
                            }
                        )
                    } else {
                        ManualAddTabContent(
                            onConfirm = { title, author, colorArgb ->
                                viewModel.addBook(title, author, colorArgb)
                                onDismiss()
                            }
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = {
                        viewModel.clearOnlineSearch()
                        onDismiss()
                    }) {
                        Text("Cancelar")
                    }
                }
            }
        }
    }
}

// TAB 1: BÚSQUEDA ONLINE
@Composable
private fun OnlineSearchTabContent(
    viewModel: BookViewModel,
    onBookAdded: () -> Unit
) {
    var query by remember { mutableStateOf("") }
    val searchResults by viewModel.onlineSearchResults.collectAsState()
    val isSearching by viewModel.isSearchingOnline.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    val defaultColors = listOf(
        Color(0xFF2B2D42), Color(0xFF8D99AE), Color(0xFFD90429),
        Color(0xFF006D77), Color(0xFFE29578), Color(0xFF6B705C)
    )

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OutlinedTextField(
            value = query,
            onValueChange = {
                query = it
                viewModel.searchBooksOnline(it)
            },
            placeholder = { Text("Título, autor o ISBN...") },
            leadingIcon = {
                IconButton(onClick = { viewModel.searchBooksOnline(query) }) {
                    Icon(Icons.Default.Search, contentDescription = "Buscar")
                }
            },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            if (isSearching) {
                CircularProgressIndicator()
            } else if (errorMessage != null) {
                Text(
                    text = errorMessage ?: "",
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 14.sp
                )
            } else if (searchResults.isEmpty() && query.isNotBlank()) {
                Text(
                    text = "No se encontraron libros.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else if (searchResults.isEmpty() && query.isBlank()) {
                Text(
                    text = "Escribe para buscar miles de libros en Google Books",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 14.sp
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(searchResults) { bookItem ->
                        val volumeInfo = bookItem.volumeInfo
                        val title = volumeInfo.title ?: "Sin título"
                        val authors = volumeInfo.authors?.joinToString(", ") ?: "Autor desconocido"
                        val imageUrl = volumeInfo.imageLinks?.thumbnail?.replace("http://", "https://")
                        val randomColor = defaultColors[title.length % defaultColors.size]

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.addBookFromApi(
                                        bookItem = bookItem,
                                        coverColorArgb = randomColor.toArgb()
                                    )
                                    onBookAdded()
                                },
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (!imageUrl.isNullOrBlank()) {
                                    AsyncImage(
                                        model = imageUrl,
                                        contentDescription = title,
                                        modifier = Modifier
                                            .width(45.dp)
                                            .height(68.dp)
                                            .clip(RoundedCornerShape(6.dp)),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .width(45.dp)
                                            .height(68.dp)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(randomColor),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            Icons.Default.Book,
                                            contentDescription = null,
                                            tint = Color.White
                                        )
                                    }
                                }

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = title,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        maxLines = 2
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = authors,
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// TAB 2: AGREGAR MANUALMENTE
@Composable
private fun ManualAddTabContent(
    onConfirm: (String, String, Int) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var author by remember { mutableStateOf("") }

    val presetColors = listOf(
        Color(0xFF2B2D42), Color(0xFF8D99AE), Color(0xFFD90429),
        Color(0xFF006D77), Color(0xFFE29578), Color(0xFF6B705C)
    )
    var selectedColor by remember { mutableStateOf(presetColors[0]) }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Título del libro") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        OutlinedTextField(
            value = author,
            onValueChange = { author = it },
            label = { Text("Autor") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        Text("Color de la portada", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            presetColors.forEach { color ->
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(color)
                        .clickable { selectedColor = color }
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = {
                if (title.isNotBlank() && author.isNotBlank()) {
                    onConfirm(title, author, selectedColor.toArgb())
                }
            },
            enabled = title.isNotBlank() && author.isNotBlank(),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Guardar Libro")
        }
    }
}