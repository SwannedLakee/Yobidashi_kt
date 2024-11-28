/*
 * Copyright (c) 2022 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.web.view.reader

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import jp.toastkid.lib.preference.PreferenceApplier

@Composable
internal fun ReaderModeUi(title: String, text: String, onClose: () -> Unit) {
    val preferenceApplier = PreferenceApplier(LocalContext.current)

    val scrollState = rememberScrollState()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .drawBehind { drawRect(Color(preferenceApplier.editorBackgroundColor())) }
            .padding(16.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.weight(1f)
                ) {
                    SelectionContainer {
                        Text(
                            text = title,
                            color = Color(preferenceApplier.editorFontColor()),
                            fontSize = 24.sp,
                            lineHeight = 32.sp,
                            maxLines = 3
                        )
                    }
                }
                Icon(
                    painterResource(jp.toastkid.lib.R.drawable.ic_close_black),
                    contentDescription = stringResource(id = jp.toastkid.lib.R.string.close),
                    tint = Color(preferenceApplier.editorFontColor()),
                    modifier = Modifier
                        .size(44.dp)
                        .padding(8.dp)
                        .clickable(onClick = onClose)
                )
            }
            SelectionContainer {
                Text(
                    text = text,
                    color = Color(preferenceApplier.editorFontColor()),
                    fontSize = 16.sp,
                    modifier = Modifier
                        .verticalScroll(scrollState)
                        .fillMaxWidth()
                        .fillMaxHeight()
                )
            }
        }
    }

}