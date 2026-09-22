package com.example.bukkin.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
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
import com.example.bukkin.data.network.BookItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddBookOnlineDialog(
    viewModel: BookViewModel,
    onDismiss: () -> Unit
) {
    var query by remember { mutableStateOf("") }
    val searchResults by viewModel.onlineSearchResults.collectAsState()
    val isSearching by viewModel.isSearchingOnline.collectAsState()

    // Paleta de colores predefinida para la portada en caso de fallback
    val defaultColors = listOf(
        Color(0xFF2B2D42), Color(0xFF8D99AE), Color(0xFFD90429),
        Color(0xFF006D77), Color(0xFFE29578), Color(0xFF6B705C)
    )

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
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Buscar libro en internet 🌐",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                // Campo de búsqueda con icono de lupa
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it }, // Solo actualiza el texto local, NO busca automáticamente
                    placeholder = { Text("Título, autor o ISBN...") },
                    leadingIcon = {
                        IconButton(onClick = { viewModel.searchBooksOnline(query) }) {
                            Icon(Icons.Default.Search, contentDescription = "Buscar")
                        }
                    },
                    singleLine = true,
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        imeAction = androidx.compose.ui.text.input.ImeAction.Search
                    ),
                    keyboardActions = androidx.compose.foundation.text.KeyboardActions(
                        onSearch = { viewModel.searchBooksOnline(query) }
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                // Indicador de carga / Resultados
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    if (isSearching) {
                        CircularProgressIndicator()
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
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(searchResults) { bookItem ->
                                val volumeInfo = bookItem.volumeInfo
                                val title = volumeInfo.title ?: "Sin título"
                                val authors = volumeInfo.authors?.joinToString(", ") ?: "Autor desconocido"
                                val imageUrl = volumeInfo.imageLinks?.thumbnail?.replace("http://", "https://")

                                // Elegimos un color distintivo asignado
                                val randomColor = defaultColors[title.length % defaultColors.size]

                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            viewModel.addBookFromApi(
                                                bookItem = bookItem,
                                                coverColorArgb = randomColor.toArgb()
                                            )
                                            viewModel.clearOnlineSearch()
                                            onDismiss()
                                        },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // Muestra la imagen con Coil si existe, si no muestra una portada simulada
                                        if (!imageUrl.isNullOrBlank()) {
                                            AsyncImage(
                                                model = imageUrl,
                                                contentDescription = title,
                                                modifier = Modifier
                                                    .width(50.dp)
                                                    .height(75.dp)
                                                    .clip(RoundedCornerShape(6.dp)),
                                                contentScale = ContentScale.Crop
                                            )
                                        } else {
                                            Box(
                                                modifier = Modifier
                                                    .width(50.dp)
                                                    .height(75.dp)
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
                                                fontSize = 15.sp,
                                                maxLines = 2
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = authors,
                                                fontSize = 13.sp,
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

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = {
                        viewModel.clearOnlineSearch()
                        onDismiss()
                    }) {
                        Text("Cerrar")
                    }
                }
            }
        }
    }
}