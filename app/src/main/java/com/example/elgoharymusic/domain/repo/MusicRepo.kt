package com.example.elgoharymusic.domain.repo

import com.example.elgoharymusic.domain.models.Album
import com.example.elgoharymusic.domain.models.Artist
import com.example.elgoharymusic.domain.models.Song
import kotlinx.coroutines.flow.SharedFlow

interface MusicRepo {
     fun getSongs(): List<Song>
     fun getArtists(): List<Artist>
     fun getAlbums(): List<Album>
    fun hasStoragePermission(): Boolean
    suspend fun updateSongMetadata(
        songId: Long,
        title: String,
        artist: String,
        album: String?
    ): Result<Unit>
    suspend fun deleteSong(song: Song): Result<Unit>
    suspend fun deleteSongs(songs: List<Song>): Result<Unit>

    // ✅ New: Notify other repos about deleted songs
    suspend fun notifySongsDeleted(songIds: List<Long>)
    val mediaStoreChangeFlow: SharedFlow<Unit>
    fun startObservingMediaStoreChanges()
    fun stopObservingMediaStoreChanges()

}