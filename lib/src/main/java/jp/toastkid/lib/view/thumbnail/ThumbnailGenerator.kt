/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.lib.view.thumbnail

import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.View
import androidx.annotation.UiThread
import timber.log.Timber
import kotlin.math.min
import androidx.core.graphics.createBitmap

/**
 * @author toastkidjp
 */
class ThumbnailGenerator {

    @UiThread
    operator fun invoke(view: View?): Bitmap? {
        if (view == null || view.measuredWidth == 0 || view.measuredHeight == 0) {
            return null
        }

        val displayMetrics = view.context.resources.displayMetrics
        val bitmap = createBitmap(
            min(view.measuredWidth, displayMetrics.widthPixels),
            min(view.measuredHeight, displayMetrics.heightPixels)
        )

        val c = Canvas(bitmap)

        view.invalidate()

        // For avoiding IllegalArgumentException: Software rendering doesn't support hardware bitmaps
        try {
            view.draw(c)
        } catch (e: IllegalArgumentException) {
            Timber.e(e)
            return null
        } catch (e: IllegalStateException) {
            Timber.e(e)
            return null
        }

        return bitmap
    }
}