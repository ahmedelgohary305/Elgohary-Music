package com.example.elgoharymusic.presentation.utils

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.elgoharymusic.R
import com.example.elgoharymusic.domain.models.Album
import com.example.elgoharymusic.presentation.viewmodels.MusicViewModel

@Composable
fun AlbumsGrid(
    modifier: Modifier = Modifier,
    albums: List<Album>,
    currentLanguage: String,
    musicViewModel: MusicViewModel,
    onAlbumClick: (Album) -> Unit,
    emptyMessage: String = stringResource(R.string.no_albums_found),
    emptyDescription: String = stringResource(R.string.try_adding_some_music_to_your_device)
) {
    val currentSong = musicViewModel.currentSong.collectAsStateWithLifecycle().value

    if (albums.isEmpty()) {
        EmptyState(
            emptyMessage = emptyMessage,
            emptyDescription = emptyDescription,
            currentSong = currentSong,
            currentLanguage = currentLanguage,
            painter = painterResource(id = R.drawable.music_album)
        )
    } else {
        LazyVerticalGrid(
            modifier = modifier.fillMaxSize(),
            columns = GridCells.Adaptive(150.dp),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(
                items = albums, key = { album -> album.albumId }) { album ->
                AlbumGridItem(
                    album = album,
                    currentLanguage,
                    musicViewModel = musicViewModel,
                    onAlbumClick = { onAlbumClick(album) })
            }
        }
    }
}

@Composable
fun AlbumGridItem(
    album: Album,
    currentLanguage: String,
    musicViewModel: MusicViewModel,
    onAlbumClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }) { onAlbumClick() }
            .fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .aspectRatio(1f)
                .clip(RoundedCornerShape(16.dp))
                .background(if (isSystemInDarkTheme()) colorScheme.surfaceVariant else Color.LightGray)
                .border(
                    width = 1.dp,
                    color = colorScheme.outline.copy(0.5f),
                    shape = RoundedCornerShape(16.dp)
                ), contentAlignment = Alignment.BottomEnd
        ) {
            AsyncImage(
                model = album.albumArtUri,
                contentDescription = "Album artwork",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            // Play button in bottom-right
            PlayButton(
                paddingValues = PaddingValues(8.dp),
                onPlayClick = { musicViewModel.playSongs(album.songs) },
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = album.name,
            style = MaterialTheme.typography.titleMedium,
            color = colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.offset(
                y = if (currentLanguage == LocaleManager.Language.ARABIC.code) (-6).dp else 0.dp
            )
        )

        Text(
            text = album.artist,
            style = MaterialTheme.typography.bodySmall,
            color = colorScheme.onSurface.copy(alpha = 0.7f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.offset(
                y = if (currentLanguage == LocaleManager.Language.ARABIC.code) (-12).dp else 0.dp
            )
        )
    }
}
