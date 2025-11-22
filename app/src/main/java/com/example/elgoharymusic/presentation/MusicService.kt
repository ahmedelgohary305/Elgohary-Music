package com.example.elgoharymusic.presentation

import android.os.Bundle
import androidx.annotation.OptIn
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.CommandButton
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSession.ControllerInfo
import androidx.media3.session.MediaSessionService
import androidx.media3.session.SessionCommand
import androidx.media3.session.SessionResult
import com.example.elgoharymusic.R
import com.google.common.collect.ImmutableList
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MusicService : MediaSessionService() {

    private var mediaSession: MediaSession? = null
    private lateinit var player: ExoPlayer

    companion object {
        private const val STOP_ACTION = "STOP_ACTION"
    }

    override fun onCreate() {
        super.onCreate()
        initializeSessionAndPlayer()
    }

    override fun onGetSession(controllerInfo: ControllerInfo): MediaSession? = mediaSession

    @OptIn(UnstableApi::class)
    private fun initializeSessionAndPlayer() {
        player = ExoPlayer.Builder(this)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                    .setUsage(C.USAGE_MEDIA)
                    .build(),
                true
            )
            .setHandleAudioBecomingNoisy(true)
            .build()

        // Create stop command button
        val stopButton = CommandButton.Builder()
            .setDisplayName("Stop")
            .setIconResId(R.drawable.close)
            .setSessionCommand(SessionCommand(STOP_ACTION, Bundle.EMPTY))
            .build()

        mediaSession = MediaSession.Builder(this, player)
            .setCallback(object : MediaSession.Callback {
                override fun onConnect(
                    session: MediaSession,
                    controller: ControllerInfo
                ): MediaSession.ConnectionResult {
                    val connectionResult = super.onConnect(session, controller)
                    return MediaSession.ConnectionResult.accept(
                        connectionResult.availableSessionCommands.buildUpon()
                            .add(SessionCommand(STOP_ACTION, Bundle.EMPTY))
                            .build(),
                        connectionResult.availablePlayerCommands
                    )
                }

                override fun onCustomCommand(
                    session: MediaSession,
                    controller: ControllerInfo,
                    customCommand: SessionCommand,
                    args: Bundle
                ): ListenableFuture<SessionResult> {
                    if (customCommand.customAction == STOP_ACTION) {
                        try {
                            player.stop()
                            player.clearMediaItems()
                            stopSelf()

                        } catch (e: Exception) {
                            stopSelf()
                        }

                        return Futures.immediateFuture(SessionResult(SessionResult.RESULT_SUCCESS))
                    }
                    return super.onCustomCommand(session, controller, customCommand, args)
                }
            })
            .setCustomLayout(ImmutableList.of(stopButton))
            .build()
    }

    @OptIn(UnstableApi::class)
    override fun onDestroy() {
        mediaSession?.let { session ->
            // Clear media items before releasing
            if (::player.isInitialized) {
                player.stop()
                player.clearMediaItems()
                player.release()
            }
            session.release()
            mediaSession = null
        }


        super.onDestroy()
    }
}