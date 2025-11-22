package com.example.elgoharymusic.presentation


sealed class Routes(val route: String) {
    object Home : Routes("song_list")
    object FullPlayer : Routes("full_player")
    object ArtistSongs : Routes("artist_songs/{artistId}") {
        fun createRoute(artistId: Long) = "artist_songs/$artistId"
    }
    object AlbumSongs : Routes("album_songs/{albumId}") {
        fun createRoute(albumId: Long) = "album_songs/$albumId"
    }
    object PlaylistDetail : Routes("playlist_detail/{playlistId}") {
        fun createRoute(playlistId: Long) = "playlist_detail/$playlistId"
    }
}
