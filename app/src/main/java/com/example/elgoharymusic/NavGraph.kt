package com.example.elgoharymusic

import android.content.Context
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.elgoharymusic.presentation.Routes
import com.example.elgoharymusic.presentation.screens.AlbumSongsScreen
import com.example.elgoharymusic.presentation.screens.ArtistSongsScreen
import com.example.elgoharymusic.presentation.screens.FullPlayerScreen
import com.example.elgoharymusic.presentation.screens.ModernHomeScreen
import com.example.elgoharymusic.presentation.screens.PlaylistDetailScreen
import com.example.elgoharymusic.presentation.viewmodels.FavViewModel
import com.example.elgoharymusic.presentation.viewmodels.MusicViewModel
import com.example.elgoharymusic.presentation.viewmodels.PlaylistViewModel

@Composable
fun ModernMusicNavGraph(
    musicViewModel: MusicViewModel,
    favViewModel: FavViewModel,
    playlistViewModel: PlaylistViewModel,
    language: String,
    isDarkTheme: Boolean,
    context: Context
) {
    val navController = rememberNavController()
    var selectedTab by rememberSaveable { mutableIntStateOf(2) }

    val fullPlayerEnterTransition = slideInVertically(initialOffsetY = { it }) + fadeIn()
    val fullPlayerExitTransition = slideOutVertically(targetOffsetY = { it }) + fadeOut()

    NavHost(
        navController = navController,
        startDestination = Routes.Home.route
    ) {
        composable(Routes.Home.route) {
            AppBackground(isDarkTheme) {
                ModernHomeScreen(
                    musicViewModel = musicViewModel,
                    favViewModel = favViewModel,
                    navController = navController,
                    selectedTab = selectedTab,
                    onTabSelected = { selectedTab = it },
                    onNavigateToPlayer = {
                        navController.navigate(Routes.FullPlayer.route)
                    },
                    playlistViewModel = playlistViewModel,
                    context = context,
                    currentLanguage = language
                )
            }
        }

        composable(
            Routes.FullPlayer.route,
            enterTransition = { fullPlayerEnterTransition },
            exitTransition = { fullPlayerExitTransition }
        ) {
            AppBackground(isDarkTheme) {
                FullPlayerScreen(
                    musicViewModel = musicViewModel,
                    favViewModel = favViewModel,
                    onBack = { navController.popBackStack() },
                    context = context,
                    currentLanguage = language
                )
            }
        }

        composable(
            route = Routes.ArtistSongs.route,
            arguments = listOf(navArgument("artistId") { type = NavType.LongType }),
        ) { backStackEntry ->
            val artistId = backStackEntry.arguments?.getLong("artistId") ?: return@composable

            AppBackground(isDarkTheme) {
                ArtistSongsScreen(
                    artistId = artistId,
                    musicViewModel = musicViewModel,
                    favViewModel = favViewModel,
                    playlistViewModel = playlistViewModel,
                    currentLanguage = language,
                    context = context,
                    onNavigateToPlayer = {
                        navController.navigate(Routes.FullPlayer.route)
                    },
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    onNavigateToArtist = { newArtistId ->
                        // Navigate to the new artist and replace current artist in back stack
                        navController.navigate(Routes.ArtistSongs.createRoute(newArtistId)) {
                            popUpTo(Routes.ArtistSongs.createRoute(artistId)) {
                                inclusive = true
                            }
                        }
                    },
                    navigateToPlaylist = { playlistId ->
                        navController.navigate(Routes.PlaylistDetail.createRoute(playlistId))
                    }
                )
            }
        }

        composable(
            route = Routes.AlbumSongs.route,
            arguments = listOf(navArgument("albumId") { type = NavType.LongType }),
        ) { backStackEntry ->
            val albumId = backStackEntry.arguments?.getLong("albumId") ?: return@composable

            AppBackground(isDarkTheme) {
                AlbumSongsScreen(
                    albumId = albumId,
                    musicViewModel = musicViewModel,
                    favViewModel = favViewModel,
                    playlistViewModel = playlistViewModel,
                    currentLanguage = language,
                    context = context,
                    onNavigateToPlayer = {
                        navController.navigate(Routes.FullPlayer.route)
                    },
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    onNavigateToAlbum = { newAlbumId ->
                        // Navigate to the new album and replace current album in back stack
                        navController.navigate(Routes.AlbumSongs.createRoute(newAlbumId)) {
                            // Remove the current album screen from back stack
                            popUpTo(Routes.AlbumSongs.createRoute(albumId)) {
                                inclusive = true
                            }
                        }
                    },
                    navigateToPlaylist = { playlistId ->
                        navController.navigate(Routes.PlaylistDetail.createRoute(playlistId))
                    }
                )
            }
        }

        composable(
            route = Routes.PlaylistDetail.route,
            arguments = listOf(navArgument("playlistId") { type = NavType.LongType }),
        ) { backStackEntry ->
            val playlistId = backStackEntry.arguments?.getLong("playlistId") ?: return@composable
            AppBackground(isDarkTheme) {
                PlaylistDetailScreen(
                    playlistId = playlistId,
                    playlistViewModel = playlistViewModel,
                    musicViewModel = musicViewModel,
                    favViewModel = favViewModel,
                    navController = navController,
                    currentLanguage = language,
                    context = context
                ) {
                    navController.navigate(
                        Routes.FullPlayer.route
                    )
                }
            }
        }
    }
}

@Composable
fun AppBackground(
    isDarkTheme: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(
                       if (isDarkTheme) Color(0xFF272B49) else Color(0xFFDDDDF1),
                        MaterialTheme.colorScheme.background,
                       if (isDarkTheme) Color(0xFF060E28) else Color(0xFFE3E3F5)
                    ),
                    center = Offset(0.5f, 0.3f),
                    radius = 1800f
                )
            )
    ) {
        content()
    }
}
