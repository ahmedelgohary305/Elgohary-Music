package com.example.elgoharymusic.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.elgoharymusic.domain.models.Song
import com.example.elgoharymusic.domain.repo.FavSongsRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class FavViewModel @Inject constructor(
    private val favSongsRepo: FavSongsRepo
): ViewModel() {
    private val _favSongs = MutableStateFlow<List<Song>>(emptyList())
    val favSongs: StateFlow<List<Song>> = _favSongs

    init {
        getAllSongs()
    }

    fun getAllSongs() {
        viewModelScope.launch {
            _favSongs.value = favSongsRepo.getAllSongs()
        }
    }

    fun toggleFavorite(song: Song) {
        viewModelScope.launch {
            if (_favSongs.value.any { it.id == song.id }) {
                favSongsRepo.deleteSongById(song.id)
            } else {
                favSongsRepo.insertSong(song)
            }
            _favSongs.value = favSongsRepo.getAllSongs()
        }
    }

    fun updateSongMetadataInFavorites(songId: Long, title: String, artist: String, album: String?) {
        viewModelScope.launch {
            favSongsRepo.updateSongMetadataInFavorites(songId, title, artist, album)
            _favSongs.value = favSongsRepo.getAllSongs()
        }
    }

    fun removeFromFavorites(song: Song) {
        viewModelScope.launch {
            favSongsRepo.deleteSongById(song.id)
            _favSongs.value = favSongsRepo.getAllSongs()
        }
    }

    fun isFavorite(song: Song): Boolean {
        return _favSongs.value.any { it.id == song.id }
    }
}