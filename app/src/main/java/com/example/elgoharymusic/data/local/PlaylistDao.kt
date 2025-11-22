package com.example.elgoharymusic.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update

@Dao
interface PlaylistDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertSong(song: SongEntity)

    @Transaction
    @Query("SELECT * FROM PlaylistEntity ORDER BY createdAt DESC")
    suspend fun getAllPlaylistsWithSongs(): List<PlaylistWithSongs>

    @Transaction
    @Query("SELECT * FROM PlaylistEntity WHERE id = :playlistId")
    suspend fun getPlaylistWithSongs(playlistId: Long): PlaylistWithSongs?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylist(playlist: PlaylistEntity): Long

    @Update
    suspend fun updatePlaylist(playlist: PlaylistEntity)

    @Query("DELETE FROM PlaylistEntity WHERE id = :playlistId")
    suspend fun deletePlaylist(playlistId: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylistSongCrossRef(crossRef: PlaylistSongCrossRef)

    @Query("DELETE FROM playlist_song_cross_ref WHERE playlistId = :playlistId AND songId = :songId")
    suspend fun removeSongFromPlaylist(playlistId: Long, songId: Long)

    @Query("SELECT COALESCE(MAX(position), -1) + 1 FROM playlist_song_cross_ref WHERE playlistId = :playlistId")
    suspend fun getNextPosition(playlistId: Long): Int

    // Remove song from all playlists
    @Query("DELETE FROM playlist_song_cross_ref WHERE songId IN (:songIds)")
    suspend fun removeSongsFromAllPlaylists(songIds: List<Long>)

    @Query(
        """
    UPDATE SongEntity 
    SET title = :title, artist = :artist, album = :album 
    WHERE id = :songId
"""
    )
    suspend fun updateSongMetadataInAllPlaylists(
        songId: Long,
        title: String,
        artist: String,
        album: String?
    )

    @Transaction
    suspend fun addSongToPlaylist(playlistId: Long, song: SongEntity) {
        insertSong(song)

        val position = getNextPosition(playlistId)
        insertPlaylistSongCrossRef(
            PlaylistSongCrossRef(
                playlistId = playlistId,
                songId = song.id,
                position = position
            )
        )
    }
}