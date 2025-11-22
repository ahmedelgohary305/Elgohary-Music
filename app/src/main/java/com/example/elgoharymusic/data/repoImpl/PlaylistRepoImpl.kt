package com.example.elgoharymusic.data.repoImpl

import com.example.elgoharymusic.domain.models.Playlist
import com.example.elgoharymusic.data.local.*
import com.example.elgoharymusic.domain.models.Song
import com.example.elgoharymusic.domain.repo.PlaylistRepo
import com.example.elgoharymusic.presentation.utils.TimeFormatter
import javax.inject.Inject

class PlaylistRepoImpl @Inject constructor(
    private val database: SongDatabase
): PlaylistRepo {

    override suspend fun getAllPlaylistsWithSongs(): List<Playlist> {
        return database.playlistDao.getAllPlaylistsWithSongs().toPlaylistsWithSongs()
    }

    override suspend fun getPlaylistWithSongs(playlistId: Long): Playlist? {
        return database.playlistDao.getPlaylistWithSongs(playlistId)?.toPlaylist()
    }

    override suspend fun createPlaylist(name: String, description: String?): Long {
        val playlist = PlaylistEntity(
            name = name,
            description = description,
            createdAt = TimeFormatter.getCurrentTimestamp()
        )
        return database.playlistDao.insertPlaylist(playlist)
    }

    override suspend fun updatePlaylist(playlist: Playlist) {
        database.playlistDao.updatePlaylist(playlist.toPlaylistEntity())
    }

    override suspend fun deletePlaylist(playlistId: Long) {
        database.playlistDao.deletePlaylist(playlistId)
    }

    override suspend fun addSongToPlaylist(playlistId: Long, playlistSong: Song) {
        database.playlistDao.addSongToPlaylist(playlistId, playlistSong.toSongEntity())
    }

    override suspend fun removeSongFromPlaylist(playlistId: Long, songId: Long) {
        database.playlistDao.removeSongFromPlaylist(playlistId, songId)
    }

    // ✅ New: Remove song from all playlists
    override suspend fun removeSongsFromAllPlaylists(songIds: List<Long>) {
        database.playlistDao.removeSongsFromAllPlaylists(songIds)
    }

    override suspend fun removeSongFromAllPlaylists(songId: Long) {
        removeSongsFromAllPlaylists(listOf(songId))
    }

    override suspend fun updateSongMetadataInAllPlaylists(
        songId: Long,
        title: String,
        artist: String,
        album: String?
    ) {
        database.playlistDao.updateSongMetadataInAllPlaylists(songId, title, artist, album)
    }
}
