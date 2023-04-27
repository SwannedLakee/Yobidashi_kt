/*
 * Copyright (c) 2022 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.ui.dialog

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import jp.toastkid.ui.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InputFileNameDialogUi(
    openDialog: MutableState<Boolean>,
    defaultInput: String = "",
    onCommit: (String) -> Unit
) {
    if (openDialog.value.not()) {
        return
    }

    val input = remember { mutableStateOf(defaultInput) }
    AlertDialog(
        onDismissRequest = { openDialog.value = false },
        title = {
            Text(
                stringResource(id = R.string.title_dialog_input_file_name),
                style = MaterialTheme.typography.titleMedium
            )
        },
        text = {
            TextField(
                value = input.value,
                onValueChange = { text ->
                    input.value = text
                },
                singleLine = true,
                colors = TextFieldDefaults.textFieldColors(
                    textColor = MaterialTheme.colorScheme.onSurface,
                    containerColor = Color.Transparent,
                    cursorColor = MaterialTheme.colorScheme.onSurface
                ),
                trailingIcon = {
                    Icon(
                        Icons.Filled.Clear,
                        contentDescription = "clear text",
                        modifier = Modifier
                            .offset(x = 8.dp)
                            .clickable {
                                input.value = ""
                            }
                    )
                }
            )
        },
        confirmButton = {
            Text(
                text = stringResource(id = R.string.save),
                color = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier
                    .clickable {
                        if (input.value.isNotBlank()) {
                            onCommit(input.value)
                        }
                        openDialog.value = false
                    }
                    .padding(4.dp)
            )
        },
        dismissButton = {
            Text(
                text = stringResource(id = R.string.cancel),
                color = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier
                    .clickable {
                        openDialog.value = false
                    }
                    .padding(4.dp)
            )
        }
    )
}