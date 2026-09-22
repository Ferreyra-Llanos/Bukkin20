package com.example.bukkin.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "table_books")
data class BookEntity (
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val author: String,
    val status: String, // "PENDING", "READING", "COMPLETED"
    val finalReview: String? = null,
    val rating: Int? = null, // Escala del 0 al 10
    val coverUrl: String? = null, // Para cuando agreguemos portadas reales
    val coverColor: Int = 0xFFCCCCCC.toInt(), // Color temporal para la portada tipo Netflix
    val imageUrl: String? = null, // NUEVO: URL de portada si proviene de la API
    val lastActive: Long = System.currentTimeMillis() // ¡Clave para los "Últimos libros"!
)