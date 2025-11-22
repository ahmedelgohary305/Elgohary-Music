package com.example.elgoharymusic.presentation.utils

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Badge
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.elgoharymusic.R
import com.example.elgoharymusic.data.repoImpl.AppLanguage
import com.example.elgoharymusic.domain.models.Song
import com.example.elgoharymusic.presentation.screens.NoRtl
import com.example.elgoharymusic.presentation.viewmodels.FavViewModel
import com.example.elgoharymusic.presentation.viewmodels.MusicViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

@Composable
fun BottomContent(
    songs: List<Song>,
    currentLanguage: AppLanguage,
    isSelectionMode: Boolean,
    musicViewModel: MusicViewModel,
    favViewModel: FavViewModel,
    coroutineScope: CoroutineScope,
    currentSong: Song?,
    isPlaying: Boolean,
    queueSongs: List<Song>,
    colorScheme: ColorScheme,
    context: Context,
    onNavigateToPlayer: () -> Unit,
    onAddToPlaylist: (List<Song>) -> Unit,
    onDeleteSelected: (List<Song>) -> Unit
) {
    // Get the actual selected song IDs
    val selectedSongIds by musicViewModel.selectedSongs.collectAsStateWithLifecycle()

    // Memoize selected songs to avoid recomputation
    val selectedSongs = remember(songs, selectedSongIds) {
        songs.filter { it.id in selectedSongIds }
    }

    // Determine visibility based on state
    val isVisible = isSelectionMode || currentSong != null

    AnimatedVisibility(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        visible = isVisible,
        enter = slideInVertically(
            initialOffsetY = { it },
            animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing)
        ),
        exit = slideOutVertically(
            targetOffsetY = { it },
            animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing)
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp, start = 8.dp, end = 8.dp)
        ) {
            ExpandableBottomBar(
                isExpanded = isSelectionMode,
                currentSong = currentSong,
                currentLanguage = currentLanguage,
                isPlaying = isPlaying,
                queueSongs = queueSongs,
                colorScheme = colorScheme,
                context = context,
                musicViewModel = musicViewModel,
                onNavigateToPlayer = onNavigateToPlayer,
                selectedSongs = selectedSongs,
                favViewModel = favViewModel,
                coroutineScope = coroutineScope,
                onAddToPlaylist = onAddToPlaylist,
                onDeleteSelected = onDeleteSelected,
                songs = songs
            )
        }
    }
}

@Composable
fun ExpandableBottomBar(
    isExpanded: Boolean,
    currentSong: Song?,
    isPlaying: Boolean,
    currentLanguage: AppLanguage,
    queueSongs: List<Song>,
    colorScheme: ColorScheme,
    context: Context,
    musicViewModel: MusicViewModel,
    onNavigateToPlayer: () -> Unit,
    selectedSongs: List<Song>,
    favViewModel: FavViewModel,
    coroutineScope: CoroutineScope,
    onAddToPlaylist: (List<Song>) -> Unit,
    onDeleteSelected: (List<Song>) -> Unit,
    songs: List<Song>
) {
    // Optimized rotation with LaunchedEffect and derivedStateOf
    var rotationState by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(isPlaying) {
        if (isPlaying) {
            while (isActive) {
                withFrameMillis { frameTime ->
                    rotationState = (frameTime / 55.56f) % 360f // 20 second rotation
                }
            }
        }
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium
                )
            ),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.tertiaryContainer,
    ) {
        AnimatedContent(
            targetState = isExpanded to (currentSong != null),
            modifier = Modifier.fillMaxWidth(),
            transitionSpec = {
                fadeIn(animationSpec = tween(250, delayMillis = 90)) togetherWith
                        fadeOut(animationSpec = tween(180))
            },
            label = "bottom_bar_content"
        ) { (expanded, hasSong) ->
            when {
                expanded -> {
                    SelectionActionsContent(
                        selectedSongs = selectedSongs,
                        favViewModel = favViewModel,
                        musicViewModel = musicViewModel,
                        coroutineScope = coroutineScope,
                        context = context,
                        onAddToPlaylist = onAddToPlaylist,
                        onDeleteSelected = onDeleteSelected,
                        songs = songs
                    )
                }
                hasSong -> {
                    currentSong?.let { song ->
                        MiniPlayerContent(
                            song = song,
                            currentLanguage = currentLanguage,
                            isPlaying = isPlaying,
                            queueSongs = queueSongs,
                            context = context,
                            onPlayPause = { musicViewModel.playPause() },
                            onSkipNext = { musicViewModel.skipToNext() },
                            onSkipPrevious = { musicViewModel.skipToPrevious() },
                            onPlayerClick = onNavigateToPlayer,
                            colorScheme = colorScheme,
                            musicViewModel = musicViewModel,
                            rotationState = rotationState
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MiniPlayerContent(
    song: Song,
    isPlaying: Boolean,
    currentLanguage: AppLanguage,
    queueSongs: List<Song>,
    context: Context,
    onPlayPause: () -> Unit,
    onSkipNext: () -> Unit,
    onSkipPrevious: () -> Unit,
    onPlayerClick: () -> Unit,
    colorScheme: ColorScheme,
    musicViewModel: MusicViewModel,
    rotationState: Float
) {
    var showQueueSheet by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = colorScheme.onTertiaryContainer.copy(0.2f),
                shape = RoundedCornerShape(20.dp)
            )
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) { onPlayerClick() }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 🎵 Rotating album art
        Surface(
            modifier = Modifier
                .size(45.dp)
                .rotate(rotationState),
            shape = CircleShape,
            color = Color.DarkGray,
            shadowElevation = 4.dp
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                ComponentImage(
                    data = song.albumArtUri ?: Uri.EMPTY,
                    iconId = R.drawable.song,
                    crossfadeDuration = 300,
                    modifier = Modifier.size(22.dp),
                    context = context
                )
            }
        }

        // 🎤 Song info
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 8.dp)
        ) {
            Text(
                text = song.title,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = MaterialTheme.colorScheme.onTertiaryContainer.copy(0.8f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = song.artist,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onTertiaryContainer.copy(0.6f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        // Media controls row
        PlayerControlsRow(
            isPlaying = isPlaying,
            currentLanguage = currentLanguage,
            onPlayPause = onPlayPause,
            onSkipNext = onSkipNext,
            onSkipPrevious = onSkipPrevious,
            queueSongs = queueSongs,
            onShowQueueSheetChange = { showQueueSheet = it }
        )
    }

    // Queue Bottom Sheet
    if (showQueueSheet) {
        QueueBottomSheet(
            queueSongs = queueSongs,
            currentSong = song,
            onSongClick = musicViewModel::playSongFromQueue,
            onRemoveFromQueue = musicViewModel::removeFromQueue,
            onClearQueue = musicViewModel::clearQueue,
            onReorderQueue = musicViewModel::reorderQueue,
            onDismiss = { showQueueSheet = false },
            colorScheme = colorScheme,
            context = context,
            currentLanguage = currentLanguage
        )
    }
}

@Composable
fun PlayerControlsRow(
    isPlaying: Boolean,
    currentLanguage: AppLanguage,
    onPlayPause: () -> Unit,
    onSkipNext: () -> Unit,
    onSkipPrevious: () -> Unit,
    queueSongs: List<Song>,
    onShowQueueSheetChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme
    val layoutDirection = LocalLayoutDirection.current

    // Mirroring logic for icons that should always represent "next" and "previous"
    val skipNextIcon = painterResource(
        if (layoutDirection == LayoutDirection.Rtl)
            R.drawable.skip_previous
        else
            R.drawable.skip_next
    )

    val skipPreviousIcon = painterResource(
        if (layoutDirection == LayoutDirection.Rtl)
            R.drawable.skip_next
        else
            R.drawable.skip_previous
    )

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // ⏮ Skip Previous
        Icon(
            painter = skipPreviousIcon,
            contentDescription = "Skip Previous",
            tint = colorScheme.onTertiaryContainer.copy(0.7f),
            modifier = Modifier
                .size(20.dp)
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) { onSkipPrevious() }
        )

        // ⏯ Play/Pause button (main)
        Surface(
            modifier = Modifier.size(48.dp),
            shape = RoundedCornerShape(14.dp),
            color = colorScheme.onTertiaryContainer.copy(0.7f),
            onClick = onPlayPause
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = if (isPlaying)
                        painterResource(id = R.drawable.pause)
                    else
                        painterResource(id = R.drawable.resume),
                    contentDescription = null,
                    tint = colorScheme.tertiaryContainer,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // ⏭ Skip Next
        Icon(
            painter = skipNextIcon,
            contentDescription = "Skip Next",
            tint = colorScheme.onTertiaryContainer.copy(0.7f),
            modifier = Modifier
                .size(20.dp)
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) { onSkipNext() }
        )

        // 🎵 Queue with Badge
        NoRtl {
            Box {
                Icon(
                    painter = painterResource(id = R.drawable.queue_music),
                    contentDescription = "Queue",
                    tint = colorScheme.onTertiaryContainer.copy(0.7f),
                    modifier = Modifier
                        .size(20.dp)
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ) { onShowQueueSheetChange(true) }
                )

                if (queueSongs.isNotEmpty()) {
                    Badge(
                        containerColor = colorScheme.onTertiaryContainer.copy(0.9f),
                        contentColor = colorScheme.tertiaryContainer,
                        modifier = Modifier.offset(x = 8.dp, y = (-8).dp)
                    ) {
                        Text(
                            text = queueSongs.size.toLocalizedDigits(currentLanguage),
                            style = MaterialTheme.typography.labelSmall,
                        )
                    }
                }
            }
        }
    }
}


@Composable
fun SelectionActionsContent(
    selectedSongs: List<Song>,
    favViewModel: FavViewModel,
    musicViewModel: MusicViewModel,
    coroutineScope: CoroutineScope,
    songs: List<Song>,
    context: Context,
    onAddToPlaylist: (List<Song>) -> Unit,
    onDeleteSelected: (List<Song>) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        // Toolbar at the top
        SelectionToolbar(
            selectedCount = selectedSongs.size,
            totalCount = songs.size,
            context = context,
            onClose = { musicViewModel.exitSelectionMode() },
            onSelectAll = { musicViewModel.selectAllSongs(songs) }
        )

        // Action buttons
        val resources = context.resources

        NoRtl {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Play
                SelectionActionButton(
                    icon = R.drawable.resume,
                    label = stringResource(R.string.action_play),
                    onClick = {
                        if (selectedSongs.isNotEmpty()) {
                            musicViewModel.playSongs(selectedSongs, 0)
                            musicViewModel.exitSelectionMode()
                        }
                    }
                )

                // Favorite / Unfavorite
                val allAreFavorites = selectedSongs.all { favViewModel.isFavorite(it) }
                val favoriteLabel = if (allAreFavorites)
                    stringResource(R.string.action_unfavorite)
                else
                    stringResource(R.string.action_favorite)

                SelectionActionButton(
                    icon = if (allAreFavorites)
                        R.drawable.favorite_border
                    else
                        R.drawable.favorite,
                    label = favoriteLabel,
                    onClick = {
                        coroutineScope.launch {
                            if (allAreFavorites) {
                                selectedSongs.forEach { favViewModel.toggleFavorite(it) }
                                musicViewModel.exitSelectionMode()

                                Toast.makeText(
                                    context,
                                    resources.getQuantityString(
                                        R.plurals.removed_from_favorites,
                                        selectedSongs.size,
                                        selectedSongs.size
                                    ),
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                val songsToAdd =
                                    selectedSongs.filter { !favViewModel.isFavorite(it) }
                                songsToAdd.forEach { favViewModel.toggleFavorite(it) }
                                musicViewModel.exitSelectionMode()

                                if (songsToAdd.isNotEmpty()) {
                                    Toast.makeText(
                                        context,
                                        resources.getQuantityString(
                                            R.plurals.added_to_favorites,
                                            songsToAdd.size,
                                            songsToAdd.size
                                        ),
                                        Toast.LENGTH_SHORT
                                    ).show()
                                } else {
                                    Toast.makeText(
                                        context,
                                        context.getString(R.string.all_songs_already_in_favorites),
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        }
                    }
                )

                // Queue
                SelectionActionButton(
                    icon = R.drawable.queue_music,
                    label = stringResource(R.string.action_queue),
                    onClick = {
                        musicViewModel.addToQueue(selectedSongs)
                        musicViewModel.exitSelectionMode()
                    }
                )

                // Playlist
                SelectionActionButton(
                    icon = R.drawable.playlist_add,
                    label = stringResource(R.string.action_playlist),
                    onClick = {
                        onAddToPlaylist(selectedSongs)
                        musicViewModel.exitSelectionMode()
                    }
                )

                // Delete
                SelectionActionButton(
                    icon = R.drawable.deletefromplaylist,
                    label = stringResource(R.string.delete),
                    onClick = {
                        onDeleteSelected(selectedSongs)
                    }
                )
            }
        }
    }
}

@Composable
fun SelectionToolbar(
    selectedCount: Int,
    totalCount: Int,
    context: Context,
    onClose: () -> Unit,
    onSelectAll: () -> Unit
) {
    val selectedText = context.resources.getQuantityString(
        R.plurals.selected_items,
        selectedCount,
        selectedCount
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        IconButton(onClick = onClose) {
            Icon(
                painter = painterResource(R.drawable.close),
                contentDescription = stringResource(R.string.exit_selection),
                tint = MaterialTheme.colorScheme.onTertiaryContainer.copy(0.7f)
            )
        }

        Text(
            text = selectedText,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onTertiaryContainer.copy(0.7f),
            modifier = Modifier.weight(1f)
        )

        if (selectedCount < totalCount) {
            TextButton(onClick = onSelectAll) {
                Text(
                    text = stringResource(R.string.select_all),
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onTertiaryContainer.copy(0.7f)
                )
            }
        }
    }

}

@Composable
fun SelectionActionButton(
    icon: Int,
    label: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(
                onClick = onClick,
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            )
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Icon(
            painter = painterResource(id = icon),
            contentDescription = label,
            tint = MaterialTheme.colorScheme.onTertiaryContainer.copy(0.7f),
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onTertiaryContainer,
            fontWeight = FontWeight.Medium
        )
    }
}