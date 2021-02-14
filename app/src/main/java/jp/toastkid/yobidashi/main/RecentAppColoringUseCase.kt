/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.main

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.content.res.Resources
import android.graphics.BitmapFactory
import android.os.Build
import androidx.annotation.ColorInt
import androidx.annotation.RequiresApi
import androidx.core.graphics.ColorUtils
import jp.toastkid.yobidashi.R

/**
 * @author toastkidjp
 */
class RecentAppColoringUseCase(
        private val getString: (Int) -> String,
        private val resourcesSupplier: () -> Resources?,
        private val setTaskDescription: (ActivityManager.TaskDescription) -> Unit,
        private val sdkInt: Int
) {

    @SuppressLint("NewApi")
    operator fun invoke(@ColorInt color: Int) {
        if (sdkInt < Build.VERSION_CODES.LOLLIPOP) {
            return
        }

        val opaqueColor = ColorUtils.setAlphaComponent(color, 255)
        val taskDescription = if (sdkInt >= Build.VERSION_CODES.Q) {
            taskDescriptionForQAndOver(opaqueColor)
        } else {
            taskDescription(opaqueColor)
        } ?: return
        setTaskDescription(taskDescription)
    }

    @RequiresApi(Build.VERSION_CODES.P)
    private fun taskDescriptionForQAndOver(opaqueColor: Int): ActivityManager.TaskDescription {
        return ActivityManager.TaskDescription(
                getString(R.string.app_name),
                R.mipmap.ic_launcher,
                opaqueColor
        )
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun taskDescription(opaqueColor: Int): ActivityManager.TaskDescription? {
        val resources = resourcesSupplier() ?: return null
        return ActivityManager.TaskDescription(
                getString(R.string.app_name),
                BitmapFactory.decodeResource(resources, R.mipmap.ic_launcher),
                opaqueColor
        )
    }

}