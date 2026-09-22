package com.example.bukkin.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bukkin.ViewModel.BookViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookDetailScreen(
    bookId: Int,
    viewModel: BookViewModel,
    onBackClick: () -> Unit
) {
    val books by viewModel.allBooks.collectAsState()

    // Buscamos el libro específico por su ID
    val book = books.find { it.id == bookId }

    // Escuchamos las múltiples notas de este libro en tiempo real desde Room
    val notes by viewModel.currentBookNotes.collectAsState(initial = emptyList())

    // Control del estado para mostrar u ocultar el diálogo emergente
    var showAddNoteDialog by remember { mutableStateOf(false) }

    // Control del estado para el diálogo de eliminación del libro
    var showDeleteBookDialog by remember { mutableStateOf(false) }

    fun formatLongToDate(timeInMillis: Long): String {
        val sdf = SimpleDateFormat("dd MMM yyyy - HH:mm", Locale("es", "ES"))
        return sdf.format(Date(timeInMillis))
    }

    if (book == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Libro no encontrado")
        }
        return
    }

    // Al entrar a la pantalla, actualizamos el "lastActive" y configuramos el libro seleccionado en el ViewModel
    LaunchedEffect(bookId) {
        viewModel.setSelectedBook(bookId)
        viewModel.updateBook(book.copy(lastActive = System.currentTimeMillis()))
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalles del libro", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Atrás"
                        )
                    }
                },
                // NUEVO: Añadimos el botón de basura en la esquina superior derecha
                actions = {
                    IconButton(onClick = { showDeleteBookDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Eliminar libro",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // 1. CABECERA: Portada simulada y textos básicos
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .width(110.dp)
                        .height(165.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(book.coverColor))
                )

                Column(
                    modifier = Modifier.height(165.dp),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = book.title,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 28.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "por ${book.author}",
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            HorizontalDivider() // Actualizado al estándar de Material 3

            // 2. SELECTOR DE ESTADO (Chips de Netflix)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Estado de lectura", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val states = listOf(
                        "PENDING" to "Pendiente",
                        "READING" to "Leyendo",
                        "COMPLETED" to "Terminado"
                    )
                    states.forEach { (statusCode, label) ->
                        val isSelected = book.status == statusCode
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                viewModel.updateBook(book.copy(status = statusCode))
                            },
                            label = { Text(label) }
                        )
                    }
                }
            }

            // 3. CALIFICACIÓN (Solo si está terminado)
            if (book.status == "COMPLETED") {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Tu calificación", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        val currentRating = book.rating ?: 0
                        for (i in 1..5) {
                            val isStarred = i <= (currentRating / 2)
                            Icon(
                                imageVector = if (isStarred) Icons.Filled.Star else Icons.Outlined.Star,
                                contentDescription = "Estrella $i",
                                tint = if (isStarred) Color(0xFFFFD700) else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier
                                    .size(32.dp)
                                    .clickable {
                                        viewModel.updateBook(book.copy(rating = i * 2))
                                    }
                            )
                        }
                    }
                }
            }

            HorizontalDivider()

            // 4. NUEVA SECCIÓN: DIARIO DE NOTAS MÚLTIPLES
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Diario de lectura", fontWeight = FontWeight.Bold, fontSize = 18.sp)

                    FilledTonalButton(
                        onClick = { showAddNoteDialog = true },
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Añadir Nota", fontSize = 12.sp)
                    }
                }

                if (notes.isEmpty()) {
                    Text(
                        text = "Aún no has escrito notas para este libro. ¡Añade tu primer pensamiento!",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                } else {
                    notes.forEach { note ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Mostramos la fecha y, si existe el número de página, lo añadimos al lado
                                    val pageText = if (note.pageNumber != null) " • Pág. ${note.pageNumber}" else ""
                                    Text(
                                        text = "${formatLongToDate(note.createdAt)}$pageText",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    IconButton(
                                        onClick = { viewModel.deleteNote(note) },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Eliminar nota",
                                            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = note.content,
                                    fontSize = 14.sp,
                                    lineHeight = 20.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // DIÁLOGO EMERGENTE CON CAMPO DE PÁGINA OPCIONAL
        if (showAddNoteDialog) {
            var noteInput by remember { mutableStateOf("") }
            var pageInput by remember { mutableStateOf("") } // Guardamos la página temporalmente como texto

            AlertDialog(
                onDismissRequest = { showAddNoteDialog = false },
                title = { Text("Escribir pensamiento") },
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Campo para la nota de texto
                        OutlinedTextField(
                            value = noteInput,
                            onValueChange = { noteInput = it },
                            placeholder = { Text("¿Qué parte te llamó la atención? Escribe aquí...") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp),
                            shape = RoundedCornerShape(12.dp)
                        )

                        // NUEVO: Campo opcional para el número de página
                        OutlinedTextField(
                            value = pageInput,
                            onValueChange = { input ->
                                // Validación rápida: Solo permitimos que el usuario escriba números enteros
                                if (input.all { it.isDigit() }) {
                                    pageInput = input
                                }
                            },
                            label = { Text("Número de página (Opcional)") },
                            placeholder = { Text("Ej: 42") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (noteInput.isNotBlank()) {
                                // Convertimos el texto de la página a número (si está vacío, pasa como null)
                                val pageNumberInt = pageInput.toIntOrNull()

                                viewModel.addNoteToBook(bookId, noteInput, pageNumberInt)
                                showAddNoteDialog = false
                            }
                        },
                        enabled = noteInput.isNotBlank()
                    ) {
                        Text("Añadir")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAddNoteDialog = false }) {
                        Text("Cancelar")
                    }
                }
            )
        }

        if (showDeleteBookDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteBookDialog = false },
                title = { Text("¿Eliminar este libro?") },
                text = {
                    Text("Esta acción no se puede deshacer. Se borrarán permanentemente el libro \"${book.title}\" y todas las notas asociadas a él.")
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showDeleteBookDialog = false
                            viewModel.deleteBook(book) // Llama a tu función del ViewModel
                            onBackClick() // Regresa automáticamente a la biblioteca
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error,
                            contentColor = MaterialTheme.colorScheme.onError
                        )
                    ) {
                        Text("Eliminar")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteBookDialog = false }) {
                        Text("Cancelar")
                    }
                }
            )
        }
    }
}
