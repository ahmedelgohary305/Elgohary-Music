package com.example.elgoharymusic.data.local

import android.net.Uri
import com.example.elgoharymusic.domain.models.Playlist
import com.example.elgoharymusic.domain.models.Song

fun SongEntity.toSong() : Song{
    return Song(
        id = id,
        title = title,
        artist = artist,
        duration = duration,
        uri = uri.toUri(),
        albumArtUri = albumArtUri?.toUri()
    )
}

fun List<SongEntity>.toPlaylistSongs(): List<Song>{
    return map {
        Song(
            id = it.id,
            title = it.title,
            artist = it.artist,
            duration = it.duration,
            uri = it.uri.toUri(),
            albumArtUri = it.albumArtUri?.toUri()
        )
    }
}

fun String.toUri(): Uri {
    return Uri.parse(this)
}

fun Song.toSongEntity(): SongEntity{
    return SongEntity(
        id = id,
        title = title,
        artist = artist,
        duration = duration,
        uri = uri.toString(),
        albumArtUri = albumArtUri?.toString()
    )
}

fun PlaylistWithSongs.toPlaylist(): Playlist {
    return Playlist(
        id = playlist.id,
        name = playlist.name,
        createdAt = playlist.createdAt,
        description = playlist.description,
        songs = songs.toPlaylistSongs()
    )
}

fun List<PlaylistWithSongs>.toPlaylistsWithSongs(): List<Playlist> {
    return map { it.toPlaylist() }
}

fun Playlist.toPlaylistEntity(): PlaylistEntity {
    return PlaylistEntity(
        id = id,
        name = name,
        createdAt = createdAt,
        description = description
    )
}