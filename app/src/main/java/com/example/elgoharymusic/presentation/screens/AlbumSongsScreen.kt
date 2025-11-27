package com.example.elgoharymusic.presentation.screens

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.elgoharymusic.presentation.utils.MediaDetailScreen
import com.example.elgoharymusic.presentation.utils.MediaType
import com.example.elgoharymusic.presentation.viewmodels.FavViewModel
import com.example.elgoharymusic.presentation.viewmodels.MusicViewModel
import com.example.elgoharymusic.presentation.viewmodels.PlaylistViewModel

@Composable
fun AlbumSongsScreen(
    albumId: Long,
    musicViewModel: MusicViewModel,
    favViewModel: FavViewModel,
    playlistViewModel: PlaylistViewModel,
    currentLanguage: String,
    context: Context,
    navigateToPlaylist: (Long) -> Unit,
    onNavigateBack: () -> Unit,
    onNavigateToPlayer: () -> Unit,
    onNavigateToAlbum: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    MediaDetailScreen(
        mediaType = MediaType.Album(albumId),
        musicViewModel = musicViewModel,
        favViewModel = favViewModel,
        playlistViewModel = playlistViewModel,
        currentLanguage = currentLanguage,
        context = context,
        onNavigateBack = onNavigateBack,
        onNavigateToPlayer = onNavigateToPlayer,
        onNavigateToMedia = onNavigateToAlbum,
        navigateToPlaylist = navigateToPlaylist,
        modifier = modifier
    )
}