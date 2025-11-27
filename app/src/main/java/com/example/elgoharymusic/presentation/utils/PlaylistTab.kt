package com.example.elgoharymusic.presentation.utils

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.elgoharymusic.R
import com.example.elgoharymusic.domain.models.Playlist
import com.example.elgoharymusic.domain.models.Song
import com.example.elgoharymusic.presentation.Routes
import com.example.elgoharymusic.presentation.viewmodels.MusicViewModel
import com.example.elgoharymusic.presentation.viewmodels.PlaylistViewModel

// PlaylistTab.kt
@Composable
fun PlaylistTab(
    playlistViewModel: PlaylistViewModel,
    musicViewModel: MusicViewModel,
    navController: NavController,
    context: Context,
    currentLanguage: String,
    modifier: Modifier = Modifier,
    onCreatePlaylist: () -> Unit = {}
) {
    val playlists by playlistViewModel.playlists.collectAsStateWithLifecycle()

    Column(modifier = modifier.fillMaxSize()) {
        if (playlists.isEmpty()) {
            // Empty state with "Create Playlist" button
            EmptyPlaylistsState(
                onCreatePlaylist = onCreatePlaylist,
                currentLanguage
            )
        } else {
            // Playlist grid
            LazyVerticalGrid(
                columns = GridCells.Adaptive(160.dp),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(playlists) { playlist ->
                    PlaylistCard(
                        playlist = playlist,
                        context = context,
                        currentLanguage = currentLanguage,
                        onPlaylistClick = {
                            navController.navigate(
                                Routes.PlaylistDetail.createRoute(playlist.id)
                            )
                        },
                        onPlayClick = {
                            if (playlist.songs.isNotEmpty()) {
                                musicViewModel.playSong(
                                    playlist.songs.first(),
                                    playlist.songs
                                )
                            }
                        },
                        onDeleteClick = {
                            playlistViewModel.deletePlaylist(playlist.id)
                        }
                    )
                }
            }
        }
    }
}


@Composable
fun EmptyPlaylistsState(
    onCreatePlaylist: () -> Unit,
    currentLanguage: String,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            painter = painterResource(R.drawable.music_playlist),
            contentDescription = null,
            modifier = Modifier
                .size(80.dp)
                .alpha(0.6f),
            tint = colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = stringResource(R.string.no_playlists_yet),
            style = MaterialTheme.typography.headlineSmall,
            color = colorScheme.onSurfaceVariant
        )

        Text(
            text = stringResource(R.string.create_first_playlist),
            style = MaterialTheme.typography.bodyMedium,
            color = colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .padding(top = 8.dp)
                .offset(
                    y = if (currentLanguage == LocaleManager.Language.ARABIC.code) (-6).dp else 0.dp
                )
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onCreatePlaylist,
            modifier = Modifier.fillMaxWidth(0.6f),
            colors = ButtonDefaults.buttonColors(
                containerColor = colorScheme.secondary,
                contentColor = Color.Black
            )
        ) {
            Icon(
                painterResource(R.drawable.addplaylist),
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(stringResource(R.string.create_playlist))
        }
    }
}


@Composable
fun PlaylistCard(
    playlist: Playlist,
    context: Context,
    currentLanguage: String,
    onPlaylistClick: () -> Unit,
    onPlayClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(16.dp))
            .background(if (isSystemInDarkTheme()) colorScheme.surfaceVariant else Color.LightGray)
            .border(
                width = 1.dp,
                color = colorScheme.outline.copy(0.5f),
                shape = RoundedCornerShape(16.dp)
            )
            .clickable { onPlaylistClick() }
    ) {
        AsyncImage(
            model = playlist.songs.firstOrNull()?.albumArtUri,
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Dark overlay for better text contrast
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.3f))
        )

        // Menu button in top-right corner
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(8.dp)
        ) {
            IconButton(
                onClick = { showMenu = true }, modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable.more_vert),
                    contentDescription = "More options",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }

            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false }
            ) {
                DropdownMenuItem(
                    text = {
                        Text(stringResource(R.string.delete_playlist))
                    },
                    onClick = {
                        onDeleteClick()
                        showMenu = false
                    },
                    leadingIcon = {
                        Icon(
                            painter = painterResource(R.drawable.deletefromplaylist),
                            contentDescription = null,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                )
            }
        }

        // Center content - playlist name and song count
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = playlist.name,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = Color.White,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            val songCountText = if (playlist.songs.size == 1) {
                context.getString(R.string.one_song)
            } else {
                val localizedCount = playlist.songs.size.toLocalizedDigits(currentLanguage)
                context.getString(R.string.multiple_songs, localizedCount)
            }

            Text(
                text = songCountText,
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier.offset(
                    y = if (currentLanguage == LocaleManager.Language.ARABIC.code) (-6).dp else 0.dp
                )
            )
        }

        // Play button at bottom
        if (playlist.songs.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
            ) {
                PlayButton(
                    paddingValues = PaddingValues(8.dp),
                    onPlayClick = onPlayClick
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePlaylistDialog(
    onDismiss: () -> Unit,
    onCreatePlaylist: (String, String?, List<Song>) -> Unit,
    musicViewModel: MusicViewModel,
    currentLanguage: String,
    preselectedSongs: List<Song> = emptyList(),
    context: Context,
) {
    var playlistName by remember { mutableStateOf("") }
    var playlistDescription by remember { mutableStateOf("") }
    var showSongSelection by remember { mutableStateOf(false) }
    var selectedSongs by remember {
        mutableStateOf(preselectedSongs.map { it.id }.toSet())
    }

    LaunchedEffect(preselectedSongs) {
        selectedSongs = preselectedSongs.map { it.id }.toSet()
    }

    val focusRequester = remember { FocusRequester() }
    val allSongs by musicViewModel.songs.collectAsStateWithLifecycle()

    // Animated dialog with custom styling
    if (!showSongSelection) {
        Dialog(
            onDismissRequest = onDismiss,
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(
                    containerColor = colorScheme.background
                ), elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(top = 24.dp, start = 24.dp, end = 24.dp, bottom = 12.dp)
                ) {
                    // Header Section with gradient background
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(
                                        colorScheme.tertiary.copy(alpha = 0.2f),
                                        colorScheme.secondary.copy(alpha = 0.2f)
                                    )
                                ),
                                shape = RoundedCornerShape(16.dp)
                            )
                            .padding(20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // Circle icon
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .background(
                                        brush = Brush.radialGradient(
                                            colors = listOf(
                                                colorScheme.primary,
                                                colorScheme.secondary
                                            )
                                        ),
                                        shape = CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.addplaylist),
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(32.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Title
                            Text(
                                text = stringResource(R.string.create_new_playlist),
                                style = MaterialTheme.typography.headlineSmall.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = colorScheme.onSurface,
                                textAlign = TextAlign.Center, // ✅ force center alignment of text
                                modifier = Modifier.fillMaxWidth()
                            )

                            // Subtitle
                            Text(
                                text = stringResource(R.string.playlist_creation_subtitle),
                                style = MaterialTheme.typography.bodyMedium,
                                color = colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center, // ✅ also centered for both directions
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 4.dp)
                                    .offset(y = if (currentLanguage == LocaleManager.Language.ARABIC.code) (-6).dp else 0.dp)

                            )
                        }
                    }


                    Spacer(modifier = Modifier.height(16.dp))

                    // Enhanced Text Fields
                    Column(
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Playlist Name Field
                        Column {
                            Text(
                                text = stringResource(R.string.playlist_name_label),
                                style = MaterialTheme.typography.labelLarge,
                                color = colorScheme.secondary,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )

                            OutlinedTextField(
                                value = playlistName,
                                onValueChange = { playlistName = it },
                                leadingIcon = {
                                    Icon(
                                        painter = painterResource(R.drawable.title),
                                        contentDescription = null,
                                        tint = colorScheme.onSurface.copy(0.7f),
                                        modifier = Modifier.size(22.dp)
                                    )
                                },
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .focusRequester(focusRequester),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = colorScheme.secondary,
                                    focusedLabelColor = colorScheme.secondary,
                                    focusedLeadingIconColor = colorScheme.secondary,
                                    cursorColor = colorScheme.secondary
                                ),
                                shape = RoundedCornerShape(16.dp)
                            )
                        }

                        // Description Field
                        Column {
                            Text(
                                text = stringResource(R.string.playlist_description_label),
                                style = MaterialTheme.typography.labelLarge,
                                color = colorScheme.tertiary,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )

                            OutlinedTextField(
                                value = playlistDescription,
                                onValueChange = { playlistDescription = it },
                                leadingIcon = {
                                    Icon(
                                        painter = painterResource(R.drawable.description),
                                        contentDescription = null,
                                        tint = colorScheme.onSurface.copy(0.7f),
                                        modifier = Modifier.size(22.dp)
                                    )
                                },
                                maxLines = 3,
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = colorScheme.tertiary,
                                    focusedLabelColor = colorScheme.tertiary,
                                    focusedLeadingIconColor = colorScheme.tertiary,
                                    cursorColor = colorScheme.tertiary
                                ),
                                shape = RoundedCornerShape(16.dp)
                            )
                        }
                    }

                    // Selected songs preview with better styling
                    AnimatedVisibility(
                        visible = selectedSongs.isNotEmpty(),
                        enter = slideInVertically() + fadeIn(),
                        exit = slideOutVertically() + fadeOut(),
                        modifier = Modifier.padding(top = 20.dp)
                    ) {
                        Card(
                            modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(
                                containerColor = colorScheme.tertiaryContainer.copy(
                                    alpha = 0.4f
                                )
                            ), shape = RoundedCornerShape(16.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .background(
                                            colorScheme.tertiary.copy(alpha = 0.2f),
                                            CircleShape
                                        ), contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.music_playlist),
                                        contentDescription = null,
                                        tint = colorScheme.tertiary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column {
                                    Text(
                                        text = pluralStringResource(
                                            id = R.plurals.songs_selected,
                                            count = selectedSongs.size,
                                            selectedSongs.size
                                        ),
                                        style = MaterialTheme.typography.titleSmall,
                                        color = colorScheme.onPrimaryContainer,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = stringResource(R.string.ready_to_create_playlist),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = colorScheme.onPrimaryContainer.copy(
                                            alpha = 0.7f
                                        )
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Action Buttons with enhanced styling
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Cancel Button
                        OutlinedButton(
                            onClick = onDismiss,
                            modifier = Modifier.weight(1.1f),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = colorScheme.onSurfaceVariant
                            ),
                            border = BorderStroke(
                                1.dp, colorScheme.outline.copy(alpha = 0.5f)
                            ),
                        ) {
                            Text(
                                stringResource(R.string.cancel),
                                style = MaterialTheme.typography.labelLarge,
                                maxLines = 1,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }

                        Button(
                            onClick = {
                                    showSongSelection = true
                            },
                            modifier = Modifier.weight(1.9f),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colorScheme.secondary,
                                contentColor = Color.Black
                            ),
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(vertical = 4.dp)
                            ) {
                                Icon(
                                    painter = if (selectedSongs.isEmpty()) painterResource(R.drawable.playlist_add)
                                    else painterResource(R.drawable.edit),
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp),
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (selectedSongs.isEmpty())
                                        stringResource(R.string.add_songs)
                                    else
                                        stringResource(R.string.edit_songs),
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Medium,
                                )
                            }
                        }
                    }
                        Spacer(modifier = Modifier.height(8.dp))

                        TextButton(
                            onClick = {
                                onCreatePlaylist(
                                    playlistName.trim(),
                                    playlistDescription.trim().takeIf { it.isNotEmpty() },
                                    selectedSongs.mapNotNull { id -> allSongs.find { it.id == id } }
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = playlistName.isNotBlank(),
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = colorScheme.tertiary
                            )
                        ) {
                            Text(
                                text = if (selectedSongs.isEmpty())
                                    stringResource(R.string.create_empty_playlist)
                                else
                                    pluralStringResource(
                                        id = R.plurals.create_playlist_with_songs,
                                        count = selectedSongs.size,
                                        selectedSongs.size
                                    ),
                                style = MaterialTheme.typography.labelLarge,
                            )
                        }

                }
            }
        }
    }

    // Song selection bottom sheet using the unified component
    if (showSongSelection) {
        PlaylistBottomSheet(
            sheetState = PlaylistSheetState(
                mode = PlaylistSheetMode.CREATE,
                playlistName = playlistName,
                playlistDescription = playlistDescription,
                selectedSongs = selectedSongs
            ),
            songs = allSongs,
            allSongs = allSongs,
            onDismiss = { showSongSelection = false },
            onCreatePlaylist = { name, description, songIds ->
                val songsToAdd = allSongs.filter { songIds.contains(it.id) }
                onCreatePlaylist(
                    name,
                    description,
                    songsToAdd
                )
            },
            onAddSongsToPlaylist = { _, _ -> },
            onEditPlaylist = { _, _, _ -> },
            onSelectedSongsChange = { newSelection ->
                selectedSongs = newSelection
            },
            context = context
        )
    }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
}
