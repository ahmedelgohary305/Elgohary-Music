package com.example.elgoharymusic.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [SongEntity::class, PlaylistEntity::class, PlaylistSongCrossRef::class, FavoriteSongEntity::class],
    version = 22,
    exportSchema = false
)
abstract class SongDatabase: RoomDatabase() {
    abstract val favoriteDao: FavoriteDao
    abstract val playlistDao: PlaylistDao
}