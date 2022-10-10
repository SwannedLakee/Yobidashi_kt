/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.media.music.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.media.session.MediaButtonReceiver
import jp.toastkid.lib.preference.PreferenceApplier
import jp.toastkid.media.R
import jp.toastkid.media.music.AlbumArtFinder

/**
 * @author toastkidjp
 */
class NotificationFactory(
        private val context: Context,
        private val mediaSessionSupplier: () -> MediaSessionCompat
) {

    private val preferenceApplier = PreferenceApplier(context)

    private val albumArtFinder = AlbumArtFinder(context.contentResolver)

    private val pauseAction = NotificationCompat.Action(
            R.drawable.ic_pause,
            context.getString(R.string.action_pause),
            MediaButtonReceiver.buildMediaButtonPendingIntent(context, PlaybackStateCompat.ACTION_PAUSE)
    )

    private val playAction = NotificationCompat.Action(
            R.drawable.ic_play_media,
            context.getString(R.string.action_play),
            MediaButtonReceiver.buildMediaButtonPendingIntent(context, PlaybackStateCompat.ACTION_PLAY)
    )

    private val previousAction = NotificationCompat.Action(
        R.drawable.ic_previous_media,
        context.getString(R.string.action_skip_to_previous),
        MediaButtonReceiver.buildMediaButtonPendingIntent(context, PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS)
    )

    private val nextAction = NotificationCompat.Action(
        R.drawable.ic_next_media,
        context.getString(R.string.action_skip_to_next),
        MediaButtonReceiver.buildMediaButtonPendingIntent(context, PlaybackStateCompat.ACTION_SKIP_TO_NEXT)
    )

    operator fun invoke(): Notification? {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel()
        }

        val currentDescription = mediaSessionSupplier().controller?.metadata?.description ?: return null

        val notificationBuilder = NotificationCompat.Builder(context, CHANNEL_ID)
                .setStyle(androidx.media.app.NotificationCompat.MediaStyle()
                        .setMediaSession(mediaSessionSupplier().sessionToken)
                        .setShowActionsInCompactView(0, 1, 2)
                )
                .setColor(preferenceApplier.color)
                .setSmallIcon(R.drawable.ic_music)
                .setLargeIcon(currentDescription.iconUri?.let { albumArtFinder(it) })
                .setContentTitle(currentDescription.title)
                .setContentText(currentDescription.subtitle)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setDeleteIntent(
                        MediaButtonReceiver.buildMediaButtonPendingIntent(
                                context,
                                PlaybackStateCompat.ACTION_STOP
                        )
                )

        notificationBuilder.addAction(previousAction)
        notificationBuilder.addAction(if (isPlaying()) pauseAction else playAction)
        notificationBuilder.addAction(nextAction)
        return notificationBuilder.build()
    }

    private fun isPlaying() =
            mediaSessionSupplier().controller.playbackState.state == PlaybackStateCompat.STATE_PLAYING

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel() {
        val manager: NotificationManager? = context.getSystemService(NotificationManager::class.java)
        if (manager?.getNotificationChannel(CHANNEL_ID) == null) {
            val channel = NotificationChannel(
                    CHANNEL_ID,
                    context.getString(R.string.title_audio_player),
                    NotificationManager.IMPORTANCE_LOW
            ).also {
                it.description = context.getString(R.string.title_audio_player)
            }
            manager?.createNotificationChannel(channel)
        }
    }

    companion object {
        private const val CHANNEL_ID = "music"
    }
}