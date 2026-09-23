package com.example.bukkin.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.bukkin.ViewModel.BookViewModel
import com.example.bukkin.ui.screens.BookDetailScreen
import com.example.bukkin.ui.screens.LibraryScreen
import com.example.bukkin.ui.screens.ProfileScreen

// 1. Definición de las pantallas de la aplicación
sealed class Screen(val route: String) {
    object Library : Screen("library")
    object Profile : Screen("profile")
    object BookDetail : Screen("book_detail/{bookId}") {
        fun createRoute(bookId: Int) = "book_detail/$bookId"
    }
}

// 2. El contenedor de navegación central (NavHost)
@Composable
fun AppNavigation(
    navController: NavHostController,
    viewModel: BookViewModel,
    isDarkTheme: Boolean,                          // <-- NUEVO
    onToggleDarkTheme: (Boolean) -> Unit,          // <-- NUEVO
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Library.route,
        modifier = modifier
    ) {
        composable(route = Screen.Library.route) {
            LibraryScreen(
                viewModel = viewModel,
                onBookClick = { bookId ->
                    navController.navigate(Screen.BookDetail.createRoute(bookId))
                },
                onProfileClick = {
                    navController.navigate(Screen.Profile.route)
                }
            )
        }

        // Renderizamos el perfil pasando los estados del tema
        composable(route = Screen.Profile.route) {
            ProfileScreen(
                viewModel = viewModel,
                onBackClick = { navController.popBackStack() },
                isDarkTheme = isDarkTheme,                  // <-- NUEVO
                onToggleDarkTheme = onToggleDarkTheme       // <-- NUEVO
            )
        }

        composable(
            route = Screen.BookDetail.route,
            arguments = listOf(
                navArgument("bookId") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val bookId = backStackEntry.arguments?.getInt("bookId") ?: -1

            BookDetailScreen(
                bookId = bookId,
                viewModel = viewModel,
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}