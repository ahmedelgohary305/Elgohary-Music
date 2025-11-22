package com.example.elgoharymusic.presentation.utils

import android.content.Context
import android.net.Uri
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.elgoharymusic.R
import com.example.elgoharymusic.domain.models.Song

enum class PlaylistSheetMode {
    CREATE,
    ADD_SONGS,
    EDIT
}

// Data class to hold sheet state
data class PlaylistSheetState(
    val mode: PlaylistSheetMode,
    val playlistId: Long? = null,
    val playlistName: String = "",
    val playlistDescription: String = "",
    val selectedSongs: Set<Long> = emptySet()
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistBottomSheet(
    sheetState: PlaylistSheetState,
    songs: List<Song>,
    allSongs: List<Song> = emptyList(),
    onDismiss: () -> Unit,
    context: Context,
    onCreatePlaylist: (String, String?, Set<Long>) -> Unit,
    onAddSongsToPlaylist: (Long, Set<Long>) -> Unit,
    onEditPlaylist: (Long, String, String?) -> Unit,
    onSelectedSongsChange: (Set<Long>) -> Unit
) {
    val bottomSheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )

    // Determine which songs to show based on mode
    val displaySongs = when (sheetState.mode) {
        PlaylistSheetMode.CREATE -> allSongs
        PlaylistSheetMode.ADD_SONGS -> songs // These should be filtered songs not in playlist
        PlaylistSheetMode.EDIT -> emptyList() // No songs needed for edit mode
    }

    // State for edit mode
    var editName by remember(sheetState) { mutableStateOf(sheetState.playlistName) }
    var editDescription by remember(sheetState) { mutableStateOf(sheetState.playlistDescription) }

    // Search state
    var searchQuery by remember { mutableStateOf("") }
    var isSearchActive by remember { mutableStateOf(false) }

    // Filter songs based on search query
    val filteredSongs = remember(displaySongs, searchQuery) {
        if (searchQuery.isBlank()) {
            displaySongs
        } else {
            displaySongs.filter { song ->
                song.title.contains(searchQuery, ignoreCase = true) ||
                        song.artist.contains(searchQuery, ignoreCase = true) ||
                        song.album?.contains(searchQuery, ignoreCase = true) == true
            }
        }
    }

    // Focus requester for edit mode and search
    val focusRequester = remember { FocusRequester() }
    val searchFocusRequester = remember { FocusRequester() }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = bottomSheetState,
        containerColor = colorScheme.background,
        contentColor = colorScheme.onBackground,
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize() // Fill entire screen
                .windowInsetsPadding(WindowInsets.systemBars)
                .padding(start = 8.dp, end = 8.dp, top = 24.dp)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isSearchActive && sheetState.mode != PlaylistSheetMode.EDIT) {
                    // Search field in header
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier
                            .weight(1f)
                            .focusRequester(searchFocusRequester),
                        placeholder = { Text(stringResource(R.string.search_songs)) },
                        leadingIcon = {
                            Icon(
                                painter = painterResource(R.drawable.search),
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                                tint = colorScheme.onSurfaceVariant
                            )
                        },
                        trailingIcon = {
                            IconButton(
                                onClick = {
                                    searchQuery = ""
                                    isSearchActive = false
                                }
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.close),
                                    contentDescription = "Close search",
                                    modifier = Modifier.size(20.dp),
                                    tint = colorScheme.onSurfaceVariant
                                )
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = colorScheme.secondary,
                            cursorColor = colorScheme.secondary
                        )
                    )

                    LaunchedEffect(Unit) {
                        searchFocusRequester.requestFocus()
                    }
                } else {
                    // Normal header content
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = when (sheetState.mode) {
                                PlaylistSheetMode.CREATE -> stringResource(R.string.create_playlist)
                                PlaylistSheetMode.ADD_SONGS -> stringResource(R.string.add_songs_action)
                                PlaylistSheetMode.EDIT -> stringResource(R.string.edit_playlist)
                            },
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )

                        when (sheetState.mode) {
                            PlaylistSheetMode.ADD_SONGS -> {
                                Text(
                                    text = stringResource(
                                        R.string.to_playlist,
                                        sheetState.playlistName
                                    ),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = colorScheme.onSurfaceVariant
                                )
                            }
                            PlaylistSheetMode.CREATE -> {
                                Text(
                                    text = stringResource(R.string.choose_songs_and_enter_details),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = colorScheme.onSurfaceVariant
                                )
                            }
                            PlaylistSheetMode.EDIT -> {
                                Text(
                                    text = stringResource(R.string.update_playlist_info),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Search icon (only show in CREATE and ADD_SONGS modes)
                        if (sheetState.mode != PlaylistSheetMode.EDIT && displaySongs.isNotEmpty()) {
                            IconButton(
                                onClick = { isSearchActive = true }
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.search),
                                    contentDescription = "Search songs",
                                    modifier = Modifier.size(24.dp),
                                    tint = colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // Selected songs badge
                        if (sheetState.selectedSongs.isNotEmpty() && sheetState.mode != PlaylistSheetMode.EDIT) {
                            Surface(
                                color = colorScheme.secondaryContainer.copy(alpha = 0.3f),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Text(
                                    text = stringResource(
                                        R.string.selected_songs_count,
                                        sheetState.selectedSongs.size
                                    ),
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = colorScheme.onSecondaryContainer,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }

            when (sheetState.mode) {
                PlaylistSheetMode.EDIT -> {
                    // Edit form - only for actual editing, not used for song selection
                    Column(
                        modifier = Modifier.padding(vertical = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        OutlinedTextField(
                            value = editName,
                            onValueChange = { editName = it },
                            label = { Text(stringResource(R.string.playlist_name_label)) },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .focusRequester(focusRequester),
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = colorScheme.secondary,
                                cursorColor = colorScheme.secondary,
                                focusedLabelColor = colorScheme.secondary
                            )
                        )

                        OutlinedTextField(
                            value = editDescription,
                            onValueChange = { editDescription = it },
                            label = { Text(stringResource(R.string.playlist_description_label)) },
                            maxLines = 3,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = colorScheme.secondary,
                                cursorColor = colorScheme.secondary,
                                focusedLabelColor = colorScheme.secondary
                            )
                        )
                    }

                    LaunchedEffect(Unit) {
                        focusRequester.requestFocus()
                    }
                }

                else -> {
                    // Song selection for both CREATE and ADD_SONGS modes
                    SongSelectionContent(
                        songs = filteredSongs,
                        selectedSongs = sheetState.selectedSongs,
                        onSelectedSongsChange = onSelectedSongsChange,
                        modifier = Modifier
                            .weight(1f) // Take remaining space
                            .fillMaxWidth(),
                        searchQuery = searchQuery,
                        context = context
                    )
                }
            }

            // Bottom action buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = colorScheme.onBackground.copy(0.8f)
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(stringResource(R.string.cancel))
                }

                Button(
                    onClick = {
                        when (sheetState.mode) {
                            PlaylistSheetMode.CREATE -> {
                                if (editName.isNotBlank()) {
                                    onCreatePlaylist(
                                        editName.trim(),
                                        editDescription.trim().takeIf { it.isNotEmpty() },
                                        sheetState.selectedSongs
                                    )
                                }
                            }
                            PlaylistSheetMode.ADD_SONGS -> {
                                sheetState.playlistId?.let { playlistId ->
                                    onAddSongsToPlaylist(playlistId, sheetState.selectedSongs)
                                }
                            }
                            PlaylistSheetMode.EDIT -> {
                                if (editName.isNotBlank()) {
                                    sheetState.playlistId?.let { playlistId ->
                                        onEditPlaylist(
                                            playlistId,
                                            editName.trim(),
                                            editDescription.trim().takeIf { it.isNotEmpty() }
                                        )
                                    }
                                }
                            }
                        }
                    },
                    modifier = Modifier.weight(2f),
                    enabled = when (sheetState.mode) {
                        PlaylistSheetMode.EDIT, PlaylistSheetMode.CREATE -> editName.isNotBlank()
                        PlaylistSheetMode.ADD_SONGS -> sheetState.selectedSongs.isNotEmpty()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorScheme.secondary,
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(
                        painter = painterResource(
                            when (sheetState.mode) {
                                PlaylistSheetMode.CREATE -> R.drawable.playlist_add
                                PlaylistSheetMode.ADD_SONGS -> R.drawable.addplaylist
                                PlaylistSheetMode.EDIT -> R.drawable.edit
                            }
                        ),
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = when (sheetState.mode) {
                            PlaylistSheetMode.CREATE -> {
                                if (sheetState.selectedSongs.isEmpty())
                                    stringResource(R.string.create_playlist_action)
                                else
                                    stringResource(R.string.create_playlist_action_count, sheetState.selectedSongs.size)
                            }
                            PlaylistSheetMode.ADD_SONGS -> {
                                if (sheetState.selectedSongs.isEmpty())
                                    stringResource(R.string.add_songs_action)
                                else
                                    stringResource(R.string.add_songs_action_count, sheetState.selectedSongs.size)
                            }
                            PlaylistSheetMode.EDIT -> stringResource(R.string.save_changes)
                        }
                    )

                }
            }
        }
    }
}

@Composable
private fun SongSelectionContent(
    modifier: Modifier = Modifier,
    songs: List<Song>,
    selectedSongs: Set<Long>,
    context: Context,
    onSelectedSongsChange: (Set<Long>) -> Unit,
    searchQuery: String = ""
) {
    Column(modifier = modifier) {
        // Action buttons for song selection
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (selectedSongs.isNotEmpty()) {
                OutlinedButton(
                    onClick = { onSelectedSongsChange(emptySet()) },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = colorScheme.tertiary
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(stringResource(R.string.clear_all))
                }

                OutlinedButton(
                    onClick = {
                        onSelectedSongsChange(songs.map { it.id }.toSet())
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = colorScheme.tertiary
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(stringResource(R.string.select_all))
                }
            } else {
                OutlinedButton(
                    onClick = {
                        onSelectedSongsChange(songs.map { it.id }.toSet())
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = colorScheme.tertiary
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.select_all),
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.select_all))
                }
            }
        }

        // Songs list
        LazyColumn(
            modifier = modifier.then(
                if (songs.isEmpty()) Modifier else Modifier.heightIn(max = 300.dp)
            ),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            if (songs.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = colorScheme.surfaceVariant.copy(alpha = 0.3f)
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                painter = painterResource(
                                    if (searchQuery.isNotEmpty()) R.drawable.search
                                    else R.drawable.song
                                ),
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = if (searchQuery.isNotEmpty()) "No songs found"
                                else "No songs available",
                                style = MaterialTheme.typography.titleMedium,
                                color = colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = if (searchQuery.isNotEmpty())
                                    "Try searching with different keywords"
                                else "Add some music to your device first",
                                style = MaterialTheme.typography.bodyMedium,
                                color = colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                items(songs) { song ->
                    SongSelectionItem(
                        song = song,
                        isSelected = selectedSongs.contains(song.id),
                        onSelectionChange = { isSelected ->
                            onSelectedSongsChange(
                                if (isSelected) {
                                    selectedSongs + song.id
                                } else {
                                    selectedSongs - song.id
                                }
                            )
                        },
                        context = context
                    )
                }
            }
        }
    }
}

@Composable
fun SongSelectionItem(
    song: Song,
    isSelected: Boolean,
    context: Context,
    onSelectionChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    // Animate background color
    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) {
            colorScheme.tertiaryContainer.copy(alpha = 0.5f)
        } else {
            Color.Transparent
        },
        animationSpec = tween(300, easing = FastOutSlowInEasing),
        label = "backgroundColor"
    )

    // Animate text colors
    val primaryTextColor by animateColorAsState(
        targetValue = if (isSelected) {
            colorScheme.onTertiaryContainer
        } else {
            colorScheme.onBackground
        },
        animationSpec = tween(200),
        label = "primaryTextColor"
    )

    val secondaryTextColor by animateColorAsState(
        targetValue = if (isSelected) {
            colorScheme.onTertiaryContainer.copy(alpha = 0.7f)
        } else {
            colorScheme.onBackground.copy(alpha = 0.7f)
        },
        animationSpec = tween(200),
        label = "secondaryTextColor"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onSelectionChange(!isSelected) },
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CustomCheckbox(
                checked = isSelected,
                onCheckedChange = onSelectionChange,
                size = 24.dp,
                cornerRadius = 6.dp,
                animationDuration = 200
            )

            Spacer(modifier = Modifier.width(12.dp))

            Surface(
                modifier = Modifier
                    .size(56.dp),
                shape = RoundedCornerShape(12.dp),
                color = Color.DarkGray,
                shadowElevation = if (isSelected) 2.dp else 0.dp
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    ComponentImage(
                        data = song.albumArtUri ?: Uri.EMPTY,
                        iconId = R.drawable.song,
                        crossfadeDuration = 300,
                        modifier = Modifier.size(28.dp),
                        context = context
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = song.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = primaryTextColor
                )
                Text(
                    text = "${song.artist} • ${song.album}",
                    style = MaterialTheme.typography.bodySmall,
                    color = secondaryTextColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun CustomCheckbox(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 24.dp,
    cornerRadius: Dp = 6.dp,
    animationDuration: Int = 200
) {
    val checkboxColor by animateColorAsState(
        targetValue = if (checked) colorScheme.tertiary else Color.Transparent,
        animationSpec = tween(durationMillis = animationDuration),
        label = "checkboxColor"
    )

    val borderColor by animateColorAsState(
        targetValue = if (checked) colorScheme.tertiary else colorScheme.outline,
        animationSpec = tween(durationMillis = animationDuration),
        label = "borderColor"
    )

    val checkmarkAlpha by animateFloatAsState(
        targetValue = if (checked) 1f else 0f,
        animationSpec = tween(durationMillis = animationDuration),
        label = "checkmarkAlpha"
    )

    val scale by animateFloatAsState(
        targetValue = if (checked) 1f else 0.8f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessHigh
        ),
        label = "scale"
    )

    Box(
        modifier = modifier
            .size(size)
            .scale(scale)
            .clip(RoundedCornerShape(cornerRadius))
            .background(checkboxColor)
            .border(
                width = 2.dp,
                color = borderColor,
                shape = RoundedCornerShape(cornerRadius)
            )
            .clickable {
                onCheckedChange(!checked)
            },
        contentAlignment = Alignment.Center
    ) {
        // Animated checkmark
        Icon(
            painter = painterResource(R.drawable.check),
            contentDescription = null,
            tint = Color.Black,
            modifier = Modifier
                .size(size * 0.6f)
                .alpha(checkmarkAlpha)
                .scale(checkmarkAlpha),
        )
    }
}
