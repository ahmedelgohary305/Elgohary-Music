package com.example.elgoharymusic.presentation.screens

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.elgoharymusic.R
import com.example.elgoharymusic.data.repoImpl.AppLanguage
import com.example.elgoharymusic.domain.models.Playlist
import com.example.elgoharymusic.presentation.Routes
import com.example.elgoharymusic.presentation.utils.BottomContent
import com.example.elgoharymusic.presentation.utils.CreatePlaylistDialog
import com.example.elgoharymusic.presentation.utils.MediaArtworkBox
import com.example.elgoharymusic.presentation.utils.PlaylistBottomSheet
import com.example.elgoharymusic.presentation.utils.PlaylistSheetMode
import com.example.elgoharymusic.presentation.utils.PlaylistSheetState
import com.example.elgoharymusic.presentation.utils.SongDialogsHandler
import com.example.elgoharymusic.presentation.utils.SongList
import com.example.elgoharymusic.presentation.utils.SongMenuOption
import com.example.elgoharymusic.presentation.utils.formatPlaylistCreation
import com.example.elgoharymusic.presentation.utils.rememberSongDialogsState
import com.example.elgoharymusic.presentation.utils.toLocalizedDigits
import com.example.elgoharymusic.presentation.viewmodels.FavViewModel
import com.example.elgoharymusic.presentation.viewmodels.MusicViewModel
import com.example.elgoharymusic.presentation.viewmodels.PlaylistViewModel

@Composable
fun PlaylistDetailScreen(
    playlistId: Long,
    context: Context,
    currentLanguage: AppLanguage,
    playlistViewModel: PlaylistViewModel,
    musicViewModel: MusicViewModel,
    favViewModel: FavViewModel,
    navController: NavController,
    onNavigateToPlayer: () -> Unit,
) {
    val currentPlaylist by playlistViewModel.currentPlaylist.collectAsStateWithLifecycle()
    val currentSong by musicViewModel.currentSong.collectAsStateWithLifecycle()
    val isPlaying by musicViewModel.isPlaying.collectAsStateWithLifecycle()
    val queueSongs by musicViewModel.queueSongs.collectAsStateWithLifecycle()
    val songs by musicViewModel.songs.collectAsStateWithLifecycle()
    val isSelectionMode by musicViewModel.isSelectionMode.collectAsStateWithLifecycle()
    val showCreatePlaylistDialog by musicViewModel.showCreatePlaylistDialog.collectAsStateWithLifecycle()
    val selectedSongsForPlaylist by musicViewModel.selectedSongsForPlaylist.collectAsStateWithLifecycle()

    var sheetState by remember { mutableStateOf<PlaylistSheetState?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var selectedSongs by remember { mutableStateOf(setOf<Long>()) }

    val coroutineScope = rememberCoroutineScope()
    val dialogsState = rememberSongDialogsState()

    SongDialogsHandler(
        songToEdit = dialogsState.songToEdit,
        songsToDelete = dialogsState.songsToDelete,
        onEditDismiss = dialogsState::dismissEdit,
        onDeleteDismiss = dialogsState::dismissDelete,
        musicViewModel = musicViewModel,
        favViewModel = favViewModel,
        playlistViewModel = playlistViewModel,
        coroutineScope = coroutineScope,
        context = context
    )

    // Define playlist-specific menu option
    val playlistMenuOptions = listOf(
        SongMenuOption(
            text = stringResource(R.string.remove_from_playlist),
            icon = R.drawable.remove_from_playlist,
            action = { song ->
                playlistViewModel.removeSongFromPlaylist(playlistId, song.id)
            }
        )
    )

    LaunchedEffect(playlistId) {
        playlistViewModel.loadPlaylistWithSongs(playlistId)
    }



    currentPlaylist?.let { playlist ->
        Box(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header with playlist info
                PlaylistDetailHeader(
                    playlist = playlist,
                    context = context,
                    modifier = Modifier.size(22.dp),
                    currentLanguage = currentLanguage,
                    onBackClick = { navController.popBackStack() },
                    onEditClick = {
                        sheetState = PlaylistSheetState(
                            mode = PlaylistSheetMode.EDIT,
                            playlistId = playlistId,
                            playlistName = playlist.name,
                            playlistDescription = playlist.description ?: ""
                        )
                    },
                    onDeleteClick = { showDeleteDialog = true },
                    onAddSongsClick = {
                        selectedSongs = emptySet()
                        sheetState = PlaylistSheetState(
                            mode = PlaylistSheetMode.ADD_SONGS,
                            playlistId = playlistId,
                            playlistName = playlist.name,
                            selectedSongs = selectedSongs
                        )
                    }
                )

                // Home with playlist-specific menu option
                SongList(
                    songs = playlist.songs,
                    currentSong = currentSong,
                    musicViewModel = musicViewModel,
                    context = context,
                    currentLanguage = currentLanguage,
                    onSongClick = { song ->
                        musicViewModel.playSong(song, playlist.songs)
                    },
                    additionalMenuOptions = playlistMenuOptions,
                    onEditSong = { song -> dialogsState.showEditDialog(song) },
                    onDeleteSong = { song -> dialogsState.showDeleteDialog(listOf(song)) }
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
                                navController.navigate(Routes.PlaylistDetail.createRoute(playlistId))
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

    // Unified bottom sheet
    sheetState?.let { state ->
        val availableSongs = remember(songs, currentPlaylist, state.mode) {
            when (state.mode) {
                PlaylistSheetMode.ADD_SONGS -> {
                    songs.filter { song ->
                        currentPlaylist?.songs?.none { it.id == song.id } ?: true
                    }
                }
                PlaylistSheetMode.CREATE -> songs
                PlaylistSheetMode.EDIT -> emptyList()
            }
        }

        PlaylistBottomSheet(
            sheetState = state.copy(selectedSongs = selectedSongs),
            songs = availableSongs,
            allSongs = songs,
            onDismiss = {
                sheetState = null
                selectedSongs = emptySet()
            },
            onCreatePlaylist = { name, description, songs ->
                sheetState = null
                selectedSongs = emptySet()
            },
            onAddSongsToPlaylist = { playlistId, songIds ->
                songIds.forEach { songId ->
                    val song = songs.find { it.id == songId }
                    if (song != null) {
                        playlistViewModel.addSongToPlaylist(playlistId, song)
                    }
                }
                sheetState = null
                selectedSongs = emptySet()
            },
            onEditPlaylist = { playlistId, name, description ->
                val updatedPlaylist = currentPlaylist!!.copy(
                    name = name,
                    description = description
                )
                playlistViewModel.updatePlaylist(updatedPlaylist)
                playlistViewModel.loadPlaylistWithSongs(playlistId)
                sheetState = null
            },
            onSelectedSongsChange = { newSelection ->
                selectedSongs = newSelection
                sheetState = state.copy(selectedSongs = newSelection)
            },
            context = context
        )
    }

    // Delete dialog
    if (showDeleteDialog) {
        DeletePlaylistDialog(
            onDismiss = { showDeleteDialog = false },
            onConfirm = {
                playlistViewModel.deletePlaylist(playlistId)
                navController.navigateUp()
                showDeleteDialog = false
            }
        )
    }
}

@Composable
fun PlaylistDetailHeader(
    playlist: Playlist,
    context: Context,
    onBackClick: () -> Unit,
    currentLanguage: AppLanguage,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onAddSongsClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var showMenu by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        colorScheme.secondary.copy(alpha = 0.3f),
                        colorScheme.secondary.copy(alpha = 0.3f),
                        Color.Transparent
                    )
                )
            )
            .padding(horizontal = 8.dp, vertical = 24.dp)
    ) {
        // Top bar with back button and menu
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(
                    painter = painterResource(R.drawable.arrow_back),
                    contentDescription = "Back",
                    tint = colorScheme.onBackground
                )
            }

            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(
                        painter = painterResource(R.drawable.more_vert),
                        contentDescription = "More options",
                        tint = colorScheme.onBackground
                    )
                }

                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.add_songs)) },
                        onClick = {
                            onAddSongsClick()
                            showMenu = false
                        },
                        leadingIcon = {
                            Icon(
                                painterResource(R.drawable.addplaylist),
                                contentDescription = null,
                                modifier = modifier
                            )
                        }
                    )

                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.edit_playlist)) },
                        onClick = {
                            onEditClick()
                            showMenu = false
                        },
                        leadingIcon = {
                            Icon(
                                painterResource(R.drawable.edit),
                                contentDescription = null,
                                modifier = modifier
                            )
                        }
                    )

                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.delete_playlist)) },
                        onClick = {
                            onDeleteClick()
                            showMenu = false
                        },
                        leadingIcon = {
                            Icon(
                                painterResource(R.drawable.deletefromplaylist),
                                contentDescription = null,
                                modifier = modifier
                            )
                        }
                    )
                }

            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Playlist info
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            MediaArtworkBox(
                artworkUri = playlist.songs.firstOrNull()?.albumArtUri, // Returns null safely
                modifier = Modifier.padding(horizontal = 8.dp),
                size = 100.dp
            )
            Spacer(modifier = Modifier.width(16.dp))

            // Playlist details
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = playlist.name,
                    style = MaterialTheme.typography.headlineMedium,
                    color = colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                // Songs count and date with better styling
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    val songCountText = if (playlist.songs.size == 1) {
                        context.getString(R.string.one_song)
                    } else {
                        val localizedCount = playlist.songs.size.toLocalizedDigits(currentLanguage)
                        context.getString(R.string.multiple_songs, localizedCount)
                    }

                    Text(
                        text = songCountText,
                        style = MaterialTheme.typography.bodyLarge,
                        color = colorScheme.onBackground.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.offset(
                            y = if (currentLanguage == AppLanguage.ARABIC) (-6).dp else 0.dp
                        )
                    )

                    // Separator dot
                    Text(
                        text = "•",
                        style = MaterialTheme.typography.bodyMedium,
                        color = colorScheme.onSurfaceVariant,
                        modifier = Modifier.offset(
                            y = if (currentLanguage == AppLanguage.ARABIC) (-6).dp else 0.dp
                        )
                    )
                    Text(
                        text = playlist.createdAt.formatPlaylistCreation(
                            language = currentLanguage,
                            context = LocalContext.current
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                        color = colorScheme.onSurface.copy(0.8f),
                        modifier = Modifier.offset(
                            y = if (currentLanguage == AppLanguage.ARABIC) (-6).dp else 0.dp
                        )
                    )
                }

                playlist.description?.let { description ->
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodySmall,
                        color = colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = 4.dp).offset(
                            y = if (currentLanguage == AppLanguage.ARABIC) (-6).dp else 0.dp
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun DeletePlaylistDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(
                containerColor = colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Title
                Text(
                    text = stringResource(R.string.delete_playlist_title),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Content
                Text(
                    text = stringResource(R.string.delete_playlist_message),
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.fillMaxWidth()
                )

                // Centered Buttons
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = colorScheme.onBackground.copy(0.8f)
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(stringResource(R.string.cancel))
                    }

                    Button(
                        onClick = onConfirm,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colorScheme.error
                        ),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(stringResource(R.string.delete))
                    }
                }
            }
        }
    }
}
