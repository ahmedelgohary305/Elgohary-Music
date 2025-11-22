package com.example.elgoharymusic.presentation.utils

import android.content.Context
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.elgoharymusic.R
import com.example.elgoharymusic.data.repoImpl.AppLanguage
import com.example.elgoharymusic.domain.models.Artist
import com.example.elgoharymusic.domain.models.Song
import com.example.elgoharymusic.presentation.viewmodels.MusicViewModel

@Composable
fun ArtistsList(
    artists: List<Artist>,
    currentSong: Song?,
    musicViewModel: MusicViewModel,
    currentLanguage: AppLanguage,
    context: Context,
    onArtistClick: (Artist) -> Unit,
    miniPlayerHeight: Dp = 88.dp,
    emptyMessage: String = stringResource(R.string.no_artists_found),
    emptyDescription: String = stringResource(R.string.add_some_music_to_your_device_to_see_artists)
) {
    if (artists.isEmpty()) {
        EmptyState(
            emptyMessage = emptyMessage,
            emptyDescription = emptyDescription,
            currentLanguage = currentLanguage,
            painter = painterResource(id = R.drawable.artist),
            currentSong = currentSong
        )
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 12.dp,
                end = 12.dp,
                top = 8.dp,
                bottom = if (currentSong != null) miniPlayerHeight + 16.dp else 16.dp
            ),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            items(artists) { artist ->
                ArtistCard(
                    artist = artist,
                    currentLanguage = currentLanguage,
                    onArtistClick = { onArtistClick(artist) },
                    onPlayClick = { musicViewModel.playSongs(artist.songs) },
                    context = context
                )
                if (artist != artists.last()) {
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                }
            }
        }
    }
}

@Composable
fun ArtistCard(
    artist: Artist,
    currentLanguage: AppLanguage,
    context: Context,
    onArtistClick: () -> Unit,
    onPlayClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // Calculate unique albums count
    val albumCount = artist.songs.mapNotNull { it.album }.distinct().size

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onArtistClick
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        )
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .height(IntrinsicSize.Min),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Artist image with fallback composable
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.DarkGray),
                contentAlignment = Alignment.Center
            ) {
                ComponentImage(
                    data = artist.albumArtUri ?: Uri.EMPTY,
                    iconId = R.drawable.artist,
                    crossfadeDuration = 500,
                    modifier = Modifier.size(32.dp),
                    context = context
                )
            }

            Spacer(Modifier.width(16.dp))

            // Artist info
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = artist.name,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(4.dp))

                // Combined songs and albums count in one row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val songCountText = if (artist.songCount == 1) {
                        context.getString(R.string.one_song)
                    } else {
                        val localizedCount = artist.songCount.toLocalizedDigits(currentLanguage)
                        context.getString(R.string.multiple_songs, localizedCount)
                    }

                    Text(
                        text = songCountText,
                        style = MaterialTheme.typography.bodySmall,
                        color = colorScheme.onSurface,
                        modifier = Modifier.offset(
                            y = if (currentLanguage == AppLanguage.ARABIC) (-6).dp else 0.dp
                        )
                    )

                    // Separator dot
                    if (albumCount > 0) {
                        Text(
                            text = "•",
                            style = MaterialTheme.typography.bodySmall,
                            color = colorScheme.onSurface,
                            modifier = Modifier.offset(
                                y = if (currentLanguage == AppLanguage.ARABIC) (-6).dp else 0.dp
                            )
                        )

                        // Albums count
                        Text(
                            text = pluralStringResource(
                                id = R.plurals.album_count,
                                count = albumCount,
                                albumCount
                            ),
                            style = MaterialTheme.typography.bodySmall,
                            color = colorScheme.onSurface,
                            modifier = Modifier.offset(
                                y = if (currentLanguage == AppLanguage.ARABIC) (-6).dp else 0.dp
                            )
                        )
                    }
                }
            }

            Spacer(Modifier.width(8.dp))

            // Play button
            PlayButton(
                paddingValues = PaddingValues(0.dp),
                onPlayClick = onPlayClick,
            )
        }
    }
}

@Composable
fun PlayButton(
    paddingValues: PaddingValues,
    onPlayClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .padding(paddingValues)
            .size(42.dp)
            .clip(CircleShape)
            .background(colorScheme.onBackground)
            .clickable(onClick = onPlayClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(R.drawable.resume),
            contentDescription = "Play artist",
            tint = colorScheme.background,
            modifier = Modifier
                .size(22.dp)
        )
    }
}