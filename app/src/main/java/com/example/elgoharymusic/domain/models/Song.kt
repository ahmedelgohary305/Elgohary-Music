package com.example.elgoharymusic.domain.models

import android.net.Uri

data class Song(
    val id: Long,
    val title: String,
    val duration: Long,
    val artistId: Long? = null,
    val artist: String,
    val albumId: Long? = null,
    val album: String? = null,
    val uri: Uri,
    val albumArtUri: Uri? = null,
)