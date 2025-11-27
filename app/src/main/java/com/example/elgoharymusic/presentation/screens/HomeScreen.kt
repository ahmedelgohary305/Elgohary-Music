package com.example.elgoharymusic.presentation.screens

import android.content.Context
import androidx.annotation.DrawableRes
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.elgoharymusic.R
import com.example.elgoharymusic.presentation.Routes
import com.example.elgoharymusic.presentation.utils.AlbumsGrid
import com.example.elgoharymusic.presentation.utils.ArtistsList
import com.example.elgoharymusic.presentation.utils.BottomContent
import com.example.elgoharymusic.presentation.utils.CreatePlaylistDialog
import com.example.elgoharymusic.presentation.utils.LocaleManager
import com.example.elgoharymusic.presentation.utils.PlaylistTab
import com.example.elgoharymusic.presentation.utils.SearchField
import com.example.elgoharymusic.presentation.utils.SearchResultsList
import com.example.elgoharymusic.presentation.utils.SongDialogsHandler
import com.example.elgoharymusic.presentation.utils.SongList
import com.example.elgoharymusic.presentation.utils.rememberSongDialogsState
import com.example.elgoharymusic.presentation.utils.toLocalizedDigits
import com.example.elgoharymusic.presentation.viewmodels.FavViewModel
import com.example.elgoharymusic.presentation.viewmodels.MusicViewModel
import com.example.elgoharymusic.presentation.viewmodels.PlaylistViewModel
import com.example.elgoharymusic.ui.theme.ArabicFontFamily
import com.example.elgoharymusic.ui.theme.EnglishFontFamily
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


@Composable
fun ModernHomeScreen(
    musicViewModel: MusicViewModel,
    favViewModel: FavViewModel,
    playlistViewModel: PlaylistViewModel,
    navController: NavController,
    context: Context,
    currentLanguage: String,
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    onNavigateToPlayer: () -> Unit
) {
    val songs by musicViewModel.songs.collectAsStateWithLifecycle()
    val favSongs by favViewModel.favSongs.collectAsStateWithLifecycle()
    val albums by musicViewModel.albums.collectAsStateWithLifecycle()
    val currentSong by musicViewModel.currentSong.collectAsStateWithLifecycle()
    val isPlaying by musicViewModel.isPlaying.collectAsStateWithLifecycle()
    val isSearchActive by musicViewModel.isSearchActive.collectAsStateWithLifecycle()
    val searchQuery by musicViewModel.searchQuery.collectAsStateWithLifecycle()
    val filteredSongs by musicViewModel.filteredSongs.collectAsStateWithLifecycle()
    val filteredArtists by musicViewModel.filteredArtists.collectAsStateWithLifecycle()
    val queueSongs by musicViewModel.queueSongs.collectAsStateWithLifecycle()
    val showCreatePlaylistDialog by musicViewModel.showCreatePlaylistDialog.collectAsStateWithLifecycle()
    val selectedSongsForPlaylist by musicViewModel.selectedSongsForPlaylist.collectAsStateWithLifecycle()

    // Settings states
    val isSettingsDrawerOpen by musicViewModel.isSettingsDrawerOpen.collectAsStateWithLifecycle()
    val isDarkTheme by musicViewModel.isDarkTheme.collectAsStateWithLifecycle()

    val listState = rememberLazyListState()
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

    // === Main UI ===
    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            ModernHeader(
                songCount = songs.size,
                selectedTab = selectedTab,
                onTabSelected = onTabSelected,
                isSearchActive = isSearchActive,
                searchQuery = searchQuery,
                listState = listState,
                context = context,
                currentLanguage = currentLanguage,
                onSearchActivated = { musicViewModel.activateSearch() },
                onSearchDeactivated = { musicViewModel.deactivateSearch() },
                onSearchQueryChanged = { musicViewModel.updateSearchQuery(it) },
                onAddPlaylistClick = { musicViewModel.showCreatePlaylistDialog(true) },
                onSettingsClick = { musicViewModel.openSettingsDrawer() }
            )

            when {
                isSearchActive -> {
                    SearchResultsList(
                        searchQuery = searchQuery,
                        filteredSongs = filteredSongs,
                        currentSong = currentSong,
                        musicViewModel = musicViewModel,
                        currentLanguage = currentLanguage,
                        context = context,
                        onSongClick = { song -> musicViewModel.playSong(song, filteredSongs) }
                    )
                }

                selectedTab == 0 -> {
                    PlaylistTab(
                        playlistViewModel = playlistViewModel,
                        musicViewModel = musicViewModel,
                        navController = navController,
                        context = context,
                        currentLanguage = currentLanguage,
                        onCreatePlaylist = {
                            musicViewModel.showCreatePlaylistDialog(true)
                        }
                    )
                }

                selectedTab == 1 -> {
                    SongList(
                        songs = favSongs,
                        currentSong = currentSong,
                        musicViewModel = musicViewModel,
                        onSongClick = { song -> musicViewModel.playSong(song, favSongs) },
                        onEditSong = { song -> dialogsState.showEditDialog(song) },
                        onDeleteSong = { song -> dialogsState.showDeleteDialog(listOf(song)) },
                        context = context,
                        currentLanguage = currentLanguage,
                        emptyMessage = stringResource(R.string.no_favorites_yet),
                        emptyDescription = stringResource(R.string.heart_songs_to_add_them_to_your_favorites),
                    )
                }

                selectedTab == 2 -> {
                    SongList(
                        songs = songs,
                        currentSong = currentSong,
                        musicViewModel = musicViewModel,
                        onSongClick = { song -> musicViewModel.playSong(song, songs) },
                        onEditSong = { song -> dialogsState.showEditDialog(song) },
                        onDeleteSong = { song -> dialogsState.showDeleteDialog(listOf(song)) },
                        context = context,
                        currentLanguage = currentLanguage,
                    )
                }

                selectedTab == 3 -> {
                    AlbumsGrid(
                        albums = albums,
                        musicViewModel = musicViewModel,
                        onAlbumClick = { album ->
                            navController.navigate(Routes.AlbumSongs.createRoute(album.albumId))
                        },
                        currentLanguage = currentLanguage,
                    )
                }

                selectedTab == 4 -> {
                    ArtistsList(
                        artists = filteredArtists,
                        currentSong = currentSong,
                        musicViewModel = musicViewModel,
                        currentLanguage = currentLanguage,
                        onArtistClick = { artist ->
                            navController.navigate(Routes.ArtistSongs.createRoute(artist.artistId))
                        },
                        context = context
                    )
                }
            }
        }

        // === Selection Bottom Bar ===
        val isSelectionMode by musicViewModel.isSelectionMode.collectAsStateWithLifecycle()
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

        // === Create Playlist Dialog ===
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


        SettingsBottomSheet(
            isOpen = isSettingsDrawerOpen,
            currentLanguage = currentLanguage,
            isDarkTheme = isDarkTheme,
            onDismiss = { musicViewModel.closeSettingsDrawer() },
            onThemeChange = { isDark ->
                musicViewModel.setDarkTheme(isDark)
            }
        )
    }
}

@Composable
private fun ModernHeader(
    songCount: Int,
    selectedTab: Int,
    isSearchActive: Boolean,
    searchQuery: String,
    listState: LazyListState,
    currentLanguage: String,
    context: Context,
    onTabSelected: (Int) -> Unit,
    onSearchActivated: () -> Unit,
    onSearchDeactivated: () -> Unit,
    onSearchQueryChanged: (String) -> Unit,
    onAddPlaylistClick: () -> Unit,
    onSettingsClick: () -> Unit
) {

    val headerLabels = listOf(
        stringResource(R.string.playlists),
        stringResource(R.string.favorites),
        stringResource(R.string.your_music),
        stringResource(R.string.albums),
        stringResource(R.string.artists),
        stringResource(R.string.music)
    )

    val descriptionLabels = listOf(
        stringResource(R.string.curated_playlists),
        stringResource(R.string.favorite_tracks),
        if (songCount == 1) {
            context.getString(R.string.one_song_in_library)
        } else {
            context.getString(
                R.string.songs_in_library,
                songCount.toLocalizedDigits(currentLanguage)
            )
        },
        stringResource(R.string.album_collection),
        stringResource(R.string.artist_library),
        stringResource(R.string.discover_your_music)

    )

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 16.dp)
                .statusBarsPadding(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            if (isSearchActive) {
                SearchField(
                    searchQuery = searchQuery,
                    onSearchQueryChanged = onSearchQueryChanged,
                    onSearchDeactivated = onSearchDeactivated
                )
            } else {
                Column(
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .padding(start = 8.dp, end = 8.dp)
                ) {
                    Text(
                        text = headerText(
                            selectedTab,
                            headerLabels
                        ),
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = if (currentLanguage == LocaleManager.Language.ARABIC.code) 0.sp else (-0.5).sp,
                            lineHeight = if (currentLanguage == LocaleManager.Language.ARABIC.code) 1.sp else
                                MaterialTheme.typography.headlineMedium.lineHeight,
                        ),
                        color = colorScheme.tertiary,
                        maxLines = 1,
                        overflow = TextOverflow.Visible,
                        modifier = Modifier
                            .fillMaxWidth()
                    )
                    Text(
                        text = headerText(
                            selectedTab,
                            descriptionLabels
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                        color = colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .fillMaxWidth()
                            .offset(y = if (currentLanguage == LocaleManager.Language.ARABIC.code) (-6).dp else 0.dp)
                    )
                }

                // Action buttons row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.wrapContentWidth()
                ) {
                    // Show add playlist button only on playlists tab
                    if (selectedTab == 0) {
                        HeaderButton(
                            onClick = onAddPlaylistClick,
                            iconRes = R.drawable.addplaylist
                        )
                    }

                    HeaderButton(
                        onClick = onSearchActivated,
                        iconRes = R.drawable.search
                    )

                    HeaderButton(
                        onClick = onSettingsClick,
                        iconRes = R.drawable.settings
                    )
                }
            }
        }

        // Show tabs only when search is not active
        if (!isSearchActive) {
            CarouselNavigationTabs(
                selectedTab = selectedTab,
                onTabSelected = onTabSelected,
                listState = listState,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
    }
}

@Composable
fun HeaderButton(
    onClick: () -> Unit,
    @DrawableRes iconRes: Int,
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier.size(48.dp)
    ) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = null,
            modifier = Modifier.size(22.dp),
            tint = colorScheme.onSurface
        )
    }
}

fun headerText(
    selectedTab: Int,
    stringList: List<String>
): String {
    return when (selectedTab) {
        0 -> stringList[0]
        1 -> stringList[1]
        2 -> stringList[2]
        3 -> stringList[3]
        4 -> stringList[4]
        else -> stringList[5]
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsBottomSheet(
    isOpen: Boolean,
    currentLanguage: String,
    isDarkTheme: Boolean,
    onDismiss: () -> Unit,
    onThemeChange: (Boolean) -> Unit
) {

    if (isOpen) {

        ModalBottomSheet(
            onDismissRequest = onDismiss,
            sheetState = rememberModalBottomSheetState(),
            shape = RoundedCornerShape(16.dp),
            containerColor = colorScheme.surface,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 32.dp)
            ) {
                Text(
                    text = stringResource(R.string.settings), // Use string resource
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = colorScheme.onSurface,
                    modifier = Modifier
                        .padding(bottom = 24.dp)
                        .offset(
                            y = if (currentLanguage == LocaleManager.Language.ARABIC.code) (5).dp else 0.dp
                        )
                )

                SettingsSection(
                    title = stringResource(R.string.language),
                    icon = R.drawable.language
                ) {
                    LanguageSelector(
                        currentLanguage = currentLanguage,
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                SettingsSection(
                    title = stringResource(R.string.theme),
                    icon = R.drawable.dark_theme
                ) {
                    ThemeToggle(
                        isDarkTheme = isDarkTheme,
                        onThemeChange = onThemeChange
                    )
                }
            }
        }
    }
}


@Composable
private fun SettingsSection(
    title: String,
    icon: Int,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 12.dp)
        ) {
            Icon(
                painter = painterResource(id = icon),
                contentDescription = null,
                tint = colorScheme.tertiary,
                modifier = if (icon == R.drawable.dark_theme) Modifier.size(18.dp) else Modifier.size(
                    22.dp
                )
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = colorScheme.onSurface
            )
        }
        content()
    }
}

@Composable
private fun LanguageSelector(
    currentLanguage: String,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        LanguageOption(
            label = "English",
            fontFamily = EnglishFontFamily,
            isSelected = currentLanguage == LocaleManager.Language.ENGLISH.code,
            onClick = { LocaleManager.setLocale(LocaleManager.Language.ENGLISH) },
            modifier = Modifier.weight(1f)
        )
        LanguageOption(
            label = "العربية",
            fontFamily = ArabicFontFamily,
            isSelected = currentLanguage == LocaleManager.Language.ARABIC.code,
            onClick = { LocaleManager.setLocale(LocaleManager.Language.ARABIC) },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun LanguageOption(
    label: String,
    fontFamily: FontFamily,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(56.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (isSelected) colorScheme.secondary
                else colorScheme.onBackground.copy(0.1f)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                fontFamily = fontFamily
            ),
            color = colorScheme.onTertiaryContainer
        )
    }
}

@Composable
private fun ThemeToggle(
    isDarkTheme: Boolean,
    onThemeChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(
                if (isDarkTheme) colorScheme.background.copy(0.5f)
                else colorScheme.onBackground.copy(0.1f)
            )
            .clickable { onThemeChange(!isDarkTheme) }
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = if (isDarkTheme)
                stringResource(R.string.dark_mode)
            else
                stringResource(R.string.light_mode),
            style = MaterialTheme.typography.bodyLarge,
            color = colorScheme.onTertiaryContainer
        )

        Switch(
            checked = isDarkTheme,
            onCheckedChange = onThemeChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = colorScheme.surface,
                checkedTrackColor = colorScheme.onSurface,
                uncheckedThumbColor = colorScheme.outline,
                uncheckedTrackColor = colorScheme.surfaceVariant
            )
        )
    }
}

@Composable
fun CarouselNavigationTabs(
    listState: LazyListState,
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    NoRtl {

        val tabs = listOf(
            NavigationTab(
                stringResource(R.string.playlists),
                painterResource(R.drawable.music_playlist)
            ),
            NavigationTab(
                stringResource(R.string.favorites),
                painterResource(R.drawable.favorite_border)
            ),
            NavigationTab(
                stringResource(R.string.your_music),
                painterResource(R.drawable.song)
            ),
            NavigationTab(
                stringResource(R.string.albums),
                painterResource(R.drawable.music_album)
            ),
            NavigationTab(
                stringResource(R.string.artists),
                painterResource(R.drawable.artist)
            )
        )

        val coroutineScope = rememberCoroutineScope()

        // Track if we're programmatically scrolling to avoid auto-selection conflicts
        var isProgrammaticScroll by remember { mutableStateOf(false) }

        // Track user scroll state
        val isScrolling = listState.isScrollInProgress
        var wasScrolling by remember { mutableStateOf(false) }

        // Auto-scroll to center selected tab (only when tab is clicked)
        LaunchedEffect(selectedTab) {
            if (!isScrolling && selectedTab in 0 until tabs.size) {
                isProgrammaticScroll = true
                coroutineScope.launch {
                    try {
                        listState.animateScrollToItem(
                            index = selectedTab,
                            scrollOffset = 0
                        )
                        // Add small delay to ensure animation completes
                        delay(300)
                    } catch (_: Exception) {
                        // Handle any scroll exceptions gracefully
                    } finally {
                        isProgrammaticScroll = false
                    }
                }
            }
        }

        // Auto-select and snap to closest tab when scrolling stops
        LaunchedEffect(isScrolling, isProgrammaticScroll) {
            if (wasScrolling && !isScrolling && !isProgrammaticScroll) {
                val layoutInfo = listState.layoutInfo
                val viewportCenter =
                    layoutInfo.viewportStartOffset + layoutInfo.viewportSize.width / 2

                var closestIndex = selectedTab
                var minDistance = Float.MAX_VALUE

                layoutInfo.visibleItemsInfo.forEach { item ->
                    val itemCenter = item.offset + item.size / 2
                    val distance = kotlin.math.abs(itemCenter - viewportCenter).toFloat()
                    if (distance < minDistance && item.index < tabs.size) {
                        minDistance = distance
                        closestIndex = item.index
                    }
                }

                if (closestIndex in 0 until tabs.size) {
                    onTabSelected(closestIndex) // Update selection

                    // Snap the scroll so the tab is centered
                    isProgrammaticScroll = true
                    coroutineScope.launch {
                        listState.animateScrollToItem(closestIndex, scrollOffset = 0)
                        delay(250) // ensure animation completes
                        isProgrammaticScroll = false
                    }
                }
            }
            wasScrolling = isScrolling
        }


        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(90.dp)
        ) {
            LazyRow(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(
                    start = LocalConfiguration.current.screenWidthDp.dp / 2 - 37.dp, // Half screen minus half tab width
                    end = LocalConfiguration.current.screenWidthDp.dp / 2 - 37.dp
                ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                itemsIndexed(tabs) { index, tab ->
                    CarouselTab(
                        tab = tab,
                        selected = selectedTab == index,
                        onClick = { onTabSelected(index) },
                        index = index,
                        selectedIndex = selectedTab,
                        listState = listState
                    )
                }
            }
        }
    }
}

@Composable
private fun CarouselTab(
    tab: NavigationTab,
    selected: Boolean,
    onClick: () -> Unit,
    index: Int,
    selectedIndex: Int,
    listState: LazyListState
) {
    // Calculate dynamic properties based on scroll position for this specific tab
    val layoutInfo = remember { derivedStateOf { listState.layoutInfo } }.value
    val viewportCenter = layoutInfo.viewportStartOffset + layoutInfo.viewportSize.width / 2

    // Find this item's info
    val itemInfo = layoutInfo.visibleItemsInfo.find { it.index == index }
    val isScrolling = listState.isScrollInProgress

    // Calculate individual tab's position-based properties
    val (targetScale, targetAlpha, targetRotation) = if (itemInfo != null) {
        val itemCenter = itemInfo.offset + itemInfo.size / 2
        val distanceFromCenter = itemCenter - viewportCenter
        val absoluteDistance = kotlin.math.abs(distanceFromCenter).toFloat()
        val maxDistance = layoutInfo.viewportSize.width / 2f
        val normalizedDistance = (absoluteDistance / maxDistance).coerceIn(0f, 1.5f)

        // Scale: Larger when closer to center
        val scale = (1.15f - (normalizedDistance * 0.35f)).coerceIn(0.75f, 1.15f)

        // Alpha: More opaque when closer to center
        val alpha = (1f - (normalizedDistance * 0.45f)).coerceIn(0.5f, 1f)

        // Rotation: Based on actual position relative to center
        val rotation = if (selected) 0f else {
            val maxRotation = 15f
            val rotationFactor = (distanceFromCenter / maxDistance).coerceIn(-1f, 1f)
            rotationFactor * maxRotation
        }

        Triple(scale, alpha, rotation)
    } else {
        // Fallback for non-visible items - use selection-based logic
        val distance = kotlin.math.abs(index - selectedIndex)
        val fallbackScale = when {
            selected -> 1.1f
            distance == 1 -> 0.9f
            distance >= 2 -> 0.8f
            else -> 1f
        }
        val fallbackAlpha = when {
            selected -> 1f
            distance == 1 -> 0.9f
            distance >= 2 -> 0.7f
            else -> 1f
        }
        val fallbackRotation = when {
            selected -> 0f
            index < selectedIndex -> -8f
            index > selectedIndex -> 8f
            else -> 0f
        }
        Triple(fallbackScale, fallbackAlpha, fallbackRotation)
    }

    val animatedScale by animateFloatAsState(
        targetValue = targetScale,
        animationSpec = if (isScrolling) {
            spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessHigh)
        } else {
            spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMedium
            )
        },
        label = "tabScale"
    )

    val animatedAlpha by animateFloatAsState(
        targetValue = targetAlpha,
        animationSpec = if (isScrolling) {
            tween(durationMillis = 50, easing = LinearEasing) // Very fast during scroll
        } else {
            tween(durationMillis = 400, easing = FastOutSlowInEasing)
        },
        label = "tabAlpha"
    )

    // Color animations
    val animatedContentColor by animateColorAsState(
        targetValue = if (selected)
            Color.Black
        else
            Color.Black.copy(alpha = 0.6f),
        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
        label = "contentColor"
    )

    val animatedBackgroundColor by animateColorAsState(
        targetValue = if (selected)
            colorScheme.secondary
        else
            colorScheme.onSurface.copy(0.6f),
        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
        label = "backgroundColor"
    )

    val animatedRotation by animateFloatAsState(
        targetValue = targetRotation,
        animationSpec = if (isScrolling) {
            spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessHigh)
        } else {
            spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMedium
            )
        },
        label = "tabRotation"
    )

    Card(
        modifier = Modifier
            .width(74.dp)
            .height(84.dp)
            .scale(animatedScale)
            .alpha(animatedAlpha)
            .rotate(animatedRotation)
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = animatedBackgroundColor
        ),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Icon with enhanced bounce animation
            val baseIconScale = 1f + (targetScale - 1f) * 0.8f // Scale proportionally with tab
            val iconScale by animateFloatAsState(
                targetValue = baseIconScale,
                animationSpec = if (isScrolling) {
                    spring(
                        dampingRatio = Spring.DampingRatioNoBouncy,
                        stiffness = Spring.StiffnessHigh
                    )
                } else {
                    spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMedium
                    )
                },
                label = "iconScale"
            )

            Box(
                modifier = Modifier
                    .size(32.dp)
                    .scale(iconScale),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = tab.icon,
                    contentDescription = tab.title,
                    modifier = Modifier.size(24.dp),
                    tint = animatedContentColor
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = tab.title,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                color = animatedContentColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontSize = if (selected) 11.sp else 10.sp
            )
        }
    }
}

@Composable
fun NoRtl(content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        content()
    }
}

data class NavigationTab(
    val title: String,
    val icon: Painter
)