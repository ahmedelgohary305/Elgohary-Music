package com.example.elgoharymusic.presentation

import android.content.ComponentName
import android.content.Context
import android.os.Handler
import android.os.Looper
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.example.elgoharymusic.domain.models.Song
import com.example.elgoharymusic.presentation.viewmodels.RepeatMode
import com.google.common.util.concurrent.MoreExecutors
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MusicController @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var mediaController: MediaController? = null
    private val _currentSong = MutableStateFlow<Song?>(null)
    val currentSong: StateFlow<Song?> = _currentSong.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentPosition = MutableStateFlow(0L)
    val currentPosition: StateFlow<Long> = _currentPosition.asStateFlow()

    // New state flows for shuffle, repeat, and queue
    private val _isShuffleEnabled = MutableStateFlow(false)
    val isShuffleEnabled: StateFlow<Boolean> = _isShuffleEnabled.asStateFlow()

    private val _repeatMode = MutableStateFlow(RepeatMode.OFF)
    val repeatMode: StateFlow<RepeatMode> = _repeatMode.asStateFlow()

    private val _queueSongs = MutableStateFlow<List<Song>>(emptyList())
    val queueSongs: StateFlow<List<Song>> = _queueSongs.asStateFlow()

    private val mainHandler = Handler(Looper.getMainLooper())
    private var positionUpdateRunnable: Runnable? = null
    private var allSongs: List<Song> = emptyList()
    private var originalQueue: List<Song> = emptyList() // Store original order for shuffle toggle

    fun initialize() {
        val sessionToken = SessionToken(context, ComponentName(context, MusicService::class.java))
        val controllerFuture = MediaController.Builder(context, sessionToken).buildAsync()

        controllerFuture.addListener({
            mediaController = controllerFuture.get()

            // Sync state immediately when controller connects
            syncControllerState()

            mediaController?.addListener(object : Player.Listener {
                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    _isPlaying.value = isPlaying
                    if (isPlaying) {
                        startPositionUpdates()
                    } else {
                        stopPositionUpdates()
                    }
                }

                override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                    // Update current song based on current media item
                    mediaItem?.let { item ->
                        val songId = item.mediaId.toLongOrNull()
                        val song = allSongs.find { it.id == songId }
                        _currentSong.value = song

                        // Handle repeat one mode
                        if (_repeatMode.value == RepeatMode.ONE && reason == Player.MEDIA_ITEM_TRANSITION_REASON_AUTO) {
                            // Song ended, repeat it
                            mediaController?.seekTo(0)
                            mediaController?.play()
                        }
                    } ?: run {
                        _currentSong.value = null
                    }

                    // Update queue to reflect current order
                    updateQueueFromController()
                }

                override fun onPlaybackStateChanged(playbackState: Int) {
                    super.onPlaybackStateChanged(playbackState)

                    if (playbackState == Player.STATE_IDLE) {
                        val controller = mediaController
                        if (controller != null && controller.mediaItemCount == 0) {
                            _currentSong.value = null
                            _isPlaying.value = false
                            _currentPosition.value = 0L
                            stopPositionUpdates()
                        }
                    } else if (playbackState == Player.STATE_ENDED) {
                        // Handle different repeat modes when playlist ends
                        when (_repeatMode.value) {
                            RepeatMode.ALL -> {
                                // Restart from beginning
                                mediaController?.seekTo(0, 0)
                                mediaController?.play()
                            }
                            RepeatMode.OFF -> {
                                // Stop playing
                                _isPlaying.value = false
                            }
                            RepeatMode.ONE -> {
                                // Already handled in onMediaItemTransition
                            }
                        }
                    }
                }

                override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {
                    _isShuffleEnabled.value = shuffleModeEnabled
                }

                override fun onRepeatModeChanged(repeatMode: Int) {
                    _repeatMode.value = when (repeatMode) {
                        Player.REPEAT_MODE_OFF -> RepeatMode.OFF
                        Player.REPEAT_MODE_ALL -> RepeatMode.ALL
                        Player.REPEAT_MODE_ONE -> RepeatMode.ONE
                        else -> RepeatMode.OFF
                    }
                }
            })

        }, MoreExecutors.directExecutor())
    }

    private fun syncControllerState() {
        mediaController?.let { controller ->
            _isPlaying.value = controller.isPlaying
            _currentPosition.value = controller.currentPosition
            _isShuffleEnabled.value = controller.shuffleModeEnabled
            _repeatMode.value = when (controller.repeatMode) {
                Player.REPEAT_MODE_OFF -> RepeatMode.OFF
                Player.REPEAT_MODE_ALL -> RepeatMode.ALL
                Player.REPEAT_MODE_ONE -> RepeatMode.ONE
                else -> RepeatMode.OFF
            }

            if (controller.mediaItemCount > 0) {
                val currentMediaItem = controller.currentMediaItem
                currentMediaItem?.let { item ->
                    val songId = item.mediaId.toLongOrNull()
                    _currentSong.value = allSongs.find { it.id == songId }
                }

                updateQueueFromController()

                if (controller.isPlaying) {
                    startPositionUpdates()
                }
            } else {
                _currentSong.value = null
                _isPlaying.value = false
                _currentPosition.value = 0L
                _queueSongs.value = emptyList()
                stopPositionUpdates()
            }
        }
    }

    private fun updateQueueFromController() {
        mediaController?.let { controller ->
            val queue = mutableListOf<Song>()
            for (i in 0 until controller.mediaItemCount) {
                val mediaItem = controller.getMediaItemAt(i)
                val songId = mediaItem.mediaId.toLongOrNull()
                allSongs.find { it.id == songId }?.let { song ->
                    queue.add(song)
                }
            }
            _queueSongs.value = queue
        }
    }

    fun ensureControllerConnected() {
        if (mediaController == null) {
            initialize()
        } else {
            syncControllerState()
        }
    }

    private fun startPositionUpdates() {
        stopPositionUpdates()

        positionUpdateRunnable = object : Runnable {
            override fun run() {
                mediaController?.let { controller ->
                    _currentPosition.value = controller.currentPosition
                }
                mainHandler.postDelayed(this, 1000)
            }
        }
        mainHandler.post(positionUpdateRunnable!!)
    }

    private fun stopPositionUpdates() {
        positionUpdateRunnable?.let { runnable ->
            mainHandler.removeCallbacks(runnable)
        }
        positionUpdateRunnable = null
    }

    fun playSongs(songs: List<Song>, startIndex: Int = 0) {
        allSongs = songs
        originalQueue = songs.toList() // Store original order

        if (mediaController == null) {
            initialize()
            mainHandler.postDelayed({
                executePlaySongs(songs, startIndex)
            }, 500)
        } else {
            executePlaySongs(songs, startIndex)
        }
    }

    private fun executePlaySongs(songs: List<Song>, startIndex: Int) {
        mediaController?.let { controller ->
            val mediaItems = songs.map { song ->
                MediaItem.Builder()
                    .setUri(song.uri)
                    .setMediaId(song.id.toString())
                    .setMediaMetadata(
                        MediaMetadata.Builder()
                            .setTitle(song.title)
                            .setArtist(song.artist)
                            .setDisplayTitle(song.title)
                            .build()
                    )
                    .build()
            }

            controller.setMediaItems(mediaItems, startIndex, 0)
            controller.prepare()
            controller.play()

            if (startIndex < songs.size) {
                _currentSong.value = songs[startIndex]
            }

            // Apply current shuffle and repeat settings
            controller.shuffleModeEnabled = _isShuffleEnabled.value
            controller.repeatMode = when (_repeatMode.value) {
                RepeatMode.OFF -> Player.REPEAT_MODE_OFF
                RepeatMode.ALL -> Player.REPEAT_MODE_ALL
                RepeatMode.ONE -> Player.REPEAT_MODE_ONE
            }

            updateQueueFromController()
        }
    }

    fun playPause() {
        ensureControllerConnected()
        mediaController?.let { controller ->
            if (controller.isPlaying) {
                controller.pause()
            } else {
                controller.play()
            }
        }
    }

    fun seekTo(position: Long) {
        ensureControllerConnected()
        mediaController?.seekTo(position)
    }


    fun skipToNext() {
        ensureControllerConnected()
        mediaController?.let { controller ->
            if (controller.mediaItemCount == 0) return

            val currentIndex = controller.currentMediaItemIndex
            val wasPlaying = controller.isPlaying

            val nextIndex = if (currentIndex >= controller.mediaItemCount - 1) {
                // If at last song, loop to first song
                0
            } else {
                // Otherwise, go to next song
                currentIndex + 1
            }

            controller.seekTo(nextIndex, 0)

            // Only call play if it was already playing
            if (wasPlaying) {
                controller.play()
            }
        }
    }

    fun skipToPrevious() {
        ensureControllerConnected()
        mediaController?.let { controller ->
            if (controller.mediaItemCount == 0) return

            val currentIndex = controller.currentMediaItemIndex
            val wasPlaying = controller.isPlaying

            val previousIndex = if (currentIndex <= 0) {
                // If at first song, loop to last song
                controller.mediaItemCount - 1
            } else {
                // Otherwise, go to previous song
                currentIndex - 1
            }

            controller.seekTo(previousIndex, 0)

            // Only call play if it was already playing
            if (wasPlaying) {
                controller.play()
            }
        }
    }


    fun toggleRepeat() {
        ensureControllerConnected()
        mediaController?.let { controller ->
            val newRepeatMode = when (_repeatMode.value) {
                RepeatMode.OFF -> RepeatMode.ALL
                RepeatMode.ALL -> RepeatMode.ONE
                RepeatMode.ONE -> RepeatMode.OFF
            }

            controller.repeatMode = when (newRepeatMode) {
                RepeatMode.OFF -> Player.REPEAT_MODE_OFF
                RepeatMode.ALL -> Player.REPEAT_MODE_ALL
                RepeatMode.ONE -> Player.REPEAT_MODE_ONE
            }

            _repeatMode.value = newRepeatMode
        }
    }
    // New shuffle functionality
    fun toggleShuffle() {
        ensureControllerConnected()
        mediaController?.let { controller ->
            val newShuffleMode = !controller.shuffleModeEnabled
            controller.shuffleModeEnabled = newShuffleMode
            _isShuffleEnabled.value = newShuffleMode

            // Update queue display to reflect new order
            updateQueueFromController()
        }
    }

    fun addToQueue(songs: List<Song>) {
        if (songs.isEmpty()) return

        ensureControllerConnected()
        mediaController?.let { controller ->
            // Get existing media IDs once
            val existingIds = buildSet {
                repeat(controller.mediaItemCount) { index ->
                    add(controller.getMediaItemAt(index).mediaId)
                }
            }

            // Filter and map in one pass
            val newMediaItems = songs.mapNotNull { song ->
                val songId = song.id.toString()
                if (songId in existingIds) {
                    null
                } else {
                    MediaItem.Builder()
                        .setUri(song.uri)
                        .setMediaId(songId)
                        .setMediaMetadata(
                            MediaMetadata.Builder()
                                .setTitle(song.title)
                                .setArtist(song.artist)
                                .setDisplayTitle(song.title)
                                .build()
                        )
                        .build()
                }
            }

            if (newMediaItems.isNotEmpty()) {
                newMediaItems.forEach { controller.addMediaItem(it) }

                // Update allSongs - only add truly new songs
                allSongs = (allSongs + songs).distinct()

                updateQueueFromController()
            }
        }
    }

    fun removeFromQueue(song: Song) {
        ensureControllerConnected()
        mediaController?.let { controller ->
            // Don't allow removing if only one song left
            if (controller.mediaItemCount <= 1) {
                return
            }

            // Find the index of the song in the current queue
            for (i in 0 until controller.mediaItemCount) {
                val mediaItem = controller.getMediaItemAt(i)
                if (mediaItem.mediaId == song.id.toString()) {
                    controller.removeMediaItem(i)
                    break
                }
            }
            updateQueueFromController()
        }
    }

    fun playSongFromQueue(song: Song) {
        ensureControllerConnected()
        mediaController?.let { controller ->
            // Find the index of the song in the current queue
            for (i in 0 until controller.mediaItemCount) {
                val mediaItem = controller.getMediaItemAt(i)
                if (mediaItem.mediaId == song.id.toString()) {
                    controller.seekTo(i, 0)
                    controller.play()
                    break
                }
            }
        }
    }

    // Add this function to your MusicController class

    fun reorderQueue(fromIndex: Int, toIndex: Int) {
        ensureControllerConnected()
        mediaController?.let { controller ->
            // Validate indices
            if (fromIndex < 0 || toIndex < 0 ||
                fromIndex >= controller.mediaItemCount || toIndex >= controller.mediaItemCount ||
                fromIndex == toIndex) {
                return
            }

            // Get the media item to move
            val mediaItemToMove = controller.getMediaItemAt(fromIndex)
            val wasPlaying = controller.isPlaying
            val currentPosition = controller.currentPosition
            val currentItemIndex = controller.currentMediaItemIndex

            // Remove the item from its current position
            controller.removeMediaItem(fromIndex)

            // Add it to the new position
            val adjustedToIndex = if (fromIndex < toIndex) toIndex - 1 else toIndex
            controller.addMediaItem(adjustedToIndex, mediaItemToMove)

            // If the currently playing song was moved, update the playback position
            when {
                currentItemIndex == fromIndex -> {
                    // The currently playing song was moved
                    controller.seekTo(adjustedToIndex, currentPosition)
                    if (wasPlaying) controller.play()
                }
                currentItemIndex > fromIndex && currentItemIndex <= toIndex -> {
                    // Current song index shifts down by 1
                    controller.seekTo(currentItemIndex - 1, currentPosition)
                    if (wasPlaying) controller.play()
                }
                currentItemIndex < fromIndex && currentItemIndex >= toIndex -> {
                    // Current song index shifts up by 1
                    controller.seekTo(currentItemIndex + 1, currentPosition)
                    if (wasPlaying) controller.play()
                }
            }

            // Update the queue display
            updateQueueFromController()
        }
    }

    fun clearQueue() {
        ensureControllerConnected()
        mediaController?.let { controller ->
            // Don't clear if only one song or less
            if (controller.mediaItemCount <= 1) {
                return
            }

            // Keep the currently playing song or the first song
            val currentIndex = controller.currentMediaItemIndex
            val songToKeep = if (currentIndex >= 0 && currentIndex < controller.mediaItemCount) {
                controller.getMediaItemAt(currentIndex)
            } else {
                controller.getMediaItemAt(0) // Fallback to first song
            }

            // Clear all items
            controller.clearMediaItems()

            // Add back the song we want to keep
            controller.addMediaItem(songToKeep)

            // If we kept the currently playing song, seek to it and maintain playback state
            if (currentIndex >= 0) {
                controller.seekTo(0, controller.currentPosition)
            }

            // Update the queue display
            updateQueueFromController()
        }
    }

    fun release() {
        stopPositionUpdates()
        MediaController.releaseFuture(
            MediaController.Builder(context, SessionToken(context, ComponentName(context, MusicService::class.java)))
                .buildAsync()
        )
        mediaController = null
    }
}