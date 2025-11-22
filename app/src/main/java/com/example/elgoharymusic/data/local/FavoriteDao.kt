package com.example.elgoharymusic.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction

@Dao
interface FavoriteDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertSong(song: SongEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addToFavorites(favorite: FavoriteSongEntity)

    @Query(
        """
    UPDATE SongEntity 
    SET title = :title, artist = :artist, album = :album 
    WHERE id = :songId
"""
    )
    suspend fun updateSongMetadataInFavorites(
        songId: Long,
        title: String,
        artist: String,
        album: String?
    )

    @Query("DELETE FROM favorites WHERE songId IN (:songIds)")
    suspend fun removeFromFavorites(songIds: List<Long>)

    @Transaction
    @Query("SELECT * FROM SongEntity INNER JOIN favorites ON SongEntity.id = favorites.songId")
    suspend fun getAllFavoriteSongs(): List<SongEntity>
}
