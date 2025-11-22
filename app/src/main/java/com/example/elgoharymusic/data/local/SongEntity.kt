package com.example.elgoharymusic.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity
data class SongEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val artist: String,
    val duration: Long,
    val album: String? = null,
    val uri: String,
    val albumArtUri: String? = null
)