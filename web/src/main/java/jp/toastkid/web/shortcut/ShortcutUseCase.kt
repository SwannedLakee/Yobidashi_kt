/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.web.shortcut

import android.content.Context
import android.content.Intent
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.graphics.Bitmap
import android.graphics.drawable.Icon
import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat.getSystemService

/**
 * @author toastkidjp
 */
class ShortcutUseCase(
        private val context: Context,
        private val intentFactory: (String) -> Intent = { Intent(it) }
) {

    operator fun invoke(uri: Uri, title: String, bitmap: Bitmap?, className: String) {
        val shortcutIntent = intentFactory(Intent.ACTION_VIEW)
        shortcutIntent.setClassName(context.applicationContext, className)
        shortcutIntent.data = uri

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            pinShortcutForOreoAndOver(shortcutIntent, title, bitmap)
        else
            pinShortcutUnderOreo(shortcutIntent, title)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun pinShortcutForOreoAndOver(
            shortcutIntent: Intent,
            title: String,
            bitmap: Bitmap?
    ) {
        val applicationContext = context.applicationContext

        val iconBitmap = bitmap ?: return//VectorToBitmap(context).invoke(R.drawable.ic_yobidashi)

        val shortcut = ShortcutInfo.Builder(applicationContext, title)
                .setShortLabel(title)
                .setLongLabel(title)
                .setIcon(Icon.createWithBitmap(iconBitmap))
                .setIntent(shortcutIntent)
                .build()

        val shortcutManager = getSystemService(applicationContext, ShortcutManager::class.java)
        shortcutManager?.requestPinShortcut(shortcut, null)
    }

    private fun pinShortcutUnderOreo(
            shortcutIntent: Intent,
            title: String
    ) {
        val intent = intentFactory("com.android.launcher.action.INSTALL_SHORTCUT")
        intent.putExtra(Intent.EXTRA_SHORTCUT_NAME, title)
        intent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent)

        context.applicationContext.sendBroadcast(intent)
    }

}