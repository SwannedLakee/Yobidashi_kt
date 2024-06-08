/*
 * Copyright (c) 2022 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.yobidashi.settings.view.screen

import androidx.annotation.ColorRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import jp.toastkid.lib.ContentViewModel
import jp.toastkid.lib.preference.PreferenceApplier
import jp.toastkid.ui.parts.InsetDivider
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.settings.fragment.OverlayColorFilterUseCase
import jp.toastkid.yobidashi.settings.view.SwitchRow
import kotlin.math.roundToInt

@Composable
internal fun ColorFilterSettingUi() {
    val activityContext = LocalContext.current
    val preferenceApplier = PreferenceApplier(activityContext)

    val sample =
        remember { mutableStateOf(preferenceApplier.filterColor(Color.Transparent.toArgb())) }
    val sliderValue =
        remember { mutableStateOf(OverlayColorFilterUseCase.getDefaultAlpha().toFloat() / 255f) }
    val check = remember { mutableStateOf(preferenceApplier.useColorFilter()) }

    val contentViewModel = (activityContext as? ViewModelStoreOwner)?.let{
        viewModel(ContentViewModel::class.java, activityContext)
    }

    val useCase = remember {
        OverlayColorFilterUseCase(
            preferenceApplier,
            { ContextCompat.getColor(activityContext, it) },
            contentViewModel
        )
    }

    Surface(shadowElevation = 4.dp, modifier = Modifier.padding(8.dp)) {
        Column {
            val onClick = {
                val newState = !preferenceApplier.useColorFilter()
                preferenceApplier.setUseColorFilter(newState)
                contentViewModel?.refresh()

                check.value = preferenceApplier.useColorFilter()
            }

            SwitchRow(
                textId = R.string.title_color_filter,
                clickable = onClick,
                booleanState = check,
                iconTint = MaterialTheme.colorScheme.secondary,
                iconId = R.drawable.ic_color_filter_black
            )

            InsetDivider()

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(40.dp)
                ) {
                    Text(
                        text = stringResource(id = R.string.sample_text_color_filter),
                        maxLines = 1
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(sample.value))
                    ) { }
                }

                Text(
                    text = stringResource(id = R.string.title_filter_color),
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 4.dp, end = 4.dp)
                )
                Button(
                    onClick = {
                        useCase.setDefault()
                        sliderValue.value = OverlayColorFilterUseCase
                            .getDefaultAlpha()
                            .toFloat()
                    },
                    colors = ButtonDefaults.textButtonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        disabledContentColor = Color.LightGray
                    ),
                    modifier = Modifier
                        .wrapContentWidth()
                        .padding(8.dp)
                ) {
                    Text(
                        text = stringResource(id = R.string.title_default),
                        textAlign = TextAlign.Center
                    )
                }
            }

            InsetDivider()

            Row(
                modifier = Modifier
                    .horizontalScroll(rememberScrollState())
                    .padding(start = 16.dp, end = 16.dp, top = 4.dp)
            ) {
                ColorFilterItem(R.color.default_color_filter) { useCase.setYellow() }
                ColorFilterItem(R.color.red_yellow) { useCase.setRedYellow() }
                ColorFilterItem(R.color.deep_orange_500_dd) { useCase.setOrange() }
                ColorFilterItem(R.color.darkgray_scale) { useCase.setDark() }
                ColorFilterItem(R.color.red_200_dd) { useCase.setRed() }
                ColorFilterItem(R.color.lime_bg) { useCase.setGreen() }
                ColorFilterItem(R.color.light_blue_200_dd) { useCase.setBlue() }
            }

            Slider(
                value = sliderValue.value,
                onValueChange = {
                    sliderValue.value = it
                    useCase.setAlpha(((255) * it).roundToInt())
                },
                steps = 256,
                modifier = Modifier.padding(
                    top = 8.dp,
                    bottom = 8.dp,
                    start = 16.dp,
                    end = 16.dp
                )
            )

            Spacer(modifier = Modifier.height(200.dp))
        }
    }
}

@Composable
private fun ColorFilterItem(@ColorRes colorId: Int, onClick: () -> Unit) {
    Surface(shadowElevation = 4.dp) {
        Box(
            modifier = Modifier
                .background(colorResource(id = colorId))
                .size(40.dp)
                .clickable(onClick = onClick)
                .padding(start = 8.dp)
        ) { }
    }
}
