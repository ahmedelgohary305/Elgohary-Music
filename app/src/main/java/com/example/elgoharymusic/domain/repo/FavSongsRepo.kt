package com.example.elgoharymusic.domain.repo

import com.example.elgoharymusic.domain.models.Song

interface FavSongsRepo {
    suspend fun getAllSongs(): List<Song>
    suspend fun insertSong(song: Song)
    suspend fun updateSongMetadataInFavorites(
        songId: Long,
        title: String,
        artist: String,
        album: String?
    )
    suspend fun deleteSongById(id: Long)
    suspend fun deleteSongsByIds(ids: List<Long>)
}