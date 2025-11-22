package com.example.elgoharymusic.data.repoImpl

import com.example.elgoharymusic.data.local.FavoriteSongEntity
import com.example.elgoharymusic.data.local.SongDatabase
import com.example.elgoharymusic.data.local.toSongEntity
import com.example.elgoharymusic.data.local.toSong
import com.example.elgoharymusic.domain.models.Song
import com.example.elgoharymusic.domain.repo.FavSongsRepo
import javax.inject.Inject

class FavSongsRepoImpl @Inject constructor(
    private val database: SongDatabase
) : FavSongsRepo {

    override suspend fun getAllSongs(): List<Song> {
        return database.favoriteDao.getAllFavoriteSongs().map { it.toSong() }
    }

    override suspend fun insertSong(song: Song) {
        database.favoriteDao.insertSong(song.toSongEntity())
        database.favoriteDao.addToFavorites(FavoriteSongEntity(song.id))
    }

    override suspend fun deleteSongById(id: Long) {
        deleteSongsByIds(listOf(id))
    }

    override suspend fun updateSongMetadataInFavorites(
        songId: Long,
        title: String,
        artist: String,
        album: String?
    ) {
        database.favoriteDao.updateSongMetadataInFavorites(songId, title, artist, album)
    }

    // ✅ New: Delete multiple songs from favorites
    override suspend fun deleteSongsByIds(ids: List<Long>) {
        database.favoriteDao.removeFromFavorites(ids)
    }
}