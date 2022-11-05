/*
 * Copyright (c) 2022 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.yobidashi.settings.view.screen

import androidx.activity.ComponentActivity
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import jp.toastkid.lib.ContentViewModel
import jp.toastkid.lib.model.OptionMenu
import jp.toastkid.lib.preference.PreferenceApplier
import jp.toastkid.ui.dialog.DestructiveChangeConfirmDialog
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.libs.db.DatabaseFinder
import jp.toastkid.yobidashi.settings.color.DefaultColorInsertion
import jp.toastkid.yobidashi.settings.color.RandomColorInsertion
import jp.toastkid.yobidashi.settings.color.SavedColor
import jp.toastkid.yobidashi.settings.color.SavedColorRepository
import jp.toastkid.yobidashi.settings.view.ColorPaletteUi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun ColorSettingUi() {
    val context = LocalContext.current
    val preferenceApplier = PreferenceApplier(context)

    val repository = remember { DatabaseFinder().invoke(context).savedColorRepository() }

    val colorPair = preferenceApplier.colorPair()
    val initialBgColor = colorPair.bgColor()
    val initialFontColor = colorPair.fontColor()

    val coroutineScope = rememberCoroutineScope()

    val contentViewModel = (context as? ViewModelStoreOwner)?.let {
        viewModel(ContentViewModel::class.java, it)
    }

    val savedColors = remember { mutableStateListOf<SavedColor>() }

    val currentBackgroundColor =
        remember { mutableStateOf(Color(preferenceApplier.color)) }

    val currentFontColor =
        remember { mutableStateOf(Color(preferenceApplier.fontColor)) }

    val openConfirmDialog = remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.padding(start = 8.dp, end = 8.dp, top = 8.dp)
    ) {
        ColorPaletteUi(
            currentBackgroundColor,
            currentFontColor,
            initialBgColor,
            initialFontColor,
            onCommit = {
                val bgColor = currentBackgroundColor.value
                val fontColor = currentFontColor.value

                commitNewColor(
                    preferenceApplier,
                    currentBackgroundColor,
                    currentFontColor,
                    bgColor,
                    fontColor
                )
                contentViewModel?.snackShort(R.string.settings_color_done_commit)
                contentViewModel?.refresh()

                CoroutineScope(Dispatchers.IO).launch {
                    val savedColor =
                        SavedColor.make(
                            bgColor.toArgb(),
                            fontColor.toArgb()
                        )
                    val newId = repository.add(savedColor)
                    savedColor.id = newId
                    savedColors.add(savedColor)
                }
            },
            onReset = {
                commitNewColor(
                    preferenceApplier,
                    currentBackgroundColor,
                    currentFontColor,
                    Color(initialBgColor),
                    Color(initialFontColor)
                )

                contentViewModel?.snackShort(R.string.settings_color_done_reset)
                contentViewModel?.refresh()
            }
        )

        if (savedColors.isNotEmpty()) {
            Surface(
                shadowElevation = 4.dp,
                modifier = Modifier
                    .height(44.dp)
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            ) {
                Text(
                    stringResource(id = R.string.settings_color_saved_title),
                    fontSize = 18.sp,
                    modifier = Modifier.padding(8.dp)
                )
            }
        }

        LazyVerticalGrid(columns = GridCells.Fixed(3)) {
            items(savedColors, { it.id }) { savedColor ->
                Surface(
                    shadowElevation = 4.dp,
                    modifier = Modifier
                        .clickable {
                            commitNewColor(
                                preferenceApplier,
                                currentBackgroundColor,
                                currentFontColor,
                                Color(savedColor.bgColor),
                                Color(savedColor.fontColor)
                            )
                            contentViewModel?.snackShort(R.string.settings_color_done_commit)
                            contentViewModel?.refresh()
                        }
                        .weight(1f)
                        .padding(8.dp)
                        .animateItemPlacement()
                ) {
                    Box(
                        modifier = Modifier
                            .height(100.dp)
                            .background(Color(savedColor.bgColor))
                            .padding(8.dp)
                    ) {
                        Text(
                            text = stringResource(id = R.string.sample_a),
                            color = Color(savedColor.fontColor),
                            modifier = Modifier.align(
                                Alignment.Center
                            )
                        )
                        Icon(
                            painterResource(id = R.drawable.ic_remove_circle),
                            contentDescription = stringResource(
                                id = R.string.delete
                            ),
                            modifier = Modifier
                                .width(40.dp)
                                .height(40.dp)
                                .clickable {
                                    coroutineScope.launch {
                                        withContext(Dispatchers.IO) {
                                            repository.delete(savedColor)
                                            savedColors.remove(savedColor)
                                        }
                                    }
                                }
                                .align(Alignment.TopEnd))
                    }
                }
            }
        }
    }


    viewModel(modelClass = ContentViewModel::class.java).optionMenus(
        OptionMenu(
            titleId = R.string.title_clear_saved_color,
            action = {
                openConfirmDialog.value = true
            }
        ),
        OptionMenu(
            titleId = R.string.title_add_recommended_colors,
            action = {
                DefaultColorInsertion().insert(context)
                coroutineScope.launch {
                    reload(repository, savedColors)
                }
            }
        ),
        OptionMenu(
            titleId = R.string.title_add_random,
            action = {
                RandomColorInsertion(repository)() {
                    coroutineScope.launch {
                        reload(repository, savedColors)
                    }
                }
                contentViewModel?.snackShort(R.string.done_addition)
            }
        )
    )

    DestructiveChangeConfirmDialog(
        openConfirmDialog,
        titleId = R.string.title_clear_saved_color
    ) {
        CoroutineScope(Dispatchers.Main).launch {
            withContext(Dispatchers.IO) {
                DatabaseFinder().invoke(context).searchHistoryRepository().deleteAll()
            }

            (context as? ComponentActivity)?.let { activity ->
                ViewModelProvider(activity).get(ContentViewModel::class.java)
                    .snackShort(R.string.settings_color_delete)
            }
        }
    }

    LaunchedEffect("initial_load") {
        reload(repository, savedColors)
    }
}

private suspend fun reload(
    repository: SavedColorRepository,
    savedColors: SnapshotStateList<SavedColor>
) {
    withContext(Dispatchers.IO) {
        savedColors.clear()
        savedColors.addAll(
            repository.findAll()
        )
    }
}

/**
 * Commit new color.
 *
 * @param bgColor   Background color int
 * @param fontColor Font color int
 */
private fun commitNewColor(
    preferenceApplier: PreferenceApplier,
    currentBackgroundColor: MutableState<Color>,
    currentFontColor: MutableState<Color>,
    bgColor: Color,
    fontColor: Color
) {
    preferenceApplier.color = bgColor.toArgb()
    preferenceApplier.fontColor = fontColor.toArgb()

    currentBackgroundColor.value = bgColor
    currentFontColor.value = fontColor
}