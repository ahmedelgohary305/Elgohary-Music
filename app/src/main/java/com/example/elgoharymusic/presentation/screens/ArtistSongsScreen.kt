package com.example.elgoharymusic.presentation.screens

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.elgoharymusic.data.repoImpl.AppLanguage
import com.example.elgoharymusic.presentation.utils.MediaDetailScreen
import com.example.elgoharymusic.presentation.utils.MediaType
import com.example.elgoharymusic.presentation.viewmodels.FavViewModel
import com.example.elgoharymusic.presentation.viewmodels.MusicViewModel
import com.example.elgoharymusic.presentation.viewmodels.PlaylistViewModel


@Composable
fun ArtistSongsScreen(
    artistId: Long,
    musicViewModel: MusicViewModel,
    favViewModel: FavViewModel,
    playlistViewModel: PlaylistViewModel,
    navigateToPlaylist: (Long) -> Unit,
    context: Context,
    currentLanguage: AppLanguage,
    onNavigateBack: () -> Unit,
    onNavigateToPlayer: () -> Unit,
    onNavigateToArtist: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    MediaDetailScreen(
        mediaType = MediaType.Artist(artistId),
        musicViewModel = musicViewModel,
        favViewModel = favViewModel,
        playlistViewModel = playlistViewModel,
        currentLanguage = currentLanguage,
        context = context,
        onNavigateBack = onNavigateBack,
        onNavigateToPlayer = onNavigateToPlayer,
        onNavigateToMedia = onNavigateToArtist,
        navigateToPlaylist = navigateToPlaylist,
        modifier = modifier
    )
}