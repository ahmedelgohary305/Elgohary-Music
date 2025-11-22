package com.example.elgoharymusic.data.local

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.Junction
import androidx.room.PrimaryKey
import androidx.room.Relation
import com.example.elgoharymusic.presentation.utils.TimeFormatter

@Entity
data class PlaylistEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val createdAt: Long = TimeFormatter.getCurrentTimestamp(),
    val description: String? = null
)

@Entity(
    tableName = "playlist_song_cross_ref",
    primaryKeys = ["playlistId", "songId"],
    foreignKeys = [
        ForeignKey(
            entity = PlaylistEntity::class,
            parentColumns = ["id"],
            childColumns = ["playlistId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = SongEntity::class,
            parentColumns = ["id"],
            childColumns = ["songId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("playlistId"), Index("songId"), Index(value = ["playlistId", "position"])]
)
data class PlaylistSongCrossRef(
    val playlistId: Long,
    val songId: Long,
    val position: Int = 0
)

data class PlaylistWithSongs(
    @Embedded val playlist: PlaylistEntity,

    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            PlaylistSongCrossRef::class,
            parentColumn = "playlistId",
            entityColumn = "songId"
        )
    )
    val songs: List<SongEntity>,
)
