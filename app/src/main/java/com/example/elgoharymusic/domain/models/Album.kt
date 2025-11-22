package com.example.elgoharymusic.domain.models

import android.net.Uri

data class Album(
    val albumId: Long,
    val name: String,
    val artist: String,
    val songCount: Int,
    val songs: List<Song>,
    val albumArtUri: Uri?,
    val duration: Long = songs.sumOf { it.duration }
)
