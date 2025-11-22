package com.example.elgoharymusic.domain.models

import com.example.elgoharymusic.presentation.utils.TimeFormatter

data class Playlist(
    val id: Long = 0,
    val name: String,
    val createdAt: Long = TimeFormatter.getCurrentTimestamp(),
    val description: String? = null,
    val songs: List<Song> = emptyList(),
    val songCount: Int = songs.size
)
