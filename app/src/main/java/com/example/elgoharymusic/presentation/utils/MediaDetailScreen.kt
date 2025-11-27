package com.example.elgoharymusic.presentation.utils

import android.content.Context
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.elgoharymusic.R
import com.example.elgoharymusic.domain.models.Song
import com.example.elgoharymusic.presentation.viewmodels.FavViewModel
import com.example.elgoharymusic.presentation.viewmodels.MusicViewModel
import com.example.elgoharymusic.presentation.viewmodels.PlaylistViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

sealed class MediaType {
    data class Album(val albumId: Long) : MediaType()
    data class Artist(val artistId: Long) : MediaType()
}

// Data class to hold media information
data class MediaInfo(
    val id: Long,
    val name: String,
    val subtitle: String? = null, // Artist name for albums, null for artists
    val artworkUri: Uri?,
    val songs: List<Song>,
    val totalDuration: Long
)

@Composable
fun MediaDetailScreen(
    mediaType: MediaType,
    musicViewModel: MusicViewModel,
    favViewModel: FavViewModel,
    playlistViewModel: PlaylistViewModel,
    currentLanguage: String,
    context: Context,
    onNavigateBack: () -> Unit,
    onNavigateToPlayer: () -> Unit,
    onNavigateToMedia: (Long) -> Unit,
    navigateToPlaylist: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    // Collect the state flows so changes trigger recomposition
    val albums by musicViewModel.albums.collectAsStateWithLifecycle()
    val artists by musicViewModel.artists.collectAsStateWithLifecycle()
    val songs by musicViewModel.songs.collectAsStateWithLifecycle()
    val isSelectionMode by musicViewModel.isSelectionMode.collectAsStateWithLifecycle()
    val showCreatePlaylistDialog by musicViewModel.showCreatePlaylistDialog.collectAsStateWithLifecycle()
    val selectedSongsForPlaylist by musicViewModel.selectedSongsForPlaylist.collectAsStateWithLifecycle()

    // Get media info based on type - now depends on actual data
    val mediaInfo by remember(mediaType, albums, artists) {
        derivedStateOf {
            when (mediaType) {
                is MediaType.Album -> {
                    albums.find { it.albumId == mediaType.albumId }?.let { album ->
                        MediaInfo(
                            id = album.albumId,
                            name = album.name,
                            subtitle = album.artist,
                            artworkUri = album.albumArtUri,
                            songs = album.songs,
                            totalDuration = album.songs.sumOf { it.duration }
                        )
                    }
                }
                is MediaType.Artist -> {
                    artists.find { it.artistId == mediaType.artistId }?.let { artist ->
                        MediaInfo(
                            id = artist.artistId,
                            name = artist.name,
                            subtitle = null,
                            artworkUri = artist.albumArtUri,
                            songs = artist.songs,
                            totalDuration = artist.songs.sumOf { it.duration }
                        )
                    }
                }
            }
        }
    }

    val currentSong by musicViewModel.currentSong.collectAsStateWithLifecycle()
    val isPlaying by musicViewModel.isPlaying.collectAsStateWithLifecycle()
    val queueSongs by musicViewModel.queueSongs.collectAsStateWithLifecycle()

    val coroutineScope = rememberCoroutineScope()
    val dialogsState = rememberSongDialogsState()

    val useArtistForCallback = mediaType is MediaType.Artist

    val onSongEditedCallback: (Long, String) -> Unit = remember(mediaType) {
        { editedSongId: Long, newName: String ->
            coroutineScope.launch {
                delay(500)

                when (mediaType) {
                    is MediaType.Album -> {
                        val updatedAlbums = musicViewModel.albums.value
                        val currentAlbumStillExists = updatedAlbums.any { it.albumId == mediaType.albumId }

                        if (!currentAlbumStillExists) {
                            val newAlbum = updatedAlbums.find {
                                it.name.equals(newName, ignoreCase = true)
                            }
                            if (newAlbum != null) {
                                onNavigateToMedia(newAlbum.albumId)
                            } else {
                                onNavigateBack()
                            }
                        } else {
                            val currentAlbum = updatedAlbums.find { it.albumId == mediaType.albumId }
                            val songStillInAlbum = currentAlbum?.songs?.any {
                                it.id == editedSongId
                            } ?: false

                            if (!songStillInAlbum) {
                                val newAlbum = updatedAlbums.find {
                                    it.songs.any { song -> song.id == editedSongId }
                                }
                                if (newAlbum != null) {
                                    onNavigateToMedia(newAlbum.albumId)
                                }
                            }
                        }
                    }
                    is MediaType.Artist -> {
                        val updatedArtists = musicViewModel.artists.value
                        val currentArtistStillExists = updatedArtists.any { it.artistId == mediaType.artistId }

                        if (!currentArtistStillExists) {
                            val newArtist = updatedArtists.find {
                                it.name.equals(newName, ignoreCase = true)
                            }
                            if (newArtist != null) {
                                onNavigateToMedia(newArtist.artistId)
                            } else {
                                onNavigateBack()
                            }
                        } else {
                            val currentArtist = updatedArtists.find { it.artistId == mediaType.artistId }
                            val songStillWithArtist = currentArtist?.songs?.any {
                                it.id == editedSongId
                            } ?: false

                            if (!songStillWithArtist) {
                                val newArtist = updatedArtists.find {
                                    it.songs.any { song -> song.id == editedSongId }
                                }
                                if (newArtist != null) {
                                    onNavigateToMedia(newArtist.artistId)
                                }
                            }
                        }
                    }
                }
            }
            Unit
        }
    }

    LaunchedEffect(mediaInfo) {
        if (mediaInfo == null) {
            onNavigateBack()
        }
    }

    SongDialogsHandler(
        songToEdit = dialogsState.songToEdit,
        songsToDelete = dialogsState.songsToDelete,
        onEditDismiss = dialogsState::dismissEdit,
        onDeleteDismiss = dialogsState::dismissDelete,
        musicViewModel = musicViewModel,
        favViewModel = favViewModel,
        playlistViewModel = playlistViewModel,
        coroutineScope = coroutineScope,
        context = context,
        onSongEdited = onSongEditedCallback,
        useArtistForCallback = useArtistForCallback
    )

    mediaInfo?.let { media ->
        Box(modifier = modifier.fillMaxSize()) {
            Column(modifier = Modifier.fillMaxSize()) {
                MediaHeader(
                    mediaInfo = media,
                    onNavigateBack = onNavigateBack,
                    currentLanguage = currentLanguage
                )

                SongList(
                    songs = media.songs,
                    currentSong = currentSong,
                    musicViewModel = musicViewModel,
                    onSongClick = { song ->
                        musicViewModel.playSongs(media.songs, media.songs.indexOf(song))
                    },
                    onEditSong = { song -> dialogsState.showEditDialog(song) },
                    onDeleteSong = { song -> dialogsState.showDeleteDialog(listOf(song)) },
                    context = context,
                    currentLanguage = currentLanguage
                )
            }

            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
            ) {
                BottomContent(
                    songs = songs,
                    currentLanguage = currentLanguage,
                    isSelectionMode = isSelectionMode,
                    musicViewModel = musicViewModel,
                    favViewModel = favViewModel,
                    coroutineScope = coroutineScope,
                    currentSong = currentSong,
                    isPlaying = isPlaying,
                    queueSongs = queueSongs,
                    colorScheme = colorScheme,
                    context = context,
                    onNavigateToPlayer = onNavigateToPlayer,
                    onAddToPlaylist = { selectedSongs ->
                        musicViewModel.setSelectedSongsForPlaylist(selectedSongs)
                        musicViewModel.showCreatePlaylistDialog(true)
                    },
                    onDeleteSelected = { selectedSongsList ->
                        if (selectedSongsList.isNotEmpty()) {
                            dialogsState.showDeleteDialog(selectedSongsList)
                        }
                    }
                )
            }
            if (showCreatePlaylistDialog) {
                CreatePlaylistDialog(
                    onDismiss = {
                        musicViewModel.showCreatePlaylistDialog(false)
                        musicViewModel.setSelectedSongsForPlaylist(emptyList())
                    },
                    onCreatePlaylist = { name, description, selectedSongs ->
                        musicViewModel.showCreatePlaylistDialog(false)
                        musicViewModel.setSelectedSongsForPlaylist(emptyList())

                        playlistViewModel.createPlaylistWithSongs(
                            name = name,
                            description = description,
                            songs = selectedSongs,
                            onPlaylistCreated = { playlistId ->
                                navigateToPlaylist(playlistId)
                            }
                        )
                    },
                    musicViewModel = musicViewModel,
                    currentLanguage = currentLanguage,
                    context = context,
                    preselectedSongs = selectedSongsForPlaylist
                )
            }
        }
    }
}

@Composable
fun MediaHeader(
    mediaInfo: MediaInfo,
    currentLanguage: String,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp),
                clip = false
            )
            .clip(RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp))
            .background(
                if (isSystemInDarkTheme()) colorScheme.surface else colorScheme.onSurface.copy(0.2f)
            ),
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Top bar with back button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onNavigateBack, modifier = Modifier.size(28.dp)) {
                    Icon(
                        painter = painterResource(R.drawable.arrow_back),
                        contentDescription = "Navigate back",
                        tint = colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                // More options
                IconButton(
                    onClick = { /* Handle more options in future updates */ },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.more_vert),
                        contentDescription = "More options",
                        tint = colorScheme.onSurface
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Media info section
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Media artwork
                MediaArtworkBox(
                    artworkUri = mediaInfo.artworkUri,
                    size = 120.dp
                )

                Spacer(modifier = Modifier.width(16.dp))

                // Media details
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = mediaInfo.name,
                        style = MaterialTheme.typography.headlineSmall,
                        color = colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    // Show subtitle only for albums (artist name)
                    mediaInfo.subtitle?.let { subtitle ->
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.titleMedium,
                            color = colorScheme.onSurface.copy(alpha = 0.8f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.offset(
                                y = if (currentLanguage == LocaleManager.Language.ARABIC.code) (-6).dp else 0.dp
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = TimeFormatter.formatTotalDuration(
                            mediaInfo.totalDuration,
                            mediaInfo.songs.size,
                            context = LocalContext.current
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                        color = colorScheme.onSurface.copy(alpha = 0.6f),
                        modifier = Modifier.offset(
                            y = if (currentLanguage == LocaleManager.Language.ARABIC.code) (-8).dp else 0.dp
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun MediaArtworkBox(
    artworkUri: Uri?,
    modifier: Modifier = Modifier,
    size: Dp = 120.dp
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(16.dp))
            .background(if (isSystemInDarkTheme()) colorScheme.surfaceVariant else Color.LightGray)
            .border(
                width = 1.dp,
                color = colorScheme.outline.copy(0.5f),
                shape = RoundedCornerShape(16.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        AsyncImage(
            model = artworkUri,
            contentDescription = "Media artwork",
            modifier = Modifier.matchParentSize(),
            contentScale = ContentScale.Crop
        )
    }
}
