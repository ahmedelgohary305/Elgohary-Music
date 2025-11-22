package com.example.elgoharymusic.presentation.utils

import android.content.Context
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.elgoharymusic.R
import com.example.elgoharymusic.data.repoImpl.AppLanguage
import com.example.elgoharymusic.domain.models.Song
import com.example.elgoharymusic.presentation.viewmodels.MusicViewModel

@Composable
fun SearchResultsList(
    searchQuery: String,
    filteredSongs: List<Song>,
    currentSong: Song?,
    musicViewModel: MusicViewModel,
    context: Context,
    currentLanguage: AppLanguage,
    onSongClick: (Song) -> Unit
) {
    val miniPlayerHeight = 88.dp

    Column(modifier = Modifier.fillMaxSize()) {
        // Search results header
        if (searchQuery.isNotEmpty()) {
            Text(
                text = if (filteredSongs.isEmpty()) {
                    stringResource(R.string.no_results_for, searchQuery)
                } else {
                    stringResource(R.string.results_for, filteredSongs.size, searchQuery)
                },
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 12.dp)
            )
        }

        if (searchQuery.isEmpty()) {
            EmptyState(
                emptyMessage = stringResource(R.string.no_results_found),
                emptyDescription = stringResource(R.string.try_different_search),
                currentSong = currentSong,
                currentLanguage = currentLanguage,
                miniPlayerHeight = miniPlayerHeight,
                painter = painterResource(id = R.drawable.search)
            )
        } else if (filteredSongs.isEmpty()) {
            EmptyState(
                emptyMessage = stringResource(R.string.no_results_for, searchQuery),
                emptyDescription = stringResource(R.string.try_different_search),
                currentSong = currentSong,
                currentLanguage = currentLanguage,
                miniPlayerHeight = miniPlayerHeight,
                painter = painterResource(id = R.drawable.search)
            )
        } else {
            SongList(
                songs = filteredSongs,
                currentSong = currentSong,
                musicViewModel = musicViewModel,
                onSongClick = onSongClick,
                miniPlayerHeight = miniPlayerHeight,
                context = context,
                currentLanguage = currentLanguage
            )
        }
    }
}
