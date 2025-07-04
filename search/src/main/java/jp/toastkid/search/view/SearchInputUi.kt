/*
 * Copyright (c) 2022 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.search.view

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.speech.RecognizerIntent
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import jp.toastkid.api.trend.TrendApi
import jp.toastkid.lib.ContentViewModel
import jp.toastkid.lib.model.OptionMenu
import jp.toastkid.lib.network.NetworkChecker
import jp.toastkid.lib.preference.PreferenceApplier
import jp.toastkid.search.R
import jp.toastkid.search.SearchAction
import jp.toastkid.search.SearchCategory
import jp.toastkid.search.favorite.FavoriteSearchListUi
import jp.toastkid.search.history.SearchHistoryListUi
import jp.toastkid.search.usecase.QueryingUseCase
import jp.toastkid.search.viewmodel.SearchUiViewModel
import jp.toastkid.search.voice.VoiceSearchIntentFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.IOException

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SearchInputUi(
    inputQuery: String? = null,
    currentTitle: String? = null,
    currentUrl: String? = null
) {
    val context = LocalContext.current as? ComponentActivity ?: return

    val contentViewModel = viewModel(ContentViewModel::class.java, context)

    val viewModel = remember {
        val vm = SearchUiViewModel(QueryingUseCase.make(context))
        vm.copyFrom(PreferenceApplier(context))
        vm.setCategoryName(
            (SearchCategory.findByUrlOrNull(currentUrl)?.name
                ?: PreferenceApplier(context).getDefaultSearchEngine())
                ?: SearchCategory.getDefaultCategoryName()
        )
        vm
    }

    val keyboardController = LocalSoftwareKeyboardController.current

    val localLifecycleOwner = LocalLifecycleOwner.current

    if (viewModel.openFavoriteSearch.value.not()) {
        LaunchedEffect(key1 = localLifecycleOwner, block = {
            contentViewModel.replaceAppBarContent {
                val focusRequester = remember { FocusRequester() }

                val spinnerOpen = remember { mutableStateOf(false) }

                val useVoice = remember { mutableStateOf(false) }

                val voiceSearchLauncher =
                    rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { activityResult ->
                        if (activityResult.resultCode != Activity.RESULT_OK) {
                            return@rememberLauncherForActivityResult
                        }
                        val result =
                            activityResult.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                        if (result == null || result.size == 0) {
                            return@rememberLauncherForActivityResult
                        }

                        viewModel.replaceSuggestions(result)
                    }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    SearchCategorySpinner(
                        spinnerOpen.value,
                        { spinnerOpen.value = true },
                        { spinnerOpen.value = false },
                        viewModel.categoryName(),
                        viewModel::setCategory
                    )

                    TextField(
                        value = viewModel.input.value,
                        onValueChange = { text ->
                            viewModel.setInput(text)
                            useVoice.value = text.text.isBlank()
                        },
                        label = {
                            Text(
                                stringResource(id = jp.toastkid.lib.R.string.title_search),
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            cursorColor = MaterialTheme.colorScheme.onPrimary,
                            focusedIndicatorColor = MaterialTheme.colorScheme.onPrimary,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        singleLine = true,
                        textStyle = TextStyle(
                            color = MaterialTheme.colorScheme.onPrimary,
                            textAlign = TextAlign.Start,
                        ),
                        trailingIcon = {
                            Icon(
                                Icons.Filled.Clear,
                                contentDescription = "clear text",
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier
                                    .clickable {
                                        viewModel.setInput(TextFieldValue())
                                    }
                            )
                        },
                        maxLines = 1,
                        keyboardActions = KeyboardActions {
                            keyboardController?.hide()
                            search(context, contentViewModel, currentUrl, viewModel.categoryName(), viewModel.input.value.text)
                        },
                        keyboardOptions = KeyboardOptions(
                            autoCorrectEnabled = true,
                            imeAction = ImeAction.Search
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 4.dp)
                            .drawBehind { drawRect(Color.Transparent) }
                            .focusRequester(focusRequester)
                    )

                    Icon(
                        painterResource(id = if (useVoice.value) jp.toastkid.lib.R.drawable.ic_mic else R.drawable.ic_search_white),
                        contentDescription = stringResource(id = R.string.title_search_action),
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier
                            .width(32.dp)
                            .fillMaxHeight()
                            .align(Alignment.CenterVertically)
                            .combinedClickable(
                                true,
                                onClick = {
                                    keyboardController?.hide()

                                    if (useVoice.value) {
                                        try {
                                            voiceSearchLauncher.launch(VoiceSearchIntentFactory().invoke())
                                        } catch (e: ActivityNotFoundException) {
                                            Timber.e(e)
                                        }
                                        return@combinedClickable
                                    }

                                    search(
                                        context,
                                        contentViewModel,
                                        currentUrl,
                                        viewModel.categoryName(),
                                        viewModel.input.value.text
                                    )
                                },
                                onLongClick = {
                                    search(
                                        context,
                                        contentViewModel,
                                        currentUrl,
                                        viewModel.categoryName(),
                                        viewModel.input.value.text,
                                        true
                                    )
                                }
                            )
                    )
                }
                LaunchedEffect(key1 = viewModel, block = {
                    viewModel.startReceiver()

                    CoroutineScope(Dispatchers.IO).launch {
                        val trendItems = try {
                            TrendApi()()
                        } catch (e: IOException) {
                            Timber.e(e)
                            null
                        }

                        viewModel.replaceTrends(trendItems)
                    }

                    val text = inputQuery ?: ""
                    viewModel.setInput(TextFieldValue(text, TextRange(0, text.length), TextRange(text.length)))
                    focusRequester.requestFocus()
                })
            }
        })
    }

    if (viewModel.enableBackHandler().not()) {
        SearchContentsUi(viewModel, currentTitle, currentUrl)
    }

    if (viewModel.openSearchHistory.value) {
        SearchHistoryListUi()
    }

    if (viewModel.openFavoriteSearch.value) {
        FavoriteSearchListUi()
    }
    
    BackHandler(viewModel.enableBackHandler()) {
        viewModel.closeOption()
    }

    LaunchedEffect(key1 = localLifecycleOwner, block = {
        contentViewModel.optionMenus(
            OptionMenu(
                titleId = jp.toastkid.lib.R.string.title_context_editor_double_quote,
                action = {
                    val queryOrEmpty = viewModel.input.value.text
                    if (queryOrEmpty.isNotBlank()) {
                        viewModel.putQuery("\"$queryOrEmpty\"")
                    }
                }
            ),
            OptionMenu(
                titleId = R.string.title_context_editor_set_default_search_category,
                action = {
                    viewModel.setCategoryName(
                        PreferenceApplier(context).getDefaultSearchEngine()
                            ?: SearchCategory.getDefaultCategoryName()
                    )
                }
            ),
            OptionMenu(
                titleId = R.string.title_enable_suggestion,
                action = {
                    val preferenceApplier = PreferenceApplier(context)
                    preferenceApplier.switchEnableSuggestion()
                    viewModel.copyFrom(preferenceApplier)
                    if (preferenceApplier.isEnableSuggestion.not()) {
                        viewModel.clearSuggestions()
                    }
                },
                check = { viewModel.isEnableSuggestion() }
            ),
            OptionMenu(
                titleId = R.string.title_use_search_history,
                action = {
                    val preferenceApplier = PreferenceApplier(context)
                    preferenceApplier.switchEnableSearchHistory()
                    viewModel.copyFrom(preferenceApplier)
                    if (preferenceApplier.isEnableSearchHistory.not()) {
                        viewModel.clearSearchHistories()
                    }
                },
                check = { viewModel.isEnableSearchHistory() }
            ),
            OptionMenu(
                titleId = R.string.title_favorite_search,
                action = {
                    viewModel.openFavoriteSearch()
                }
            ),
            OptionMenu(
                titleId = R.string.title_search_history,
                action = {
                    viewModel.openSearchHistory()
                }
            )
        )

        viewModel.search.collect {
            if (it.background.not()) {
                keyboardController?.hide()
            }

            search(
                context,
                contentViewModel,
                currentUrl,
                it.category ?: viewModel.categoryName(),
                it.query,
                it.background
            )
        }
    })

    DisposableEffect(key1 = localLifecycleOwner, effect = {
        onDispose {
            viewModel.dispose()
        }
    })
}

private inline fun search(
    context: Context,
    contentViewModel: ContentViewModel?,
    currentUrl: String?,
    category: String,
    query: String,
    onBackground: Boolean = false
) {
    if (NetworkChecker().isNotAvailable(context)) {
        contentViewModel?.snackShort("Network is not available...")
        return
    }

    if (query.isBlank()) {
        contentViewModel?.snackShort(R.string.message_should_input_keyword)
        return
    }

    SearchAction(context, category, query, currentUrl, onBackground).invoke()
}