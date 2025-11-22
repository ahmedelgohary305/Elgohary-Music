package com.example.elgoharymusic.presentation.utils

import android.app.Activity
import android.content.Context
import android.widget.Toast
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.elgoharymusic.R
import com.example.elgoharymusic.domain.models.Song
import com.example.elgoharymusic.presentation.viewmodels.FavViewModel
import com.example.elgoharymusic.presentation.viewmodels.MusicViewModel
import com.example.elgoharymusic.presentation.viewmodels.PlaylistViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun SongDialogsHandler(
    songToEdit: Song?,
    songsToDelete: List<Song>,
    onEditDismiss: () -> Unit,
    onDeleteDismiss: () -> Unit,
    musicViewModel: MusicViewModel,
    favViewModel: FavViewModel,
    playlistViewModel: PlaylistViewModel,
    coroutineScope: CoroutineScope,
    context: Context,
    onSongEdited: ((Long, String) -> Unit)? = null,
    useArtistForCallback: Boolean = false // Determines whether callback receives artist or album name
) {
    val intentSenderLauncher = songOperationPermissionHandler(
        musicViewModel = musicViewModel,
        favViewModel = favViewModel,
        playlistViewModel = playlistViewModel,
        coroutineScope = coroutineScope,
        context = context,
        onSongEdited = onSongEdited
    )

    // Edit dialog
    songToEdit?.let { song ->
        EditSongDialog(
            song = song,
            musicViewModel = musicViewModel,
            playlistViewModel = playlistViewModel,
            favViewModel = favViewModel,
            coroutineScope = coroutineScope,
            context = context,
            intentSenderLauncher = intentSenderLauncher,
            onDismiss = onEditDismiss,
            onSongEdited = onSongEdited,
            useArtistForCallback = useArtistForCallback
        )
    }

    // Delete dialog
    if (songsToDelete.isNotEmpty()) {
        DeleteSongsDialog(
            songs = songsToDelete,
            musicViewModel = musicViewModel,
            favViewModel = favViewModel,
            playlistViewModel = playlistViewModel,
            coroutineScope = coroutineScope,
            context = context,
            intentSenderLauncher = intentSenderLauncher,
            onDismiss = onDeleteDismiss
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditSongDialog(
    song: Song,
    musicViewModel: MusicViewModel,
    playlistViewModel: PlaylistViewModel,
    favViewModel: FavViewModel,
    coroutineScope: CoroutineScope,
    context: Context,
    intentSenderLauncher: ManagedActivityResultLauncher<IntentSenderRequest, ActivityResult>,
    onDismiss: () -> Unit,
    onSongEdited: ((Long, String) -> Unit)? = null,
    useArtistForCallback: Boolean = false
) {
    var title by remember { mutableStateOf(song.title) }
    var artist by remember { mutableStateOf(song.artist) }
    var album by remember { mutableStateOf(song.album ?: "") }
    val focusRequester = remember { FocusRequester() }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
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
                    text = stringResource(R.string.edit_song),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                // Input Fields
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text(stringResource(R.string.song_title)) },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(focusRequester),
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.secondary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                            cursorColor = MaterialTheme.colorScheme.secondary,
                            focusedLabelColor = MaterialTheme.colorScheme.secondary,
                        )
                    )

                    OutlinedTextField(
                        value = artist,
                        onValueChange = { artist = it },
                        label = { Text(stringResource(R.string.artist)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.secondary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                            cursorColor = MaterialTheme.colorScheme.secondary,
                            focusedLabelColor = MaterialTheme.colorScheme.secondary,
                        )
                    )

                    OutlinedTextField(
                        value = album,
                        onValueChange = { album = it },
                        label = { Text(stringResource(R.string.album_optional)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.secondary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                            cursorColor = MaterialTheme.colorScheme.secondary,
                            focusedLabelColor = MaterialTheme.colorScheme.secondary,
                        )
                    )
                }

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
                            contentColor = MaterialTheme.colorScheme.onBackground.copy(0.8f)
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(stringResource(R.string.cancel))
                    }

                    Button(
                        onClick = {
                            if (title.isNotBlank() && artist.isNotBlank()) {
                                coroutineScope.launch {
                                    try {
                                        val newAlbumName = album.trim().takeIf { it.isNotEmpty() } ?: "Unknown Album"
                                        val newArtistName = artist.trim()

                                        val intentSender = musicViewModel.updateSongMetadata(
                                            song.id,
                                            title.trim(),
                                            newArtistName,
                                            newAlbumName
                                        )

                                        if (intentSender != null) {
                                            // Need user approval (Android 10+)
                                            EditSongPermissionHandler.pendingEditParams = EditParams(
                                                songId = song.id,
                                                title = title.trim(),
                                                artist = newArtistName,
                                                album = newAlbumName
                                            )
                                            // Store the appropriate value for callback based on the flag
                                            EditSongPermissionHandler.pendingCallbackValue =
                                                if (useArtistForCallback) newArtistName else newAlbumName

                                            intentSenderLauncher.launch(
                                                IntentSenderRequest.Builder(intentSender).build()
                                            )
                                            onDismiss()
                                        } else {
                                            // Success - update metadata in playlists and favorites
                                            playlistViewModel.updateSongMetadataInAllPlaylists(
                                                song.id,
                                                title.trim(),
                                                newArtistName,
                                                newAlbumName
                                            )
                                            favViewModel.updateSongMetadataInFavorites(
                                                song.id,
                                                title.trim(),
                                                newArtistName,
                                                newAlbumName
                                            )

                                            onDismiss()

                                            // Trigger navigation callback with appropriate value
                                            val callbackValue = if (useArtistForCallback) newArtistName else newAlbumName
                                            onSongEdited?.invoke(song.id, callbackValue)
                                        }
                                    } catch (_: SecurityException) {
                                        Toast.makeText(context, context.getString(R.string.permission_denied), Toast.LENGTH_LONG).show()
                                        onDismiss()
                                    } catch (e: Exception) {
                                        Toast.makeText(context, context.getString(R.string.failed_to_update_song), Toast.LENGTH_SHORT).show()
                                        onDismiss()
                                    }
                                }
                            }
                        },
                        enabled = title.isNotBlank() && artist.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary,
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(stringResource(R.string.save))
                    }
                }

                LaunchedEffect(Unit) {
                    focusRequester.requestFocus()
                }
            }
        }
    }
}

@Composable
fun DeleteSongsDialog(
    songs: List<Song>,
    musicViewModel: MusicViewModel,
    favViewModel: FavViewModel,
    playlistViewModel: PlaylistViewModel,
    coroutineScope: CoroutineScope,
    context: Context,
    intentSenderLauncher: ManagedActivityResultLauncher<IntentSenderRequest, ActivityResult>,
    onDismiss: () -> Unit
) {
    val isSingle = songs.size == 1
    val song = songs.firstOrNull()
    val songCount = songs.size

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
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
                    text = pluralStringResource(
                        R.plurals.delete_song_dialog,
                        songCount,
                        songCount
                    ),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Content
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = pluralStringResource(
                            R.plurals.are_you_sure_you_want_to_delete_this_song,
                            songCount,
                            songCount
                        ),
                        style = MaterialTheme.typography.bodyLarge
                    )

                    if (isSingle && song != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = stringResource(R.string.song_by_artist, song.title, song.artist),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontStyle = FontStyle.Italic
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.this_action_cannot_be_undone),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
                    )
                }

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
                            contentColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(text = stringResource(R.string.cancel))
                    }

                    Button(
                        onClick = {
                            coroutineScope.launch {
                                try {
                                    val intentSender = musicViewModel.deleteSongs(songs)

                                    if (intentSender != null) {
                                        // Need user approval - store pending songs
                                        DeleteSongPermissionHandler.pendingSongs = songs
                                        intentSenderLauncher.launch(
                                            IntentSenderRequest.Builder(intentSender).build()
                                        )
                                        onDismiss()
                                    } else {
                                        // Success - cleanup
                                        songs.forEach { favViewModel.removeFromFavorites(it) }
                                        playlistViewModel.removeSongsFromAllPlaylists(songs.map { it.id })

                                        // Refresh current playlist if exists
                                        playlistViewModel.currentPlaylist.value?.id?.let { currentId ->
                                            playlistViewModel.loadPlaylistWithSongs(currentId)
                                        }

                                        val message = if (isSingle)
                                            context.getString(R.string.song_deleted_success)
                                        else
                                            context.getString(R.string.songs_deleted_success, songCount)

                                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                                        onDismiss()
                                    }
                                } catch (_: SecurityException) {
                                    Toast.makeText(
                                        context,
                                        context.getString(R.string.permission_denied),
                                        Toast.LENGTH_LONG
                                    ).show()
                                    onDismiss()
                                } catch (e: Exception) {
                                    val message = if (isSingle)
                                        context.getString(R.string.failed_to_delete_song, e.localizedMessage ?: "")
                                    else
                                        context.getString(R.string.failed_to_delete_songs, e.localizedMessage ?: "")
                                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                                    onDismiss()
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        ),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(text = stringResource(if (isSingle) R.string.delete else R.string.delete_all))
                    }
                }
            }
        }
    }
}

// Permission handlers to store pending operations
object EditSongPermissionHandler {
    var pendingEditParams: EditParams? = null
    var pendingCallbackValue: String? = null // Stores either artist or album name for navigation
}

object DeleteSongPermissionHandler {
    var pendingSongs: List<Song>? = null
}

data class EditParams(
    val songId: Long,
    val title: String,
    val artist: String,
    val album: String?
)

// Composable to handle permission results
@Composable
fun songOperationPermissionHandler(
    musicViewModel: MusicViewModel,
    favViewModel: FavViewModel,
    playlistViewModel: PlaylistViewModel,
    coroutineScope: CoroutineScope,
    context: Context,
    onSongEdited: ((Long, String) -> Unit)? = null
): ManagedActivityResultLauncher<IntentSenderRequest, ActivityResult> {
    return rememberLauncherForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            // Handle edit permission result
            EditSongPermissionHandler.pendingEditParams?.let { params ->
                coroutineScope.launch {
                    try {
                        val intentSender = musicViewModel.updateSongMetadata(
                            params.songId,
                            params.title,
                            params.artist,
                            params.album
                        )

                        if (intentSender == null) {
                            // Update metadata in playlists and favorites
                            playlistViewModel.updateSongMetadataInAllPlaylists(
                                params.songId,
                                params.title,
                                params.artist,
                                params.album
                            )
                            favViewModel.updateSongMetadataInFavorites(
                                params.songId,
                                params.title,
                                params.artist,
                                params.album
                            )

                            Toast.makeText(context, "Song updated successfully", Toast.LENGTH_SHORT).show()

                            // Trigger navigation callback with the stored value
                            EditSongPermissionHandler.pendingCallbackValue?.let { callbackValue ->
                                onSongEdited?.invoke(params.songId, callbackValue)
                            }
                        }
                    } catch (e: Exception) {
                        Toast.makeText(context, "Failed to update: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                    } finally {
                        // Cleanup
                        EditSongPermissionHandler.pendingEditParams = null
                        EditSongPermissionHandler.pendingCallbackValue = null
                    }
                }
            }

            // Handle delete permission result
            DeleteSongPermissionHandler.pendingSongs?.let { songs ->
                coroutineScope.launch {
                    try {
                        val intentSender = musicViewModel.deleteSongs(songs)
                        if (intentSender == null) {
                            // Cleanup after successful deletion
                            songs.forEach { favViewModel.removeFromFavorites(it) }
                            playlistViewModel.removeSongsFromAllPlaylists(songs.map { it.id })

                            Toast.makeText(context, "${songs.size} song(s) deleted successfully", Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        Toast.makeText(context, "Failed to delete: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                    } finally {
                        DeleteSongPermissionHandler.pendingSongs = null
                    }
                }
            }
        } else {
            // User denied permission - cleanup
            EditSongPermissionHandler.pendingEditParams = null
            EditSongPermissionHandler.pendingCallbackValue = null
            DeleteSongPermissionHandler.pendingSongs = null
        }
    }
}

// Dialog state management
class SongDialogsState {
    var songToEdit by mutableStateOf<Song?>(null)
    var songsToDelete by mutableStateOf<List<Song>>(emptyList())

    fun showEditDialog(song: Song) {
        songToEdit = song
    }

    fun showDeleteDialog(songs: List<Song>) {
        songsToDelete = songs
    }

    fun dismissEdit() {
        songToEdit = null
    }

    fun dismissDelete() {
        songsToDelete = emptyList()
    }
}

@Composable
fun rememberSongDialogsState(): SongDialogsState {
    return remember { SongDialogsState() }
}