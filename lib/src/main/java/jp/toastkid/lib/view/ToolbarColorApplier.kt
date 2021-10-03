/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.lib.view

import android.content.res.ColorStateList
import androidx.appcompat.widget.Toolbar
import androidx.core.graphics.drawable.DrawableCompat
import com.google.android.material.bottomappbar.BottomAppBar
import jp.toastkid.lib.preference.ColorPair

/**
 * @author toastkidjp
 */
class ToolbarColorApplier {

    operator fun invoke(toolbar: Toolbar, colorPair: ColorPair) {
        toolbar.let {
            it.backgroundTintList = ColorStateList.valueOf(colorPair.bgColor())

            val fontColor = colorPair.fontColor()
            it.setTitleTextColor(fontColor)
            it.setSubtitleTextColor(fontColor)

            it.navigationIcon?.let { icon -> DrawableCompat.setTint(icon, fontColor) }
            it.overflowIcon?.let { icon -> DrawableCompat.setTint(icon, fontColor) }
        }

        if (toolbar is BottomAppBar) {
            toolbar.backgroundTint = ColorStateList.valueOf(colorPair.bgColor())
        }
    }
}