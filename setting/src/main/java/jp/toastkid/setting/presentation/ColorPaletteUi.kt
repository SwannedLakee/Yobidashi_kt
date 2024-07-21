/*
 * Copyright (c) 2022 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.setting.presentation

import androidx.annotation.ColorInt
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.godaddy.android.colorpicker.ClassicColorPicker
import jp.toastkid.setting.R

@Composable
internal fun ColorPaletteUi(
    currentBackgroundColor: MutableState<Color>,
    currentFontColor: MutableState<Color>,
    @ColorInt initialBgColor: Int,
    @ColorInt initialFontColor: Int,
    onCommit: () -> Unit,
    onReset: () -> Unit
) {
    Surface(
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    stringResource(id = R.string.settings_color_background_title),
                    fontSize = 16.sp,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
                ClassicColorPicker(
                    color = currentBackgroundColor.value,
                    onColorChanged = { hsvColor ->
                        currentBackgroundColor.value = hsvColor.toColor()
                    },
                    modifier = Modifier.height(200.dp)
                )
                Button(
                    onClick = onCommit,
                    colors = ButtonDefaults.textButtonColors(
                        containerColor = currentBackgroundColor.value,
                        contentColor = currentFontColor.value,
                        disabledContentColor = Color.LightGray
                    ),
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Text(stringResource(id = jp.toastkid.lib.R.string.commit), fontSize = 14.sp)
                }
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .wrapContentHeight()
            ) {
                Text(
                    stringResource(id = R.string.settings_color_font_title),
                    fontSize = 16.sp,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
                ClassicColorPicker(
                    color = currentFontColor.value,
                    modifier = Modifier.height(200.dp),
                    onColorChanged = { hsvColor ->
                        currentFontColor.value = hsvColor.toColor()
                    }
                )
                Button(
                    onClick = onReset,
                    colors = ButtonDefaults.textButtonColors(
                        containerColor = Color(initialBgColor),
                        contentColor = Color(initialFontColor),
                        disabledContentColor = Color.LightGray
                    ),
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Text(stringResource(id = jp.toastkid.lib.R.string.reset), fontSize = 14.sp)
                }
            }
        }
    }
}