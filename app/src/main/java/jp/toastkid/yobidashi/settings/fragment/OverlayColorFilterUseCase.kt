/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.settings.fragment

import android.graphics.Color
import androidx.annotation.ColorInt
import androidx.core.graphics.ColorUtils
import jp.toastkid.lib.preference.PreferenceApplier
import jp.toastkid.yobidashi.R

/**
 * @author toastkidjp
 */
class OverlayColorFilterUseCase(
        private val preferenceApplier: PreferenceApplier,
        colorResolver: (Int) -> Int,
        private val overlayColorFilterViewModel: OverlayColorFilterViewModel?
) {

    private val blueBase = colorResolver(R.color.light_blue_200_dd)

    private val redBase = colorResolver(R.color.red_200_dd)

    private val yellowBase = colorResolver(R.color.default_color_filter)

    private val redYellowBase = colorResolver(R.color.red_yellow)

    private val orangeBase = colorResolver(R.color.deep_orange_500_dd)

    private val greenBase = colorResolver(R.color.lime_bg)

    private val darkBase = colorResolver(R.color.darkgray_scale)

    fun setBlue() {
        setNewColor(currentAlpha(), blueBase)
    }

    fun setRed() {
        setNewColor(currentAlpha(), redBase)
    }

    fun setYellow() {
        setNewColor(currentAlpha(), yellowBase)
    }

    fun setOrange() {
        setNewColor(currentAlpha(), orangeBase)
    }

    fun setRedYellow() {
        setNewColor(currentAlpha(), redYellowBase)
    }

    fun setGreen() {
        setNewColor(currentAlpha(), greenBase)
    }

    fun setDark() {
        setNewColor(currentAlpha(), darkBase)
    }

    fun setAlpha(alpha: Int) {
        setNewColor(alpha, preferenceApplier.filterColor(DEFAULT_COLOR))
    }

    fun setDefault() {
        setNewColor(DEFAULT_ALPHA, yellowBase)
    }

    private fun currentAlpha(): Int = Color.alpha(preferenceApplier.filterColor(DEFAULT_COLOR))

    private fun setNewColor(alpha: Int, @ColorInt newBaseColor: Int) {
        val newColor = ColorUtils.setAlphaComponent(newBaseColor, alpha)
        preferenceApplier.setFilterColor(newColor)
        overlayColorFilterViewModel?.update()
    }

    companion object {
        private const val DEFAULT_COLOR = Color.TRANSPARENT

        private const val DEFAULT_ALPHA = 34

        fun getDefaultAlpha() = DEFAULT_ALPHA
    }


}