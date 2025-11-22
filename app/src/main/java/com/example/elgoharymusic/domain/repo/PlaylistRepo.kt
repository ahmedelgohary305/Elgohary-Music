package com.example.elgoharymusic.domain.repo

import com.example.elgoharymusic.domain.models.Playlist
import com.example.elgoharymusic.domain.models.Song

interface PlaylistRepo {
    suspend fun getAllPlaylistsWithSongs(): List<Playlist>
    suspend fun getPlaylistWithSongs(playlistId: Long): Playlist?
    suspend fun createPlaylist(name: String, description: String?): Long
    suspend fun updatePlaylist(playlist: Playlist)
    suspend fun deletePlaylist(playlistId: Long)
    suspend fun addSongToPlaylist(playlistId: Long, playlistSong: Song)
    suspend fun removeSongFromPlaylist(playlistId: Long, songId: Long)
    suspend fun removeSongFromAllPlaylists(songId: Long) // ✅ New
    suspend fun removeSongsFromAllPlaylists(songIds: List<Long>)
    suspend fun updateSongMetadataInAllPlaylists(
        songId: Long,
        title: String,
        artist: String,
        album: String?
    )
}