package com.example.bukkin

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.ViewModelProvider
import com.example.bukkin.ViewModel.BookViewModel
import com.example.bukkin.ViewModel.BookViewModelFactory
import com.example.bukkin.data.BookDatabase
import com.example.bukkin.ui.navigation.AppNavigation
import com.example.bukkin.ui.theme.BukkinTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // 1. Leer la preferencia guardada en SharedPreferences
        val sharedPreferences = getSharedPreferences("bukkin_prefs", Context.MODE_PRIVATE)
        val savedDarkTheme = sharedPreferences.getBoolean("is_dark_theme", false)

        // 2. Forzar el modo noche global a nivel de aplicación
        if (savedDarkTheme) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }

        // 3. Inicializar la Splash Screen oficial
        installSplashScreen()

        super.onCreate(savedInstanceState)

        val database = BookDatabase.getDatabase(applicationContext)
        val viewModelFactory = BookViewModelFactory(database.bookDao(), database.noteDao())
        val viewModel = ViewModelProvider(this, viewModelFactory)[BookViewModel::class.java]

        setContent {
            var isDarkTheme by remember { mutableStateOf(savedDarkTheme) }

            BukkinTheme(darkTheme = isDarkTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = androidx.navigation.compose.rememberNavController()

                    AppNavigation(
                        navController = navController,
                        viewModel = viewModel,
                        isDarkTheme = isDarkTheme,
                        onToggleDarkTheme = { newThemeValue ->
                            isDarkTheme = newThemeValue

                            // Actualizamos SharedPreferences y notificamos a AppCompatDelegate
                            sharedPreferences.edit()
                                .putBoolean("is_dark_theme", newThemeValue)
                                .apply()

                            if (newThemeValue) {
                                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                            } else {
                                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                            }
                        }
                    )
                }
            }
        }
    }
}