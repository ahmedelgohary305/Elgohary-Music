package com.example.elgoharymusic.presentation.utils

import android.content.Context
import android.net.Uri
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.example.elgoharymusic.R
import com.example.elgoharymusic.domain.models.Song
import sh.calvin.reorderable.ReorderableCollectionItemScope
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QueueBottomSheet(
    queueSongs: List<Song>,
    currentSong: Song?,
    context: Context,
    onSongClick: (Song) -> Unit,
    onRemoveFromQueue: (Song) -> Unit,
    onClearQueue: () -> Unit,
    onReorderQueue: (Int, Int) -> Unit,
    onDismiss: () -> Unit,
    colorScheme: ColorScheme,
    currentLanguage: String
) {
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )

    // Search state
    var searchQuery by remember { mutableStateOf("") }
    var isSearchActive by remember { mutableStateOf(false) }

    // Filter songs based on search query
    val filteredQueueSongs = remember(queueSongs, searchQuery) {
        if (searchQuery.isBlank()) {
            queueSongs
        } else {
            queueSongs.filter { song ->
                song.title.contains(searchQuery, ignoreCase = true) ||
                        song.artist.contains(searchQuery, ignoreCase = true) ||
                        song.album?.contains(searchQuery, ignoreCase = true) == true
            }
        }
    }

    // State for reorderable list
    var reorderableQueueSongs by remember { mutableStateOf<List<Song>>(emptyList()) }

    // Track the original position of the song being dragged
    var draggedSongOriginalIndex by remember { mutableStateOf<Int?>(null) }
    var draggedSongId by remember { mutableStateOf<Long?>(null) }

    val lazyListState = rememberLazyListState()
    val reorderableLazyListState = rememberReorderableLazyListState(
        lazyListState = lazyListState
    ) { from, to ->
        // On first move, track which song is being dragged and its original position
        if (draggedSongId == null) {
            val movingSong = reorderableQueueSongs[from.index]
            draggedSongId = movingSong.id
            draggedSongOriginalIndex = queueSongs.indexOfFirst { it.id == movingSong.id }
        }

        // Update the reorderable list immediately for visual feedback
        reorderableQueueSongs = reorderableQueueSongs.toMutableList().apply {
            add(to.index, removeAt(from.index))
        }
    }

    // When the list updates (drag complete), execute the reorder
    LaunchedEffect(reorderableQueueSongs, draggedSongId) {
        if (draggedSongId != null && draggedSongOriginalIndex != null) {
            // Wait a bit to ensure drag is complete
            kotlinx.coroutines.delay(50)

            // Find where the dragged song ended up in the reorderable list
            val currentIndexInReorderable = reorderableQueueSongs.indexOfFirst { it.id == draggedSongId }

            if (currentIndexInReorderable != -1) {
                // Find the target position in the original list by looking at surrounding songs
                val originalToIndex = if (currentIndexInReorderable == 0) {
                    // Moved to top
                    0
                } else if (currentIndexInReorderable == reorderableQueueSongs.lastIndex) {
                    // Moved to bottom
                    queueSongs.lastIndex
                } else {
                    // Find the song just before the dragged song in reorderable list
                    val songBefore = reorderableQueueSongs[currentIndexInReorderable - 1]
                    val beforeIndexInOriginal = queueSongs.indexOfFirst { it.id == songBefore.id }

                    if (beforeIndexInOriginal != -1) {
                        // Target is right after this song in original list
                        beforeIndexInOriginal + 1
                    } else {
                        currentIndexInReorderable
                    }
                }

                if (draggedSongOriginalIndex != originalToIndex) {
                    onReorderQueue(draggedSongOriginalIndex!!, originalToIndex)
                }
            }

            // Reset tracking
            draggedSongId = null
            draggedSongOriginalIndex = null
        }
    }

    // Update reorderable songs when filtered queue changes
    LaunchedEffect(filteredQueueSongs) {
        reorderableQueueSongs = filteredQueueSongs
    }

    // Focus requester for search
    val searchFocusRequester = remember { FocusRequester() }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = colorScheme.background,
        contentColor = colorScheme.onBackground,
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.systemBars)
                .padding(start = 8.dp, end = 8.dp, top = 16.dp)
        ) {
            // Header with queue count, search, and clear button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isSearchActive) {
                    // Search field in header
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier
                            .weight(1f)
                            .focusRequester(searchFocusRequester),
                        placeholder = { Text(stringResource(R.string.search_queue)) },
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
                        shape = CircleShape,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = colorScheme.secondary,
                            cursorColor = colorScheme.secondary
                        )
                    )

                    LaunchedEffect(Unit) {
                        searchFocusRequester.requestFocus()
                    }
                } else {
                    val queueText = if (queueSongs.size == 1) {
                        context.resources.getQuantityString(R.plurals.queue_songs, 1)
                    } else {
                        context.resources.getQuantityString(R.plurals.queue_songs, queueSongs.size, queueSongs.size)
                    }

                    Text(
                        text = queueText,
                        style = MaterialTheme.typography.headlineSmall,
                        color = colorScheme.onBackground
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Clear all button
                        if (queueSongs.isNotEmpty()) {
                            TextButton(
                                onClick = onClearQueue,
                                colors = ButtonDefaults.textButtonColors(
                                    contentColor = colorScheme.error
                                )
                            ) {
                                Text(
                                    text = stringResource(R.string.clear_all),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                        // Search icon
                        if (queueSongs.isNotEmpty()) {
                            IconButton(
                                onClick = { isSearchActive = true }
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.search),
                                    contentDescription = "Search queue",
                                    modifier = Modifier.size(24.dp),
                                    tint = colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            // Reorderable queue list
            if (reorderableQueueSongs.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
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
                        Text(
                            text = if (searchQuery.isNotEmpty())
                                stringResource(R.string.no_songs_found)
                            else
                                stringResource(R.string.queue_empty),
                            style = MaterialTheme.typography.bodyLarge,
                            color = colorScheme.onSurfaceVariant
                        )

                        if (searchQuery.isNotEmpty()) {
                            Text(
                                text = stringResource(R.string.try_searching_different_keywords),
                                style = MaterialTheme.typography.bodyMedium,
                                color = colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                modifier = Modifier.offset(
                                    y = if (currentLanguage == LocaleManager.Language.ARABIC.code) (-6).dp else 0.dp
                                )
                            )
                        }
                    }
                }
            } else {
                LazyColumn(
                    state = lazyListState,
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(
                        items = reorderableQueueSongs,
                        key = { song -> song.id }
                    ) { song ->
                        val index = reorderableQueueSongs.indexOf(song)

                        ReorderableItem(
                            state = reorderableLazyListState,
                            key = song.id
                        ) { isDragging ->
                            DraggableQueueSongItem(
                                song = song,
                                index = index + 1,
                                isCurrentSong = song.id == currentSong?.id,
                                isDragging = isDragging,
                                onSongClick = { onSongClick(song) },
                                onRemoveClick = { onRemoveFromQueue(song) },
                                colorScheme = colorScheme,
                                context = context,
                                scope = this // Pass the ReorderableCollectionItemScope
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DraggableQueueSongItem(
    song: Song,
    index: Int,
    isCurrentSong: Boolean,
    isDragging: Boolean,
    context: Context,
    onSongClick: () -> Unit,
    onRemoveClick: () -> Unit,
    colorScheme: ColorScheme,
    scope: ReorderableCollectionItemScope
) {
    val scale by animateFloatAsState(
        targetValue = when {
            isDragging -> 1.05f
            isCurrentSong -> 1.02f
            else -> 1f
        },
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "scaleAnimation"
    )

    val backgroundColor by animateColorAsState(
        targetValue = when {
            isDragging -> colorScheme.surfaceVariant
            isCurrentSong -> colorScheme.tertiaryContainer.copy(alpha = 0.5f)
            else -> Color.Transparent
        },
        animationSpec = tween(durationMillis = 300),
        label = "backgroundColorAnimation"
    )

    val borderColor by animateColorAsState(
        targetValue = when {
            isDragging -> colorScheme.onBackground.copy(alpha = 0.8f)
            isCurrentSong && !isSystemInDarkTheme() ->
                colorScheme.onBackground.copy(alpha = 0.3f)
            isCurrentSong && isSystemInDarkTheme() ->
                colorScheme.tertiaryContainer.copy(0.8f)
            else -> Color.Transparent
        },
        animationSpec = tween(durationMillis = 300),
        label = "borderColorAnimation"
    )

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .clip(RoundedCornerShape(16.dp))
            .border(
                width = if (isCurrentSong || isDragging) 1.dp else 0.dp,
                color = borderColor,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(
                enabled = !isDragging,
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) { onSongClick() }
            .zIndex(if (isDragging) 1f else 0f),
        color = backgroundColor,
        shape = RoundedCornerShape(16.dp),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Drag handle
            IconButton(
                onClick = {},
                modifier = with(scope) {
                    Modifier
                        .size(32.dp)
                        .draggableHandle()
                }
            ) {
                Icon(
                    painter = painterResource(R.drawable.drag_handle),
                    contentDescription = "Drag to reorder",
                    tint = colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Index or Now Playing indicator
            Box(
                modifier = Modifier.size(28.dp),
                contentAlignment = Alignment.Center
            ) {
                if (isCurrentSong) {
                    Icon(
                        painter = painterResource(R.drawable.volume_up),
                        contentDescription = "Now Playing",
                        tint = colorScheme.tertiary,
                        modifier = Modifier.size(20.dp)
                    )
                } else {
                    Text(
                        text = index.toString(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = colorScheme.onSurface
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Album art
            Surface(
                modifier = Modifier.size(56.dp),
                shape = RoundedCornerShape(12.dp),
                color = Color.DarkGray
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

            // Song info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = song.title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = if (isCurrentSong) FontWeight.Bold else FontWeight.SemiBold
                    ),
                    color = if (isCurrentSong)
                        colorScheme.tertiary
                    else
                        colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = song.artist,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isCurrentSong)
                        colorScheme.tertiary.copy(alpha = 0.8f)
                    else
                        colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Remove button
            IconButton(
                onClick = onRemoveClick,
                enabled = !isDragging
            ) {
                Icon(
                    painter = painterResource(R.drawable.close),
                    contentDescription = "Remove from queue",
                    tint = colorScheme.onSurface.copy(alpha = 0.7f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}