package com.example.bukkin.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.bukkin.data.dao.BookDao
import com.example.bukkin.data.dao.NoteDao
import com.example.bukkin.data.entities.BookEntity
import com.example.bukkin.data.entities.NoteEntity

// 1. Declaramos las tablas y la versión.
// Si en el futuro cambias la estructura de una tabla, deberás subir la versión a 2.
@Database(entities = [BookEntity::class, NoteEntity::class], version = 1, exportSchema = false)
abstract class BookDatabase : RoomDatabase() {

    // 2. Definimos los accesos a los DAOs
    abstract fun bookDao(): BookDao
    abstract fun noteDao(): NoteDao

    companion object {
        // Volatile asegura que el valor de esta variable se actualice de inmediato en todos los hilos de ejecución
        @Volatile
        private var INSTANCE: BookDatabase? = null

        // 3. Patrón Singleton: Asegura que solo exista UNA instancia de la base de datos en toda la app
        fun getDatabase(context: Context): BookDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    BookDatabase::class.java,
                    "book_mind_database" // El nombre del archivo de base de datos en el celular
                )
                    // Esto ayuda a manejar de forma básica si cambias las tablas en el futuro sin migración estructurada
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}