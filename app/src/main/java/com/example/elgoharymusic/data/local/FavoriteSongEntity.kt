package com.example.elgoharymusic.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import com.example.elgoharymusic.presentation.utils.TimeFormatter

@Entity(
    tableName = "favorites",
    primaryKeys = ["songId"],
    foreignKeys = [
        ForeignKey(
            entity = SongEntity::class,
            parentColumns = ["id"],
            childColumns = ["songId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("songId")]
)
data class FavoriteSongEntity(
    val songId: Long,
)

