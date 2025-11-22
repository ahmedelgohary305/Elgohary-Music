package com.example.elgoharymusic.domain.models

import android.net.Uri

data class Artist(
    val artistId: Long,
    val name: String,
    val songCount: Int,
    val songs: List<Song>,
    val albumArtUri: Uri? = null
)