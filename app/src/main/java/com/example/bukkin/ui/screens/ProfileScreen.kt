package com.example.bukkin.ui.screens


import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bukkin.ViewModel.BookViewModel
import androidx.compose.ui.graphics.Color

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: BookViewModel,
    onBackClick: (() -> Unit)? = null // Por si quieres un botón de atrás, o integrarlo en una barra de navegación
) {
    // Recolectamos los estados del ViewModel
    val totalBooks by viewModel.totalBooksCount.collectAsState()
    val pendingBooks by viewModel.pendingBooksCount.collectAsState()
    val readingBooks by viewModel.readingBooksCount.collectAsState()
    val completedBooks by viewModel.completedBooksCount.collectAsState()
    val totalNotes by viewModel.totalNotesCount.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mi Perfil Lector", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    if (onBackClick != null) {
                        IconButton(onClick = onBackClick) {
                            Icon(
                                imageVector = Icons.Filled.ArrowBack,
                                contentDescription = "Volver"
                            )
                        }
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
            verticalArrangement = Arrangement.spacedBy(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // 1. AVATAR SIMULADO Y NOMBRE
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(vertical = 8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(90.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.size(50.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                Text(
                    text = "Lector Bukkin",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Devorando páginas desde 2026",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            HorizontalDivider()

            // 2. FILA DE ESTADÍSTICAS PRINCIPALES (Tarjetas Rápidas)
            Text(
                text = "Resumen General",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                modifier = Modifier.align(Alignment.Start)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    modifier = Modifier.weight(1f),
                    title = "Libros en total",
                    value = totalBooks.toString(),
                    icon = Icons.Default.Book,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
                StatCard(
                    modifier = Modifier.weight(1f),
                    title = "Notas escritas",
                    value = totalNotes.toString(),
                    icon = Icons.Default.EditNote,
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                )
            }

            // 3. DESGLOSE DEL ESTADO DE LECTURA
            Text(
                text = "Tu progreso actual",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                modifier = Modifier.align(Alignment.Start)
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    ProgressRow(
                        label = "Terminados",
                        count = completedBooks,
                        total = totalBooks,
                        icon = Icons.Default.CheckCircle,
                        color = MaterialTheme.colorScheme.primary
                    )

                    ProgressRow(
                        label = "Leyendo ahora",
                        count = readingBooks,
                        total = totalBooks,
                        icon = Icons.Default.MenuBook,
                        color = MaterialTheme.colorScheme.secondary
                    )

                    ProgressRow(
                        label = "Pendientes",
                        count = pendingBooks,
                        total = totalBooks,
                        icon = Icons.Default.Book,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }
            }
        }
    }
}

// COMPONENTE AUXILIAR 1: Tarjetas de estadísticas superiores
@Composable
fun StatCard(
    title: String,
    value: String,
    icon: ImageVector,
    containerColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = containerColor),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Text(text = value, fontSize = 28.sp, fontWeight = FontWeight.Bold)
            Text(text = title, fontSize = 12.sp, lineHeight = 16.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

// COMPONENTE AUXILIAR 2: Filas con barra de progreso
@Composable
fun ProgressRow(
    label: String,
    count: Int,
    total: Int,
    icon: ImageVector,
    color: Color
) {
    // Calculamos el porcentaje de manera segura para evitar división por cero
    val progress = if (total > 0) count.toFloat() / total.toFloat() else 0f
    val percentage = (progress * 100).toInt()

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
                Text(text = label, fontWeight = FontWeight.Medium, fontSize = 14.sp)
            }
            Text(
                text = "$count ($percentage%)",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(CircleShape),
            color = color,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
    }
}