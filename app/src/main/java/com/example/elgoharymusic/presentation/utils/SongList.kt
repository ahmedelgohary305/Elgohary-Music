package com.example.elgoharymusic.presentation.utils

import android.content.Context
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.elgoharymusic.R
import com.example.elgoharymusic.data.repoImpl.AppLanguage
import com.example.elgoharymusic.domain.models.Song
import com.example.elgoharymusic.presentation.screens.NoRtl
import com.example.elgoharymusic.presentation.viewmodels.MusicViewModel
import com.example.elgoharymusic.presentation.viewmodels.SortOrder
import kotlinx.coroutines.launch

@Composable
fun SongList(
    songs: List<Song>,
    currentSong: Song?,
    musicViewModel: MusicViewModel,
    context: Context,
    currentLanguage: AppLanguage,
    onSongClick: (Song) -> Unit,
    onEditSong: ((Song) -> Unit)? = null,
    onDeleteSong: ((Song) -> Unit)? = null,
    additionalMenuOptions: List<SongMenuOption> = emptyList(),
    miniPlayerHeight: Dp = 88.dp,
    emptyMessage: String = stringResource(R.string.no_music_found),
    emptyDescription: String = stringResource(R.string.add_some_music_to_get_started),
) {
    val isSelectionMode by musicViewModel.isSelectionMode.collectAsStateWithLifecycle()
    val selectedSongs by musicViewModel.selectedSongs.collectAsStateWithLifecycle()
    val sortOrder by musicViewModel.sortOrder.collectAsStateWithLifecycle()
    var showSortMenu by remember { mutableStateOf(false) }

    // Scroll state for LazyColumn
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    // Show button when scrolled past first item
    val showScrollToTop by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex > 0
        }
    }

    // Sort songs based on current order from ViewModel
    val sortedSongs = remember(songs, sortOrder) {
        musicViewModel.getSortedSongs(songs, sortOrder)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            if (songs.isEmpty()) {
                EmptyState(
                    emptyMessage = emptyMessage,
                    emptyDescription = emptyDescription,
                    currentSong = currentSong,
                    currentLanguage = currentLanguage,
                    miniPlayerHeight = miniPlayerHeight,
                    painter = painterResource(id = R.drawable.song)
                )
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        start = 8.dp,
                        end = 16.dp,
                        bottom = when {
                            isSelectionMode -> 96.dp + 16.dp
                            currentSong != null -> miniPlayerHeight + 16.dp
                            else -> 16.dp
                        }
                    ),
                ) {
                    item {
                        SongListControls(
                            sortOrder = sortOrder,
                            showSortMenu = showSortMenu,
                            onPlayAll = { musicViewModel.playSongs(sortedSongs, 0) },
                            onShuffle = { musicViewModel.shuffleAndPlay(sortedSongs) },
                            onSortClick = { showSortMenu = true },
                            onSortMenuDismiss = { showSortMenu = false },
                            onSortOrderChange = { newOrder ->
                                musicViewModel.setSortOrder(newOrder)
                                showSortMenu = false
                            }
                        )
                    }

                    itemsIndexed(
                        items = sortedSongs,
                        key = { _, song -> song.id }
                    ) { index, song ->
                        val menuOptions = buildList {
                            add(
                                SongMenuOption(
                                text = stringResource(R.string.add_to_queue),
                                icon = R.drawable.queue_music,
                                action = { musicViewModel.addToQueue(listOf(it)) }
                            ))

                            addAll(additionalMenuOptions)

                            if (onEditSong != null) {
                                add(
                                    SongMenuOption(
                                        text = stringResource(R.string.edit_song),
                                        icon = R.drawable.edit,
                                        action = onEditSong
                                    )
                                )
                            }

                            if (onDeleteSong != null) {
                                add(
                                    SongMenuOption(
                                        text = stringResource(R.string.delete),
                                        icon = R.drawable.deletefromplaylist,
                                        action = onDeleteSong
                                    )
                                )
                            }
                        }

                        SongItem(
                            song = song,
                            isCurrentSong = currentSong?.id == song.id,
                            isSelected = selectedSongs.contains(song.id),
                            isSelectionMode = isSelectionMode,
                            onClick = {
                                if (isSelectionMode) {
                                    musicViewModel.toggleSongSelection(song.id)
                                } else {
                                    onSongClick(song)
                                }
                            },
                            onLongClick = {
                                if (!isSelectionMode) {
                                    musicViewModel.enterSelectionMode(song.id)
                                }
                            },
                            menuOptions = if (isSelectionMode) emptyList() else menuOptions,
                            context = context,
                            currentLanguage = currentLanguage
                        )
                    }
                }
            }
        }


        val targetPadding = when {
            isSelectionMode -> 128.dp + 16.dp
            currentSong != null -> miniPlayerHeight + 12.dp
            else -> 16.dp
        }

        val animatedPadding by animateDpAsState(
            targetValue = targetPadding,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioNoBouncy,
                stiffness = Spring.StiffnessLow
            ),
            label = "fab_padding"
        )

        AnimatedVisibility(
            visible = showScrollToTop,
            enter = fadeIn(animationSpec = tween(250)) +
                    scaleIn(
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessMedium
                        ),
                        initialScale = 0.8f
                    ),
            exit = fadeOut(animationSpec = tween(200)) +
                    scaleOut(
                        animationSpec = tween(200),
                        targetScale = 0.8f
                    ),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = animatedPadding)
        ) {
            FloatingActionButton(
                onClick = {
                    coroutineScope.launch {
                        listState.animateScrollToItem(0)
                    }
                },
                containerColor = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.size(34.dp),
                elevation = FloatingActionButtonDefaults.bottomAppBarFabElevation(
                    defaultElevation = 4.dp,
                )
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.arrow_upward),
                    contentDescription = "Scroll to top",
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

// 5. Updated SongListControls with new layout
@Composable
fun SongListControls(
    sortOrder: SortOrder,
    showSortMenu: Boolean,
    onPlayAll: () -> Unit,
    onShuffle: () -> Unit,
    onSortClick: () -> Unit,
    onSortMenuDismiss: () -> Unit,
    onSortOrderChange: (SortOrder) -> Unit
) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box {
            Row(
                modifier = Modifier
                    .clickable(
                        onClick = onSortClick,
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.sort),
                    contentDescription = "Sort",
                    tint = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.size(26.dp)
                )
                Text(
                    text = sortOrder.toReadableName(),
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            // 🔽 Sort Dropdown Menu
            DropdownMenu(
                expanded = showSortMenu,
                onDismissRequest = onSortMenuDismiss,
                modifier = Modifier
                    .background(
                        color = MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(12.dp)
                    )
                    .padding(vertical = 4.dp)
            ) {
                SortOrder.entries.forEach { order ->
                    SortOption(
                        text = order.toReadableName(),
                        isSelected = sortOrder == order,
                        onClick = { onSortOrderChange(order) }
                    )
                }
            }
        }

        // 🔹 Play All + Shuffle Buttons
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            SmallCircularButton(
                iconRes = R.drawable.resume,
                contentDescription = "Play All",
                backgroundColor = MaterialTheme.colorScheme.onBackground.copy(0.1f),
                iconTint = MaterialTheme.colorScheme.onSurface,
                onClick = onPlayAll
            )
            SmallCircularButton(
                iconRes = R.drawable.shuffle,
                contentDescription = "Shuffle",
                backgroundColor = MaterialTheme.colorScheme.onBackground.copy(0.1f),
                iconTint = MaterialTheme.colorScheme.onSurface,
                onClick = onShuffle
            )
        }
    }
}

@Composable
private fun SmallCircularButton(
    iconRes: Int,
    contentDescription: String,
    backgroundColor: Color,
    iconTint: Color,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = backgroundColor,
        modifier = Modifier.size(40.dp),
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = contentDescription,
                tint = iconTint,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun SortOrder.toReadableName(): String {
    return when (this) {
        SortOrder.TITLE_ASC -> stringResource(R.string.sort_by_name)
        SortOrder.ARTIST_ASC -> stringResource(R.string.sort_by_artist)
        SortOrder.DURATION_ASC -> stringResource(R.string.sort_by_duration)
    }
}


@Composable
fun SortOption(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    DropdownMenuItem(
        text = {
            Text(
                text = text,
                color = if (isSelected)
                    MaterialTheme.colorScheme.secondary
                else
                    MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                )
            )
        },
        onClick = onClick,
        trailingIcon = if (isSelected) {
            {
                Icon(
                    painter = painterResource(R.drawable.check),
                    contentDescription = "Selected",
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(20.dp)
                )
            }
        } else null,
        modifier = Modifier.padding(horizontal = 4.dp)
    )
}

// 3. Update SongItem to support long press and selection
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SongItem(
    song: Song,
    isCurrentSong: Boolean,
    isSelected: Boolean = false,
    isSelectionMode: Boolean = false,
    context: Context,
    currentLanguage: AppLanguage,
    onClick: () -> Unit,
    onLongClick: () -> Unit = {},
    menuOptions: List<SongMenuOption> = emptyList()
) {
    val backgroundColor by animateColorAsState(
        targetValue = when {
            isCurrentSong -> MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f)
            else -> Color.Transparent
        },
        animationSpec = tween(durationMillis = 300),
        label = "backgroundColorAnimation"
    )

    val borderColor by animateColorAsState(
        targetValue = when {
            isCurrentSong && !isSystemInDarkTheme() -> MaterialTheme.colorScheme.onBackground.copy(
                alpha = 0.3f
            )

            isCurrentSong && isSystemInDarkTheme() -> MaterialTheme.colorScheme.tertiaryContainer.copy(
                0.8f
            )

            else -> Color.Transparent
        },
        animationSpec = tween(durationMillis = 300),
        label = "borderColorAnimation"
    )

    var showDropdownMenu by remember { mutableStateOf(false) }
    val colorScheme = MaterialTheme.colorScheme

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(
                width = if (isCurrentSong || isSelected) 1.dp else 0.dp,
                color = borderColor,
                shape = RoundedCornerShape(16.dp)
            )
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick,
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ),
        shape = RoundedCornerShape(16.dp),
        color = backgroundColor,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = if (currentLanguage == AppLanguage.ARABIC) 6.dp else 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AnimatedVisibility(
                visible = isSelectionMode,
                enter = fadeIn(animationSpec = tween(300)) + scaleIn(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMedium
                    )
                ),
                exit = fadeOut(animationSpec = tween(200)) + scaleOut(
                    animationSpec = tween(200)
                )
            ) {
                Row {
                    Surface(
                        modifier = Modifier.size(22.dp),
                        shape = CircleShape,
                        color = if (isSelected)
                            MaterialTheme.colorScheme.tertiary
                        else
                            Color.Transparent,
                        border = BorderStroke(
                            width = 2.dp,
                            color = if (isSelected)
                                MaterialTheme.colorScheme.tertiary
                            else
                                MaterialTheme.colorScheme.outline
                        ),
                        onClick = onClick
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            androidx.compose.animation.AnimatedVisibility(
                                visible = isSelected,
                                enter = fadeIn(animationSpec = tween(200)) + scaleIn(
                                    initialScale = 0.3f,
                                    animationSpec = spring(
                                        dampingRatio = Spring.DampingRatioMediumBouncy,
                                        stiffness = Spring.StiffnessMedium
                                    )
                                ),
                                exit = fadeOut(animationSpec = tween(100)) + scaleOut(
                                    targetScale = 0.3f,
                                    animationSpec = tween(100)
                                )
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.check),
                                    contentDescription = "Selected",
                                    tint = MaterialTheme.colorScheme.onTertiary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))
                }
            }

            // Album Art Container
            Surface(
                modifier = Modifier.size(48.dp),
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

                    // Current song play indicator overlay
                    if (isCurrentSong && !isSelectionMode) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                    RoundedCornerShape(10.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.resume),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onTertiaryContainer.copy(0.8f),
                                modifier = Modifier
                                    .size(18.dp)
                                    .shadow(
                                        elevation = 16.dp,
                                        shape = CircleShape,
                                    )
                            )
                        }
                    }
                }
            }

            // Song Info
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 12.dp, end = 8.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = song.title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = if (isCurrentSong && !isSelectionMode) FontWeight.Bold else FontWeight.SemiBold
                    ),
                    color = when {
                        isCurrentSong -> MaterialTheme.colorScheme.onTertiaryContainer.copy(0.7f)
                        else -> MaterialTheme.colorScheme.onSurface
                    },
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = song.artist,
                        style = MaterialTheme.typography.bodyMedium,
                        color = when {
                            isCurrentSong -> MaterialTheme.colorScheme.onTertiaryContainer.copy(
                                alpha = 0.7f
                            )

                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false).offset(
                            y = if (currentLanguage == AppLanguage.ARABIC) (-6).dp else 0.dp
                        )
                    )

                    Text(
                        text = " • ${TimeFormatter.formatDuration(song.duration, currentLanguage)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = when {
                            isCurrentSong -> MaterialTheme.colorScheme.onTertiaryContainer.copy(0.6f)
                            else -> MaterialTheme.colorScheme.onSurface
                        },
                        modifier = Modifier.offset(
                            y = if (currentLanguage == AppLanguage.ARABIC) (-6).dp else 0.dp
                        )
                    )

                }
            }

            // More Options Menu - Only show if not in selection mode and there are menu options
            if (!isSelectionMode && menuOptions.isNotEmpty()) {
                Box {
                    Surface(
                        modifier = Modifier.size(40.dp),
                        shape = CircleShape,
                        color = if (showDropdownMenu)
                            colorScheme.surfaceVariant
                        else
                            Color.Transparent,
                        onClick = { showDropdownMenu = true }
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.more_vert),
                            contentDescription = "More options",
                            tint = when {
                                isCurrentSong -> MaterialTheme.colorScheme.onTertiaryContainer.copy(
                                    0.7f
                                )

                                else -> MaterialTheme.colorScheme.onSurface
                            },
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(8.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = showDropdownMenu,
                        onDismissRequest = { showDropdownMenu = false },
                        modifier = Modifier
                            .background(
                                color = colorScheme.surface,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .padding(vertical = 4.dp)
                    ) {
                        menuOptions.forEach { option ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = option.text,
                                        color = colorScheme.onSurface,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                },
                                onClick = {
                                    option.action(song)
                                    showDropdownMenu = false
                                },
                                leadingIcon = {
                                    NoRtl {
                                        Icon(
                                            painter = painterResource(option.icon!!),
                                            contentDescription = option.text,
                                            tint = colorScheme.onSurface.copy(alpha = 0.7f),
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                },
                                modifier = Modifier.padding(horizontal = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

data class SongMenuOption(
    val text: String,
    val icon: Int? = null,
    val action: (Song) -> Unit,
)
