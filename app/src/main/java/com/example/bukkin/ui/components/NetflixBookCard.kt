package com.example.bukkin.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.bukkin.data.entities.BookEntity

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun NetflixBookCard(
    book: BookEntity,
    onClick: () -> Unit
) {
    var showRatingOverlay by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .width(120.dp)
            .height(180.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color(book.coverColor))
            .combinedClickable(
                onClick = onClick,
                onLongClick = { showRatingOverlay = true }
            ),
        contentAlignment = Alignment.Center
    ) {
        if (!book.imageUrl.isNullOrBlank()) {
            AsyncImage(
                model = book.imageUrl,
                contentDescription = book.title,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = book.title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = book.author,
                    fontSize = 11.sp,
                    color = Color.White.copy(alpha = 0.8f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        // --- OVERLAY DE CALIFICACIÓN Y ESTADO ---
        if (showRatingOverlay) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.85f))
                    .combinedClickable(
                        onClick = { showRatingOverlay = false }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(8.dp)
                ) {
                    when (book.status) {
                        "COMPLETED" -> {
                            Text("Tu Calificación", color = Color.Gray, fontSize = 11.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            if (book.rating != null && book.rating > 0) {
                                Text(
                                    text = "⭐ ${book.rating}/5",
                                    color = Color(0xFFFFD700),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                            } else {
                                Text(
                                    text = "Falta puntuar 📝",
                                    color = Color(0xFFFFB703),
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 13.sp,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                        "READING" -> {
                            Text(
                                text = "📖 Leyendo",
                                color = Color(0xFF90E0EF),
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                        else -> { // PENDING
                            Text(
                                text = "⏳ Pendiente",
                                color = Color.Yellow,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Toca para cerrar",
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 10.sp
                    )
                }
            }
        }
    }
}