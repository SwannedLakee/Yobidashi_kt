/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.media.music

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.PlaybackParams
import android.net.Uri
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.view.KeyEvent
import androidx.core.app.NotificationManagerCompat
import androidx.media.MediaBrowserServiceCompat
import jp.toastkid.lib.compat.getParcelableCompat
import jp.toastkid.lib.preference.PreferenceApplier
import jp.toastkid.media.music.notification.NotificationFactory
import timber.log.Timber

/**
 * @author toastkidjp
 */
class MediaPlayerService : MediaBrowserServiceCompat() {

    private lateinit var stateBuilder: PlaybackStateCompat.Builder

    private lateinit var notificationManager: NotificationManagerCompat

    private lateinit var mediaSession: MediaSessionCompat

    private lateinit var preferenceApplier: PreferenceApplier

    private lateinit var notificationFactory: NotificationFactory

    private val mediaPlayer = MediaPlayer()

    private var audioNoisyReceiver: BroadcastReceiver? = null

    private var playbackSpeedReceiver: BroadcastReceiver? = null

    private val callback = object : MediaSessionCompat.Callback() {
        @PlaybackStateCompat.State
        private var mediaState: Int = PlaybackStateCompat.STATE_NONE

        override fun onPlayFromUri(uri: Uri?, extras: Bundle?) {
            super.onPlayFromUri(uri, extras)
            registerReceivers()

            mediaPlayer.reset()
            if (uri != null) {
                mediaPlayer.setDataSource(this@MediaPlayerService, uri)
            }
            mediaPlayer.isLooping = true
            mediaPlayer.prepare()

            mediaSession.setMetadata(
                    MusicFileFinder(contentResolver).invoke().firstOrNull { it.description?.mediaUri == uri }
            )
            mediaSession.isActive = true

            mediaPlayer.start()
            setNewState(PlaybackStateCompat.STATE_PLAYING)
            startService(Intent(baseContext, MediaPlayerService::class.java))
            val notification = notificationFactory() ?: return
            notificationManager.notify(NOTIFICATION_ID, notification)
            startForeground(NOTIFICATION_ID, notificationFactory())
        }

        override fun onPlay() {
            if (mediaSession.controller.metadata == null) {
                return
            }

            registerReceivers()

            mediaSession.isActive = true
            mediaPlayer.start()
            setNewState(PlaybackStateCompat.STATE_PLAYING)
            val notification = notificationFactory() ?: return
            notificationManager.notify(NOTIFICATION_ID, notification)
            startForeground(NOTIFICATION_ID, notificationFactory())
        }

        override fun onPause() {
            unregisterReceivers()

            mediaSession.isActive = false
            mediaPlayer.pause()
            setNewState(PlaybackStateCompat.STATE_PAUSED)
            val notification = notificationFactory() ?: return
            notificationManager.notify(NOTIFICATION_ID, notification)
            stopForeground(false)
        }

        override fun onStop() {
            super.onStop()
            try {
                unregisterReceivers()
            } catch (e: IllegalArgumentException) {
                Timber.w(e)
            }
            mediaSession.isActive = false
            mediaPlayer.stop()
            setNewState(PlaybackStateCompat.STATE_STOPPED)
            notificationManager.cancel(NOTIFICATION_ID)
            stopForeground(true)
        }

        override fun onSetRepeatMode(repeatMode: Int) {
            mediaSession.setRepeatMode(repeatMode)
            when (repeatMode) {
                PlaybackStateCompat.REPEAT_MODE_ONE -> {
                    //mediaPlayer.isLooping = true
                }
                else -> {
                    //mediaPlayer.isLooping = false
                }
            }
        }

        override fun onMediaButtonEvent(mediaButtonEvent: Intent): Boolean {
            val keyEvent = mediaButtonEvent.getParcelableCompat<KeyEvent?>(Intent.EXTRA_KEY_EVENT)
            if (keyEvent == null || keyEvent.action != KeyEvent.ACTION_DOWN) {
                return false
            }
            return when (keyEvent.keyCode) {
                KeyEvent.KEYCODE_MEDIA_NEXT,
                KeyEvent.KEYCODE_MEDIA_SKIP_FORWARD -> {
                    onSetRepeatMode(PlaybackStateCompat.REPEAT_MODE_ONE)
                    true
                }
                else -> super.onMediaButtonEvent(mediaButtonEvent)
            }
        }

        private fun setNewState(@PlaybackStateCompat.State newState: Int) {
            mediaState = newState
            stateBuilder = PlaybackStateCompat.Builder()
            stateBuilder
                    .setActions(PLAYBACK_ACTION)
                    .setState(newState, mediaPlayer.currentPosition.toLong(), 1.0f)
            mediaSession.setPlaybackState(stateBuilder.build())
        }

        override fun onSeekTo(pos: Long) {
            super.onSeekTo(pos)
            mediaPlayer.seekTo(pos.toInt())
        }
    }

    private fun registerReceivers() {
        initializeReceiversIfNeed()

        registerReceiver(audioNoisyReceiver, audioNoisyFilter)
        registerReceiver(playbackSpeedReceiver, audioSpeedFilter)
    }

    private fun initializeReceiversIfNeed() {
        if (audioNoisyReceiver == null) {
            audioNoisyReceiver = object : BroadcastReceiver() {
                override fun onReceive(context: Context, intent: Intent) {
                    mediaSession.controller.transportControls.pause()
                }
            }
        }
        if (playbackSpeedReceiver == null) {
            playbackSpeedReceiver = object : BroadcastReceiver() {
                override fun onReceive(context: Context, intent: Intent) {
                    val speed = intent.getFloatExtra(KEY_EXTRA_SPEED, 1f)
                    mediaPlayer.playbackParams = PlaybackParams().setSpeed(speed)
                }
            }
        }
    }

    override fun onCreate() {
        super.onCreate()

        preferenceApplier = PreferenceApplier(this)
        notificationManager = NotificationManagerCompat.from(this)
        notificationFactory = NotificationFactory(this) { mediaSession }

        mediaSession = MediaSessionCompat(this, javaClass.simpleName).also {
            stateBuilder = PlaybackStateCompat.Builder()
            stateBuilder.setActions(PLAYBACK_ACTION)
            it.setPlaybackState(stateBuilder.build())
            it.setCallback(callback)
            @Suppress("UsePropertyAccessSyntax")
            setSessionToken(it.sessionToken)
        }
    }

    override fun onGetRoot(
            clientPackageName: String,
            clientUid: Int,
            rootHints: Bundle?
    ): BrowserRoot {
        return BrowserRoot(ROOT_ID, null)
    }

    override fun onLoadChildren(
            parentId: String,
            result: Result<MutableList<MediaBrowserCompat.MediaItem>>
    ) {
        result.sendResult(
                MusicFileFinder(contentResolver).invoke()
                        .map { MediaBrowserCompat.MediaItem(it.description, MediaBrowserCompat.MediaItem.FLAG_PLAYABLE) }
                        .toMutableList()
        )
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        unregisterReceivers()
        NotificationManagerCompat.from(this).cancel(NOTIFICATION_ID)

        super.onTaskRemoved(rootIntent)
    }

    override fun onDestroy() {
        unregisterReceivers()
        NotificationManagerCompat.from(this).cancel(NOTIFICATION_ID)

        super.onDestroy()
    }

    private fun unregisterReceivers() {
        if (audioNoisyReceiver != null) {
            unregisterReceiver(audioNoisyReceiver)
        }
        if (playbackSpeedReceiver != null) {
            unregisterReceiver(playbackSpeedReceiver)
        }

        audioNoisyReceiver = null
        playbackSpeedReceiver = null
    }

    companion object {
        private val audioNoisyFilter = IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY)

        private const val ACTION_CHANGE_SPEED = "jp.toastkid.media.audio.speed"

        private val audioSpeedFilter = IntentFilter(ACTION_CHANGE_SPEED)

        private const val ROOT_ID = "media-root"

        private const val NOTIFICATION_ID = 46

        private const val KEY_EXTRA_SPEED = "speed"

        private const val PLAYBACK_ACTION =
                PlaybackStateCompat.ACTION_PLAY or
                        PlaybackStateCompat.ACTION_PAUSE or
                        PlaybackStateCompat.ACTION_PLAY_PAUSE or
                        PlaybackStateCompat.ACTION_STOP or
                        PlaybackStateCompat.ACTION_SEEK_TO

        fun makeSpeedIntent(speed: Float) =
                Intent(ACTION_CHANGE_SPEED)
                        .also { it.putExtra(KEY_EXTRA_SPEED, speed) }
    }

}