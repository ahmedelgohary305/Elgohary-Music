package com.example.elgoharymusic.presentation.viewmodels


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.elgoharymusic.domain.models.Playlist
import com.example.elgoharymusic.domain.models.Song
import com.example.elgoharymusic.domain.repo.PlaylistRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlaylistViewModel @Inject constructor(
    private val playlistRepo: PlaylistRepo
) : ViewModel() {

    private val _playlists = MutableStateFlow<List<Playlist>>(emptyList())
    val playlists: StateFlow<List<Playlist>> = _playlists.asStateFlow()

    private val _currentPlaylist = MutableStateFlow<Playlist?>(null)
    val currentPlaylist: StateFlow<Playlist?> = _currentPlaylist.asStateFlow()

    init {
        loadPlaylists()
    }

    fun loadPlaylists() {
        viewModelScope.launch {
            _playlists.value = playlistRepo.getAllPlaylistsWithSongs()
        }
    }

    fun createPlaylistWithSongs(
        name: String,
        description: String?,
        songs: List<Song>,
        onPlaylistCreated: (Long) -> Unit
    ) {
        viewModelScope.launch {
            val playlistId = playlistRepo.createPlaylist(name, description)
            songs.map { song ->
                async { playlistRepo.addSongToPlaylist(playlistId, song) }
            }.awaitAll()
            loadPlaylists()
            onPlaylistCreated(playlistId)
        }
    }

    fun deletePlaylist(playlistId: Long) {
        viewModelScope.launch {
            playlistRepo.deletePlaylist(playlistId)
            loadPlaylists()
        }
    }

    fun updatePlaylist(playlist: Playlist) {
        viewModelScope.launch {
            playlistRepo.updatePlaylist(playlist)
            loadPlaylists()
        }
    }

    fun loadPlaylistWithSongs(playlistId: Long) {
        viewModelScope.launch {
            _currentPlaylist.value = playlistRepo.getPlaylistWithSongs(playlistId)
        }
    }

    fun addSongToPlaylist(playlistId: Long, song: Song) {
        viewModelScope.launch {
            playlistRepo.addSongToPlaylist(playlistId, song)
            loadPlaylists()
            if (_currentPlaylist.value?.id == playlistId) {
                loadPlaylistWithSongs(playlistId)
            }
        }
    }

    fun removeSongFromPlaylist(playlistId: Long, songId: Long) {
        viewModelScope.launch {
            playlistRepo.removeSongFromPlaylist(playlistId, songId)
            loadPlaylists()
            if (_currentPlaylist.value?.id == playlistId) {
                loadPlaylistWithSongs(playlistId)
            }
        }
    }

    fun removeSongsFromAllPlaylists(songIds: List<Long>) {
        viewModelScope.launch {
            playlistRepo.removeSongsFromAllPlaylists(songIds)
            loadPlaylists()
        }
    }

    fun updateSongMetadataInAllPlaylists(
        songId: Long,
        title: String,
        artist: String,
        album: String?
    ){
        viewModelScope.launch {
            playlistRepo.updateSongMetadataInAllPlaylists(songId, title, artist, album)
            loadPlaylists()
            if (_currentPlaylist.value?.songs?.any { it.id == songId } == true) {
                loadPlaylistWithSongs(_currentPlaylist.value!!.id)
            }
        }
    }
}