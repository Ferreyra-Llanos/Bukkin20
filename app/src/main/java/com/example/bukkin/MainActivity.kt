package com.example.bukkin

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.compose.rememberNavController
import com.example.bukkin.ViewModel.BookViewModel
import com.example.bukkin.ViewModel.BookViewModelFactory
import com.example.bukkin.data.BookDatabase
import com.example.bukkin.ui.navigation.AppNavigation
import com.example.bukkin.ui.screens.LibraryScreen
import com.example.bukkin.ui.theme.BukkinTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. Inicializamos la base de datos local
        val database = BookDatabase.getDatabase(applicationContext)

        // 2. Creamos el ViewModel usando nuestra fábrica (Factory)
        val viewModelFactory = BookViewModelFactory(database.bookDao(), database.noteDao())
        val viewModel = ViewModelProvider(this, viewModelFactory)[BookViewModel::class.java]

        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Creamos el controlador de navegación
                    val navController = rememberNavController()

                    // Cargamos el mapa de navegación central
                    AppNavigation(
                        navController = navController,
                        viewModel = viewModel
                    )
                }
            }
        }
    }
}