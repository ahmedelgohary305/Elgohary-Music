package com.example.elgoharymusic.data.repoImpl

import android.Manifest
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.database.ContentObserver
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import com.example.elgoharymusic.domain.models.Album
import com.example.elgoharymusic.domain.models.Artist
import com.example.elgoharymusic.domain.models.Song
import com.example.elgoharymusic.domain.repo.FavSongsRepo
import com.example.elgoharymusic.domain.repo.MusicRepo
import com.example.elgoharymusic.domain.repo.PlaylistRepo
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import org.jaudiotagger.audio.AudioFileIO
import org.jaudiotagger.tag.FieldKey
import java.io.File
import javax.inject.Inject

class MusicRepoImpl @Inject constructor(
    private val context: Context,
    private val favSongsRepo: FavSongsRepo,
    private val playlistRepo: PlaylistRepo

) : MusicRepo {
    private var mediaStoreObserver: MediaStoreObserver? = null
    private val _mediaStoreChangeFlow = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    override val mediaStoreChangeFlow = _mediaStoreChangeFlow.asSharedFlow()

    override fun getSongs(): List<Song> {
        val songs = mutableListOf<Song>()
        val collection = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val sArtworkUri = Uri.parse("content://media/external/audio/albumart")
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.ALBUM_ID
            ,MediaStore.Audio.Media.ARTIST_ID
        )
        context.contentResolver.query(
            collection,
            projection,
            null,
            null,
            "${MediaStore.Audio.Media.TITLE} ASC"
        )?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
            val artistColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val albumColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
            val albumIdColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
            val artistIdColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST_ID)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val title = cursor.getString(titleColumn)
                val artist = cursor.getString(artistColumn)
                val duration = cursor.getLong(durationColumn)
                val album = cursor.getString(albumColumn)
                val contentUri = ContentUris.withAppendedId(collection, id)
                val albumId = cursor.getLong(albumIdColumn)
                val artistId = cursor.getLong(artistIdColumn)
                val albumArtUri = ContentUris.withAppendedId(
                    sArtworkUri,
                    albumId
                )
                songs.add(
                    Song(
                        id,
                        title,
                        duration,
                        artistId,
                        artist,
                        albumId,
                        album,
                        contentUri,
                        albumArtUri
                    )
                )
            }
        }
        return songs
    }

    override fun getArtists(): List<Artist> = getGroupedItems(
        groupKeySelector = { it.artistId },
        itemBuilder = { artistId, artistSongs ->
            val firstSong = artistSongs.first()
            Artist(
                artistId = artistId,
                name = firstSong.artist,
                songCount = artistSongs.size,
                songs = artistSongs.sortedBy { it.title },
                albumArtUri = firstSong.albumArtUri
            )
        },
        sortKeySelector = { it.name }
    )


    override fun getAlbums(): List<Album> = getGroupedItems(
        groupKeySelector = { it.albumId },
        itemBuilder = { albumId, albumSongs ->
            val firstSong = albumSongs.first()
            Album(
                albumId = albumId,
                name = firstSong.album ?: "Unknown Album",
                artist = firstSong.artist,
                songCount = albumSongs.size,
                songs = albumSongs.sortedBy { it.title },
                albumArtUri = firstSong.albumArtUri
            )
        },
        sortKeySelector = { it.name }
    )


    override suspend fun updateSongMetadata(
        songId: Long,
        title: String,
        artist: String,
        album: String?
    ): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                // Check if we have the required permission based on Android version
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    if (!Environment.isExternalStorageManager()) {
                        return@withContext Result.failure(
                            SecurityException("MANAGE_EXTERNAL_STORAGE permission not granted")
                        )
                    }
                } else if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                    // Android 9 and below - check WRITE_EXTERNAL_STORAGE
                    if (context.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED
                    ) {
                        return@withContext Result.failure(
                            SecurityException("WRITE_EXTERNAL_STORAGE permission not granted")
                        )
                    }
                }

                // Get the file path from MediaStore
                val contentUri = ContentUris.withAppendedId(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    songId
                )

                val projection = arrayOf(MediaStore.Audio.Media.DATA)
                val cursor = context.contentResolver.query(
                    contentUri,
                    projection,
                    null,
                    null,
                    null
                )

                val filePath = cursor?.use {
                    if (it.moveToFirst()) {
                        it.getString(it.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA))
                    } else null
                } ?: return@withContext Result.failure(Exception("File not found in MediaStore"))

                val audioFile = File(filePath)
                if (!audioFile.exists()) {
                    return@withContext Result.failure(
                        Exception("Audio file does not exist: $filePath")
                    )
                }

                // Check if file is writable
                if (!audioFile.canWrite()) {
                    return@withContext Result.failure(
                        Exception("Cannot write to file: ${audioFile.name}. Check permissions.")
                    )
                }

                // Edit the actual file metadata using JAudioTagger
                try {
                    val audioFileObj = AudioFileIO.read(audioFile)
                    val tag = audioFileObj.tagOrCreateAndSetDefault

                    tag.setField(FieldKey.TITLE, title)
                    tag.setField(FieldKey.ARTIST, artist)
                    album?.let { tag.setField(FieldKey.ALBUM, it) }

                    // Write changes to the file
                    audioFileObj.commit()
                } catch (e: Exception) {
                    return@withContext Result.failure(
                        Exception("Failed to write metadata: ${e.message}", e)
                    )
                }

                // Update MediaStore database
                try {
                    val contentValues = ContentValues().apply {
                        put(MediaStore.Audio.Media.TITLE, title)
                        put(MediaStore.Audio.Media.ARTIST, artist)
                        album?.let { put(MediaStore.Audio.Media.ALBUM, it) }
                    }

                    context.contentResolver.update(contentUri, contentValues, null, null)
                } catch (_: Exception) {
                }

                // Force MediaStore to rescan the file - CRITICAL for Android 9
                val scanCompleted = CompletableDeferred<Boolean>()

                MediaScannerConnection.scanFile(
                    context,
                    arrayOf(audioFile.absolutePath),
                    arrayOf("audio/*")
                ) { path, uri ->
                    scanCompleted.complete(uri != null)
                }

                // Wait for scan to complete with timeout
                withTimeout(3000L) {
                    scanCompleted.await()
                }

                // Additional delay for Android 9 to ensure MediaStore is updated
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                    delay(500)
                }

                Result.success(Unit)

            } catch (_: TimeoutCancellationException) {
                Result.success(Unit) // File was updated, scan timeout is not critical
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    /**
     * Deletes a song from the device
     */
    override suspend fun deleteSong(song: Song): Result<Unit> {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                // Android 11+ - Need MANAGE_EXTERNAL_STORAGE
                if (!Environment.isExternalStorageManager()) {
                    return Result.failure(SecurityException("MANAGE_EXTERNAL_STORAGE permission required"))
                }
            }

            val contentUri = ContentUris.withAppendedId(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                song.id
            )

            val deleted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                context.contentResolver.delete(contentUri, null, null) > 0
            } else {
                try {
                    context.contentResolver.delete(contentUri, null, null) > 0
                } catch (securityException: SecurityException) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        val recoverableException =
                            securityException as? RecoverableSecurityException
                                ?: throw securityException
                        throw recoverableException
                    } else {
                        throw securityException
                    }
                }
            }

            if (deleted) {
                // ✅ Cascade delete from favorites and playlists
                notifySongsDeleted(listOf(song.id))
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to delete song"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteSongs(songs: List<Song>): Result<Unit> {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                if (!Environment.isExternalStorageManager()) {
                    return Result.failure(SecurityException("MANAGE_EXTERNAL_STORAGE permission required"))
                }
            }

            val uris = songs.map { song ->
                ContentUris.withAppendedId(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    song.id
                )
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                var deletedCount = 0
                uris.forEach { uri ->
                    if (context.contentResolver.delete(uri, null, null) > 0) {
                        deletedCount++
                    }
                }

                if (deletedCount > 0) {
                    // ✅ Cascade delete from favorites and playlists
                    notifySongsDeleted(songs.map { it.id })
                }

                if (deletedCount == songs.size) {
                    Result.success(Unit)
                } else {
                    Result.failure(Exception("Failed to delete some songs"))
                }
            } else {
                try {
                    var deletedCount = 0
                    uris.forEach { uri ->
                        if (context.contentResolver.delete(uri, null, null) > 0) {
                            deletedCount++
                        }
                    }

                    if (deletedCount > 0) {
                        // ✅ Cascade delete from favorites and playlists
                        notifySongsDeleted(songs.map { it.id })
                    }

                    if (deletedCount == songs.size) {
                        Result.success(Unit)
                    } else {
                        Result.failure(Exception("Failed to delete some songs"))
                    }
                } catch (securityException: SecurityException) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        val recoverableException =
                            securityException as? RecoverableSecurityException
                                ?: throw securityException

                        throw recoverableException
                    } else {
                        throw securityException
                    }
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ✅ New: Cascade delete to favorites and playlists
    override suspend fun notifySongsDeleted(songIds: List<Long>) {
        favSongsRepo.deleteSongsByIds(songIds)
        playlistRepo.removeSongsFromAllPlaylists(songIds)
    }

    /**
     * Check if the app has permission to edit files
     */
    override fun hasStoragePermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Environment.isExternalStorageManager()
        } else {
            true
        }
    }

    override fun startObservingMediaStoreChanges() {
        mediaStoreObserver?.unregister()
        mediaStoreObserver = MediaStoreObserver(context) {
            // Emit an event whenever MediaStore changes
            _mediaStoreChangeFlow.tryEmit(Unit)
        }.also { it.register() }
    }

    override fun stopObservingMediaStoreChanges() {
        mediaStoreObserver?.unregister()
        mediaStoreObserver = null
    }

    private inline fun <T> getGroupedItems(
        crossinline groupKeySelector: (Song) -> Long?,
        crossinline itemBuilder: (Long, List<Song>) -> T,
        crossinline sortKeySelector: (T) -> String
    ): List<T> {
        return getSongs()
            .groupBy { groupKeySelector(it) }
            .mapNotNull { (id, groupedSongs) ->
                id?.let { itemBuilder(it, groupedSongs) }
            }
            .sortedBy(sortKeySelector)
    }

}

private class MediaStoreObserver(
    private val context: Context,
    private val onChange: () -> Unit
) : ContentObserver(Handler(Looper.getMainLooper())) {
    override fun onChange(selfChange: Boolean) {
        super.onChange(selfChange)
        onChange()
    }

    fun register() {
        context.contentResolver.registerContentObserver(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            true,
            this
        )
    }

    fun unregister() {
        context.contentResolver.unregisterContentObserver(this)
    }
}


class RecoverableSecurityException(
    message: String,
    val intentSender: android.content.IntentSender
) : SecurityException(message)