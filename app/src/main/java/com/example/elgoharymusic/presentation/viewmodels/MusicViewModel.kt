package com.example.elgoharymusic.presentation.viewmodels


import android.content.Context
import android.content.IntentSender
import androidx.activity.ComponentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.elgoharymusic.data.repoImpl.AppLanguage
import com.example.elgoharymusic.data.repoImpl.RecoverableSecurityException
import com.example.elgoharymusic.domain.models.Album
import com.example.elgoharymusic.domain.models.Artist
import com.example.elgoharymusic.domain.models.Song
import com.example.elgoharymusic.domain.repo.AppPreferencesRepo
import com.example.elgoharymusic.domain.repo.MusicRepo
import com.example.elgoharymusic.presentation.MusicController
import com.example.elgoharymusic.saveLanguagePreference
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

// RepeatMode enum
enum class RepeatMode {
    OFF,    // No repeat
    ALL,    // Repeat all songs in queue
    ONE     // Repeat current song
}

enum class SortOrder {
    TITLE_ASC,
    ARTIST_ASC,
    DURATION_ASC,
}

// Updated MusicViewModel.kt
@HiltViewModel
class MusicViewModel @Inject constructor(
    private val musicRepo: MusicRepo,
    private val musicController: MusicController,
    private val appPreferencesRepo: AppPreferencesRepo,
    @ApplicationContext private val context: Context
) : ViewModel() {

    // Preferences
    private val _sortOrder = MutableStateFlow(SortOrder.TITLE_ASC)
    val sortOrder: StateFlow<SortOrder> = _sortOrder

    private val _isDarkTheme = MutableStateFlow(false)
    val isDarkTheme: StateFlow<Boolean> = _isDarkTheme

    private val _language = MutableStateFlow(AppLanguage.ENGLISH)
    val language: StateFlow<AppLanguage> = _language

    // Settings Drawer
    private val _isSettingsDrawerOpen = MutableStateFlow(false)
    val isSettingsDrawerOpen: StateFlow<Boolean> = _isSettingsDrawerOpen

    // Playlist Dialog
    private val _showCreatePlaylistDialog = MutableStateFlow(false)
    val showCreatePlaylistDialog: StateFlow<Boolean> = _showCreatePlaylistDialog

    private val _selectedSongsForPlaylist = MutableStateFlow<List<Song>>(emptyList())
    val selectedSongsForPlaylist: StateFlow<List<Song>> = _selectedSongsForPlaylist

    private val _songs = MutableStateFlow<List<Song>>(emptyList())
    val songs: StateFlow<List<Song>> = _songs

    private val _artists = MutableStateFlow<List<Artist>>(emptyList())
    val artists: StateFlow<List<Artist>> = _artists

    private val _albums = MutableStateFlow<List<Album>>(emptyList())
    val albums: StateFlow<List<Album>> = _albums

    // Search related state
    private val _isSearchActive = MutableStateFlow(false)
    val isSearchActive: StateFlow<Boolean> = _isSearchActive

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _filteredSongs = MutableStateFlow<List<Song>>(emptyList())
    val filteredSongs: StateFlow<List<Song>> = _filteredSongs

    private val _filteredArtists = MutableStateFlow<List<Artist>>(emptyList())
    val filteredArtists: StateFlow<List<Artist>> = _filteredArtists

    // Storage permission state
    private val _hasStoragePermission = MutableStateFlow(false)

    private val _isSelectionMode = MutableStateFlow(false)
    val isSelectionMode: StateFlow<Boolean> = _isSelectionMode

    private val _selectedSongs = MutableStateFlow<Set<Long>>(emptySet())
    val selectedSongs: StateFlow<Set<Long>> = _selectedSongs

    val currentSong = musicController.currentSong
    val isPlaying = musicController.isPlaying
    val currentPosition = musicController.currentPosition
    val isShuffleEnabled = musicController.isShuffleEnabled
    val repeatMode = musicController.repeatMode
    val queueSongs = musicController.queueSongs

    init {
        observeMediaStoreChanges()
        observeSearchFiltering()
        observePreferences()
        initializeController()
        checkStoragePermission()
    }

    private fun observeMediaStoreChanges() {
        musicRepo.startObservingMediaStoreChanges()

        viewModelScope.launch {
            musicRepo.mediaStoreChangeFlow.collectLatest {
                loadSongs()
            }
        }
    }

    private fun observeSearchFiltering() {
        viewModelScope.launch {
            combine(_searchQuery, _songs, _artists, _albums) { query, allSongs, allArtists, allAlbums ->
                if (query.isBlank()) {
                    _filteredSongs.value = allSongs
                    _filteredArtists.value = allArtists
                } else {
                    val lowerQuery = query.lowercase()
                    _filteredSongs.value = allSongs.filter { song ->
                        song.title.contains(lowerQuery, ignoreCase = true) ||
                                song.artist.contains(lowerQuery, ignoreCase = true) ||
                                song.album?.contains(lowerQuery, ignoreCase = true) == true
                    }
                    _filteredArtists.value = allArtists.filter { artist ->
                        artist.name.contains(lowerQuery, ignoreCase = true)
                    }
                }
            }.collect()
        }
    }

    private fun observePreferences() {
        viewModelScope.launch {
            appPreferencesRepo.getPreferencesFlow().collectLatest { prefs ->
                _sortOrder.value = prefs.sortOrder
                _isDarkTheme.value = prefs.isDarkTheme
                _language.value = prefs.language
            }
        }
    }

    private fun initializeController() {
        musicController.initialize()
    }

    // Settings Functions
    fun openSettingsDrawer() {
        _isSettingsDrawerOpen.value = true
    }

    fun closeSettingsDrawer() {
        _isSettingsDrawerOpen.value = false
    }

    fun setDarkTheme(isDark: Boolean) {
        viewModelScope.launch {
            appPreferencesRepo.setIsDarkTheme(isDark)
        }
    }

    fun setLanguage(language: AppLanguage) {
        viewModelScope.launch {
            // Save to DataStore
            appPreferencesRepo.setLanguage(language)

            // Save to SharedPreferences for attachBaseContext
            context.saveLanguagePreference(language)

            // Trigger activity recreation to apply RTL
            if (context is ComponentActivity) {
                withContext(Dispatchers.Main) {
                    context.recreate()
                }
            }
        }
    }

    fun setSortOrder(order: SortOrder) {
        viewModelScope.launch {
            appPreferencesRepo.setSortOrder(order)
        }
    }

    fun getSortedSongs(songs: List<Song>, order: SortOrder): List<Song> {
        return when (order) {
            SortOrder.TITLE_ASC -> songs.sortedBy { it.title.lowercase() }
            SortOrder.ARTIST_ASC -> songs.sortedBy { it.artist.lowercase() }
            SortOrder.DURATION_ASC -> songs.sortedBy { it.duration }
        }
    }

    fun showCreatePlaylistDialog(show: Boolean) {
        _showCreatePlaylistDialog.value = show
    }

    fun setSelectedSongsForPlaylist(songs: List<Song>) {
        _selectedSongsForPlaylist.value = songs
    }

    fun checkStoragePermission() {
        _hasStoragePermission.value = musicRepo.hasStoragePermission()
    }

    fun setStoragePermissionGranted(granted: Boolean) {
        _hasStoragePermission.value = granted
    }

    fun enterSelectionMode(songId: Long) {
        _isSelectionMode.value = true
        _selectedSongs.value = setOf(songId)
    }

    fun exitSelectionMode() {
        _isSelectionMode.value = false
        _selectedSongs.value = emptySet()
    }

    fun toggleSongSelection(songId: Long) {
        _selectedSongs.value = if (_selectedSongs.value.contains(songId)) {
            _selectedSongs.value - songId
        } else {
            _selectedSongs.value + songId
        }

        if (_selectedSongs.value.isEmpty()) {
            _isSelectionMode.value = false
        }
    }

    fun selectAllSongs(songs: List<Song>) {
        _selectedSongs.value = songs.map { it.id }.toSet()
    }

    fun loadSongs() {
        viewModelScope.launch(Dispatchers.IO) {
            _songs.value = musicRepo.getSongs()
            _artists.value = musicRepo.getArtists()
            _albums.value = musicRepo.getAlbums()
        }
    }

    fun playSongs(songs: List<Song>, startIndex: Int = 0) {
        musicController.playSongs(songs, startIndex)
    }

    suspend fun updateSongMetadata(
        songId: Long,
        title: String,
        artist: String,
        album: String?
    ): IntentSender? {
        if (!_hasStoragePermission.value) {
            throw SecurityException("Storage permission not granted")
        }

        return withContext(Dispatchers.IO) {
            val result = musicRepo.updateSongMetadata(songId, title, artist, album)

            result.fold(
                onSuccess = {
                    loadSongs()
                    null
                },
                onFailure = { error ->
                    when (error) {
                        is RecoverableSecurityException -> error.intentSender
                        else -> throw error
                    }
                }
            )
        }
    }

    suspend fun deleteSongs(songs: List<Song>): IntentSender? {
        if (!_hasStoragePermission.value) {
            throw SecurityException("Storage permission not granted")
        }

        return withContext(Dispatchers.IO) {
            val result = if (songs.size == 1) {
                musicRepo.deleteSong(songs.first())
            } else {
                musicRepo.deleteSongs(songs)
            }

            result.fold(
                onSuccess = {
                    withContext(Dispatchers.Main) {
                        songs.forEach { song ->
                            musicController.removeFromQueue(song)
                        }
                    }
                    loadSongs()
                    null
                },
                onFailure = { error ->
                    when (error) {
                        is RecoverableSecurityException -> error.intentSender
                        else -> throw error
                    }
                }
            )
        }
    }

    fun shuffleAndPlay(songList: List<Song>) {
        if (songList.isNotEmpty()) {
            val shuffledList = songList.shuffled()
            musicController.playSongs(shuffledList, 0)
            if (!musicController.isShuffleEnabled.value) {
                musicController.toggleShuffle()
            }
        }
    }

    fun activateSearch() {
        _isSearchActive.value = true
    }

    fun deactivateSearch() {
        _isSearchActive.value = false
        _searchQuery.value = ""
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun playSong(song: Song, songList: List<Song>? = null) {
        val listToUse = songList ?: if (_isSearchActive.value) _filteredSongs.value else _songs.value
        val songIndex = listToUse.indexOf(song)
        if (songIndex != -1) {
            musicController.playSongs(listToUse, songIndex)
        }
    }

    fun playPause() {
        musicController.playPause()
    }

    fun seekTo(position: Long) {
        musicController.seekTo(position)
    }

    fun skipToNext() {
        musicController.skipToNext()
    }

    fun skipToPrevious() {
        musicController.skipToPrevious()
    }

    fun toggleShuffle() {
        musicController.toggleShuffle()
    }

    fun toggleRepeat() {
        musicController.toggleRepeat()
    }

    fun playSongFromQueue(song: Song) {
        musicController.playSongFromQueue(song)
    }

    fun removeFromQueue(song: Song) {
        musicController.removeFromQueue(song)
    }

    fun addToQueue(songs: List<Song>) {
        musicController.addToQueue(songs)
    }

    fun reorderQueue(fromIndex: Int, toIndex: Int) {
        musicController.reorderQueue(fromIndex, toIndex)
    }

    fun clearQueue() {
        musicController.clearQueue()
    }

    override fun onCleared() {
        super.onCleared()
        musicController.release()
        musicRepo.stopObservingMediaStoreChanges()
    }
}