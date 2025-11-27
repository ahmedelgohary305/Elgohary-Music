package com.example.elgoharymusic.presentation.screens

import android.content.Context
import android.net.Uri
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Badge
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.elgoharymusic.R
import com.example.elgoharymusic.domain.models.Song
import com.example.elgoharymusic.presentation.viewmodels.FavViewModel
import com.example.elgoharymusic.presentation.viewmodels.MusicViewModel
import com.example.elgoharymusic.presentation.utils.ComponentImage
import com.example.elgoharymusic.presentation.utils.LocaleManager
import com.example.elgoharymusic.presentation.utils.QueueBottomSheet
import com.example.elgoharymusic.presentation.utils.TimeFormatter
import com.example.elgoharymusic.presentation.utils.formatDuration
import com.example.elgoharymusic.presentation.utils.toLocalizedDigits
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.absoluteValue
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

@Composable
fun FullPlayerScreen(
    musicViewModel: MusicViewModel,
    favViewModel: FavViewModel,
    context: Context,
    currentLanguage: String,
    onBack: () -> Unit
) {
    val currentSong by musicViewModel.currentSong.collectAsStateWithLifecycle()
    val isPlaying by musicViewModel.isPlaying.collectAsStateWithLifecycle()
    val currentPosition by musicViewModel.currentPosition.collectAsStateWithLifecycle()
    val isShuffleEnabled by musicViewModel.isShuffleEnabled.collectAsStateWithLifecycle()
    val repeatMode by musicViewModel.repeatMode.collectAsStateWithLifecycle()
    val queueSongs by musicViewModel.queueSongs.collectAsStateWithLifecycle()
    var showQueue by remember { mutableStateOf(false) }

    currentSong?.let { song ->
        val colorScheme = MaterialTheme.colorScheme

        // Master fade animation - controls all background animations
        val fadeOutMultiplier by animateFloatAsState(
            targetValue = if (isPlaying) 1f else 0f,
            animationSpec = tween(
                durationMillis = 1000,
                easing = FastOutSlowInEasing
            ),
            label = "fade_out"
        )

        // Only run infinite animations when playing OR fading out
        val shouldAnimate = isPlaying || fadeOutMultiplier > 0.01f

        var rotationState by remember { mutableFloatStateOf(0f) }
        val rotationSpeed = 360f / 20000f

        // Update rotation based on play state
        LaunchedEffect(isPlaying) {
            if (isPlaying) {
                while (true) {
                    val startTime = TimeFormatter.getCurrentTimestamp()
                    val startRotation = rotationState

                    while (true) {
                        val elapsed = TimeFormatter.getCurrentTimestamp() - startTime
                        rotationState = (startRotation + elapsed * rotationSpeed) % 360f
                        delay(16) // ~60fps updates
                    }
                }
            }
        }

        Box(modifier = Modifier.fillMaxSize()) {
            val primaryColor: Color
            val secondaryColor: Color
            val tertiaryColor: Color
            val backgroundColor = colorScheme.background

            if (isSystemInDarkTheme()) {
                primaryColor = colorScheme.primary
                secondaryColor = colorScheme.secondary
                tertiaryColor = colorScheme.tertiary
            } else {
                primaryColor = Color(0xFF3F51B5)
                secondaryColor = Color(0xFF9C27B0)
                tertiaryColor = Color(0xFF2196F3)
            }

            // Separate animated background to isolate recompositions
            AnimatedBackgroundLayer(
                shouldAnimate = shouldAnimate,
                fadeOutMultiplier = fadeOutMultiplier,
                primaryColor = primaryColor,
                secondaryColor = secondaryColor,
                tertiaryColor = tertiaryColor,
                backgroundColor = backgroundColor
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                TopBar(
                    favViewModel = favViewModel,
                    currentSong = currentSong!!,
                    currentLanguage = currentLanguage,
                    onBack = onBack,
                    onQueueClick = { showQueue = true },
                    colorScheme = colorScheme,
                    queueSongs = queueSongs
                )

                Spacer(modifier = Modifier.height(8.dp))

                AlbumArtSection(
                    song = song,
                    rotation = rotationState,
                    context = context,
                )

                Spacer(modifier = Modifier.height(16.dp))

                SongInfo(song = song, colorScheme = colorScheme, currentLanguage = currentLanguage)

               if (currentLanguage != LocaleManager.Language.ARABIC.code)
                   Spacer(modifier = Modifier.height(20.dp))
                else
                   Spacer(modifier = Modifier.height(12.dp))

                NoRtl {
                    ProgressSection(
                        currentPosition = currentPosition,
                        duration = song.duration,
                        onSeek = musicViewModel::seekTo,
                        colorScheme = colorScheme,
                        currentLanguage = currentLanguage,
                        songId = currentSong!!.id
                    )
                }

                Spacer(modifier = Modifier.weight(0.8f))

                GlassmorphicControlsPanel(
                    isPlaying = isPlaying,
                    isShuffleEnabled = isShuffleEnabled,
                    repeatMode = repeatMode,
                    onPlayPause = musicViewModel::playPause,
                    onPrevious = musicViewModel::skipToPrevious,
                    onNext = musicViewModel::skipToNext,
                    onShuffleToggle = musicViewModel::toggleShuffle,
                    onRepeatToggle = musicViewModel::toggleRepeat,
                    colorScheme = colorScheme,
                )
            }

            if (showQueue) {
                QueueBottomSheet(
                    queueSongs = queueSongs,
                    currentSong = currentSong,
                    onSongClick = musicViewModel::playSongFromQueue,
                    onRemoveFromQueue = musicViewModel::removeFromQueue,
                    onClearQueue = musicViewModel::clearQueue,
                    onReorderQueue = musicViewModel::reorderQueue,
                    onDismiss = { showQueue = false },
                    colorScheme = colorScheme,
                    context = context,
                    currentLanguage = currentLanguage
                )
            }
        }
    }
}

// Separate composable to isolate animation recompositions
@Composable
private fun AnimatedBackgroundLayer(
    shouldAnimate: Boolean,
    fadeOutMultiplier: Float,
    primaryColor: Color,
    secondaryColor: Color,
    tertiaryColor: Color,
    backgroundColor: Color
) {
    val wave1: Float
    val wave2: Float
    val wave3: Float

    if (shouldAnimate) {
        val infiniteTransition = rememberInfiniteTransition(label = "player_animations")

        val wave1Infinite by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(6000, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "wave1"
        )

        val wave2Infinite by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(4500, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "wave2"
        )

        val wave3Infinite by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(8000, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "wave3"
        )

        wave1 = wave1Infinite * fadeOutMultiplier
        wave2 = wave2Infinite * fadeOutMultiplier
        wave3 = wave3Infinite * fadeOutMultiplier
    } else {
        wave1 = 0f
        wave2 = 0f
        wave3 = 0f
    }

    AnimatedBackground(
        wave1 = wave1,
        wave2 = wave2,
        wave3 = wave3,
        primaryColor = primaryColor,
        secondaryColor = secondaryColor,
        tertiaryColor = tertiaryColor,
        backgroundColor = backgroundColor
    )
}

@Composable
private fun AnimatedBackground(
    wave1: Float,
    wave2: Float,
    wave3: Float,
    primaryColor: Color,
    secondaryColor: Color,
    tertiaryColor: Color,
    backgroundColor: Color
) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        drawRect(color = backgroundColor)
        val width = size.width
        val height = size.height

        val baseAlpha = 0.05f
        val activeAlphaMultiplier = 0.25f

        val alpha1 = baseAlpha + (activeAlphaMultiplier - baseAlpha) * wave1
        val alpha2 = baseAlpha + (activeAlphaMultiplier - baseAlpha) * wave2
        val alpha3 = baseAlpha + (activeAlphaMultiplier - baseAlpha) * wave3

        val baseRadius1 = width * 0.8f
        val radiusVariation1 = width * 0.4f
        val radius1 = baseRadius1 + (wave1 * radiusVariation1 - radiusVariation1 / 2)

        val centerX1 = width * (0.1f + wave1 * 0.8f)
        val centerY1 = height * (0.1f + wave2 * 0.3f)

        if (alpha1 > 0f) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        primaryColor.copy(alpha = alpha1),
                        Color.Transparent
                    ),
                    radius = radius1,
                    center = Offset(centerX1, centerY1)
                ),
                radius = radius1,
                center = Offset(centerX1, centerY1)
            )
        }

        val baseRadius2 = width * 0.7f
        val radiusVariation2 = width * 0.3f
        val radius2 = baseRadius2 + (wave2 * radiusVariation2 - radiusVariation2 / 2)

        val centerX2 = width * (0.9f - wave2 * 0.7f)
        val centerY2 = height * (0.5f + wave3 * 0.4f)

        if (alpha2 > 0f) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        secondaryColor.copy(alpha = alpha2),
                        Color.Transparent
                    ),
                    radius = radius2,
                    center = Offset(centerX2, centerY2)
                ),
                radius = radius2,
                center = Offset(centerX2, centerY2)
            )
        }

        val baseRadius3 = width * 0.6f
        val radiusVariation3 = width * 0.5f
        val radius3 = baseRadius3 + (wave3 * radiusVariation3 - radiusVariation3 / 2)

        val centerX3 = width * (0.2f + wave3 * 0.5f)
        val centerY3 = height * (0.9f - wave1 * 0.6f)

        if (alpha3 > 0f) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        tertiaryColor.copy(alpha = alpha3),
                        Color.Transparent
                    ),
                    radius = radius3,
                    center = Offset(centerX3, centerY3)
                ),
                radius = radius3,
                center = Offset(centerX3, centerY3)
            )
        }
    }
}

@Composable
private fun TopBar(
    favViewModel: FavViewModel,
    currentSong: Song,
    currentLanguage: String,
    onBack: () -> Unit,
    onQueueClick: () -> Unit,
    queueSongs: List<Song>,
    colorScheme: ColorScheme
) {
    // Memoize queue size to avoid unnecessary recompositions
    val queueSize = remember(queueSongs.size) { queueSongs.size }

    val favSongs by favViewModel.favSongs.collectAsStateWithLifecycle()
    val isFavorite = remember(favSongs, currentSong.id) {
        favSongs.any { it.id == currentSong.id }
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack) {
            Icon(
                painter = painterResource(R.drawable.arrow_down),
                contentDescription = "Back",
                modifier = Modifier.size(21.dp),
                tint = colorScheme.onSurface
            )
        }

        Text(
            text = stringResource(R.string.now_playing),
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.SemiBold
            ),
            color = colorScheme.tertiary
        )

        Row {
            NoRtl {
                Box(contentAlignment = Alignment.Center) {
                    IconButton(onClick = onQueueClick) {
                        Icon(
                            painter = painterResource(R.drawable.queue_music),
                            contentDescription = "Queue",
                            modifier = Modifier.size(24.dp),
                            tint = colorScheme.onBackground
                        )
                    }
                    Badge(
                        containerColor = MaterialTheme.colorScheme.tertiary,
                        contentColor = Color.Black,
                        modifier = Modifier.offset(x = 8.dp, y = (-8).dp)
                    ) {
                        Text(
                            text = queueSize.toLocalizedDigits(currentLanguage),
                            style = MaterialTheme.typography.labelSmall,
                        )
                    }
                }
            }
            IconButton(
                onClick = { favViewModel.toggleFavorite(currentSong) }
            ) {
                Icon(
                    painter = if (isFavorite) {
                        painterResource(R.drawable.favorite)
                    } else {
                        painterResource(R.drawable.favorite_border)
                    },
                    contentDescription = "Favorite",
                    modifier = Modifier.size(24.dp),
                    tint = colorScheme.onBackground
                )
            }
        }
    }
}

@Composable
private fun AlbumArtSection(
    song: Song,
    rotation: Float,
    context: Context
) {
    Box(
        modifier = Modifier.size(300.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier
                .size(300.dp)
                .rotate(rotation),
            shape = CircleShape,
            color = Color.DarkGray,
            shadowElevation = 16.dp
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                ComponentImage(
                    data = song.albumArtUri ?: Uri.EMPTY,
                    iconId = R.drawable.song,
                    crossfadeDuration = 500,
                    modifier = Modifier.size(120.dp),
                    context = context
                )
            }
        }
    }
}

@Composable
private fun SongInfo(
    song: Song,
    currentLanguage: String,
    colorScheme: ColorScheme
) {
    val headlineSmall = MaterialTheme.typography.headlineSmall.copy(
        fontWeight = FontWeight.Bold,
        letterSpacing = (-0.5).sp
    )
    // Memoize text style to avoid recreation
    val titleStyle = remember {
        headlineSmall
    }

    Text(
        text = song.title,
        style = titleStyle,
        textAlign = TextAlign.Center,
        color = colorScheme.onBackground,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = Modifier.padding(horizontal = 16.dp)
    )

    if (currentLanguage != LocaleManager.Language.ARABIC.code) Spacer(modifier = Modifier.height(8.dp))

    Text(
        text = song.artist,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        style = MaterialTheme.typography.titleMedium,
        color = colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProgressSection(
    songId: Long,
    currentPosition: Long,
    duration: Long,
    currentLanguage: String,
    onSeek: (Long) -> Unit,
    colorScheme: ColorScheme
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Slider(
            value = currentPosition.toFloat().coerceIn(0f, duration.toFloat()),
            onValueChange = { onSeek(it.toLong()) },
            valueRange = 0f..duration.toFloat(),
            modifier = Modifier.fillMaxWidth(),
            thumb = {
                Box(modifier = Modifier.size(0.dp))
            },
            track = { sliderState ->
                WaveformTrack(
                    songId = songId,
                    currentPosition = currentPosition,
                    duration = duration,
                    colorScheme = colorScheme
                )
            }
        )

        // Memoize formatted durations to avoid repeated formatting
        val formattedCurrent = remember(currentPosition) {
            currentPosition.formatDuration(currentLanguage)
        }
        val formattedDuration = remember(duration) {
            duration.formatDuration(currentLanguage)
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = formattedCurrent,
                style = MaterialTheme.typography.bodySmall,
                color = colorScheme.onSurface
            )
            Text(
                text = formattedDuration,
                style = MaterialTheme.typography.bodySmall,
                color = colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun WaveformTrack(
    songId: Long,
    currentPosition: Long,
    duration: Long,
    colorScheme: ColorScheme
) {
    val progress = remember(currentPosition, duration) {
        if (duration > 0) {
            (currentPosition.toFloat() / duration.toFloat()).coerceIn(0f, 1f)
        } else 0f
    }

    val barCount = 60
    val maxHeight = 50.dp
    val minHeight = 8.dp

    val waveformHeights = remember(songId) {
        val seed = songId.hashCode()
        val random = Random(seed)

        (0 until barCount).map { index ->
            val position = index.toFloat() / barCount

            val wave1 = sin(position * PI * (3 + random.nextFloat() * 2)) * 0.4
            val wave2 = sin(position * PI * (6 + random.nextFloat() * 4)) * 0.3
            val wave3 = cos(position * PI * (4 + random.nextFloat() * 3)) * 0.3
            val randomVariation = random.nextFloat() * 0.2 - 0.1

            val combined = 0.4 + (wave1 + wave2 + wave3).absoluteValue + randomVariation
            combined.coerceIn(0.2, 1.0)
        }
    }

    // Memoize gradient brushes
    val activeBarBrush = remember(colorScheme) {
        Brush.linearGradient(
            colors = listOf(
                colorScheme.secondary,
                colorScheme.tertiary,
            ),
        )
    }

    val inactiveBarColor = remember(colorScheme) {
        colorScheme.onSurface.copy(alpha = 0.4f)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(maxHeight),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        waveformHeights.forEachIndexed { index, heightRatio ->
            val barProgress = index.toFloat() / barCount
            val isActive = progress >= barProgress

            val animatedHeightRatio by animateFloatAsState(
                targetValue = heightRatio.toFloat(),
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium
                ),
                label = "bar_height_$index"
            )

            val backgroundBrush = if (isActive) {
                activeBarBrush
            } else {
                Brush.verticalGradient(
                    colors = listOf(inactiveBarColor, inactiveBarColor)
                )
            }

            val finalHeight = minHeight + (maxHeight - minHeight) * animatedHeightRatio

            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(finalHeight)
                    .clip(RoundedCornerShape(24.dp))
                    .background(backgroundBrush),
            )
        }
    }
}

@Composable
private fun GlassmorphicControlsPanel(
    isPlaying: Boolean,
    isShuffleEnabled: Boolean,
    repeatMode: com.example.elgoharymusic.presentation.viewmodels.RepeatMode,
    onPlayPause: () -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onShuffleToggle: () -> Unit,
    onRepeatToggle: () -> Unit,
    colorScheme: ColorScheme,
) {
    val isDarkTheme = isSystemInDarkTheme()

    // Memoize brushes
    val backgroundBrush = Brush.verticalGradient(
                listOf(
                    Color.Black.copy(alpha = 0.19f),
                    Color.Black.copy(alpha = 0.15f)
                )
        )


    val borderBrush = remember(isDarkTheme) {
        Brush.linearGradient(
            listOf(
                if (isDarkTheme)
                    Color.White.copy(alpha = 0.4f)
                else
                    Color.Black.copy(alpha = 0.3f),
                Color.Transparent
            )
        )
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp, start = 8.dp, end = 8.dp)
            .clip(RoundedCornerShape(32.dp))
            .background(backgroundBrush)
            .border(1.dp, borderBrush, RoundedCornerShape(32.dp))
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SecondaryControlsRow(
                isShuffleEnabled = isShuffleEnabled,
                repeatMode = repeatMode,
                onShuffleToggle = onShuffleToggle,
                onRepeatToggle = onRepeatToggle,
                colorScheme = colorScheme
            )

            MainControlsRow(
                isPlaying = isPlaying,
                onPlayPause = onPlayPause,
                onPrevious = onPrevious,
                onNext = onNext,
                colorScheme = colorScheme
            )
        }
    }
}

@Composable
private fun SecondaryControlsRow(
    isShuffleEnabled: Boolean,
    repeatMode: com.example.elgoharymusic.presentation.viewmodels.RepeatMode,
    onShuffleToggle: () -> Unit,
    onRepeatToggle: () -> Unit,
    colorScheme: ColorScheme
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        GlassButton(
            onClick = onShuffleToggle,
            isActive = isShuffleEnabled,
            size = 48.dp
        ) {
            val shuffleTint by animateColorAsState(
                targetValue = if (isShuffleEnabled) colorScheme.onSurface.copy(0.9f)
                else colorScheme.onSurface.copy(alpha = 0.5f),
                animationSpec = tween(300),
                label = "shuffleTint"
            )

            val shuffleScale by animateFloatAsState(
                targetValue = if (isShuffleEnabled) 1.1f else 1f,
                animationSpec = tween(300, easing = FastOutSlowInEasing),
                label = "shuffleScale"
            )

            Icon(
                painter = painterResource(R.drawable.shuffle),
                contentDescription = "Shuffle",
                modifier = Modifier
                    .size(22.dp)
                    .graphicsLayer {
                        scaleX = shuffleScale
                        scaleY = shuffleScale
                    },
                tint = shuffleTint
            )
        }

        GlassButton(
            onClick = onRepeatToggle,
            isActive = repeatMode != com.example.elgoharymusic.presentation.viewmodels.RepeatMode.OFF,
            size = 48.dp
        ) {
            val repeatTint by animateColorAsState(
                targetValue = when (repeatMode) {
                    com.example.elgoharymusic.presentation.viewmodels.RepeatMode.OFF ->
                        colorScheme.onSurface.copy(alpha = 0.5f)
                    else -> colorScheme.onSurface.copy(alpha = 0.9f)
                },
                animationSpec = tween(300),
                label = "repeatTint"
            )

            val repeatScale by animateFloatAsState(
                targetValue = if (repeatMode != com.example.elgoharymusic.presentation.viewmodels.RepeatMode.OFF) 1.1f else 1f,
                animationSpec = tween(300, easing = FastOutSlowInEasing),
                label = "repeatScale"
            )

            Crossfade(
                targetState = repeatMode,
                animationSpec = tween(400),
                label = "repeatCrossfade"
            ) { mode ->
                Icon(
                    painter = painterResource(
                        when (mode) {
                            com.example.elgoharymusic.presentation.viewmodels.RepeatMode.OFF -> R.drawable.repeat
                            com.example.elgoharymusic.presentation.viewmodels.RepeatMode.ALL -> R.drawable.repeat
                            com.example.elgoharymusic.presentation.viewmodels.RepeatMode.ONE -> R.drawable.repeat_one
                        }
                    ),
                    contentDescription = "Repeat",
                    modifier = Modifier
                        .size(22.dp)
                        .then(
                            if (mode == com.example.elgoharymusic.presentation.viewmodels.RepeatMode.ONE) {
                                Modifier.subtlePulse()
                            } else Modifier
                        )
                        .graphicsLayer {
                            scaleX = repeatScale
                            scaleY = repeatScale
                        },
                    tint = repeatTint
                )
            }
        }
    }
}

@Composable
private fun MainControlsRow(
    isPlaying: Boolean,
    onPlayPause: () -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    colorScheme: ColorScheme
) {
    NoRtl {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            GlassButton(
                onClick = onPrevious,
                size = 56.dp
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.skip_previous),
                    contentDescription = "Previous",
                    modifier = Modifier.size(28.dp),
                    tint = if (isSystemInDarkTheme()) colorScheme.onSurface.copy(0.9f)
                    else colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }

            Box(
                modifier = Modifier
                    .size(85.dp)
                    .clip(CircleShape)
                    .background(colorScheme.secondary)
                    .clickable { onPlayPause() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = if (isPlaying)
                        painterResource(id = R.drawable.pause)
                    else
                        painterResource(id = R.drawable.resume),
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                    tint = Color(red = 0.11f, green = 0.153f, blue = 0.298f)
                )
            }

            GlassButton(
                onClick = onNext,
                size = 56.dp
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.skip_next),
                    contentDescription = "Next",
                    modifier = Modifier.size(28.dp),
                    tint = if (isSystemInDarkTheme()) colorScheme.onSurface.copy(0.9f)
                    else colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }
        }
    }
}

@Composable
private fun GlassButton(
    onClick: () -> Unit,
    size: Dp,
    isActive: Boolean = false,
    content: @Composable () -> Unit
) {
    val tertiaryColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.6f)
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface.copy(0.3f)

    // Memoize background color
    val backgroundColor = remember(isActive) {
        if (isActive)
            tertiaryColor
        else
            Color.Black.copy(alpha = 0.25f)
    }

    val borderColor = remember(isActive) {
        if (isActive) {
            onSurfaceColor
        } else {
            Color.Transparent
        }
    }

    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(backgroundColor)
            .border(1.dp, borderColor, CircleShape)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

@Composable
fun Modifier.subtlePulse(): Modifier {
    val infiniteTransition = rememberInfiniteTransition(label = "subtlePulse")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    return this.graphicsLayer {
        this.alpha = alpha
    }
}