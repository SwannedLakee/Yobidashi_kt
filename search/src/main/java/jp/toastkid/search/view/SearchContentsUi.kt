/*
 * Copyright (c) 2022 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.search.view

import androidx.activity.ComponentActivity
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import jp.toastkid.data.repository.factory.RepositoryFactory
import jp.toastkid.lib.ContentViewModel
import jp.toastkid.lib.clip.Clipboard
import jp.toastkid.lib.intent.ShareIntentFactory
import jp.toastkid.search.R
import jp.toastkid.search.url_suggestion.ItemDeletionUseCase
import jp.toastkid.search.viewmodel.SearchUiViewModel
import jp.toastkid.ui.image.EfficientImage

@OptIn(
    ExperimentalFoundationApi::class,
    ExperimentalLayoutApi::class
)
@Composable
internal fun SearchContentsUi(
    viewModel: SearchUiViewModel,
    currentTitle: String?,
    currentUrl: String?
) {
    val context = LocalContext.current
    val searchHistoryRepository = remember { RepositoryFactory().searchHistoryRepository(context) }

    val itemDeletionUseCase = remember {
        val database = RepositoryFactory()
        ItemDeletionUseCase(
            database.bookmarkRepository(context),
            database.viewHistoryRepository(context)
        )
    }

    val keyboardController = LocalSoftwareKeyboardController.current

    val contentViewModel = viewModel(
        ContentViewModel::class.java,
        context as ViewModelStoreOwner
    )

    Column(
        modifier = Modifier
            .padding(top = 8.dp)
            .verticalScroll(rememberScrollState())
    ) {
        if (viewModel.useUrlCard() && !currentUrl.isNullOrBlank()) {
            UrlCard(currentTitle, currentUrl) { viewModel.putQuery(it) }
        }

        if (viewModel.urlItems.isNotEmpty()) {
            HeaderWithLink(jp.toastkid.lib.R.string.title_view_history, R.string.link_open_history) {
                contentViewModel.nextRoute("web/history/list")
            }

            viewModel.urlItems.forEach { urlItem ->
                BindItemContent(
                    urlItem,
                    onClick = {
                        keyboardController?.hide()
                        viewModel.search(urlItem.urlString())
                    },
                    onLongClick = {
                        keyboardController?.hide()
                        viewModel.searchOnBackground(urlItem.urlString())
                    },
                    onDelete = {
                        itemDeletionUseCase(urlItem)
                        viewModel.urlItems.remove(urlItem)
                    }
                )
            }
        }

        if (viewModel.enableFavoriteSearchItems()) {
            HeaderWithLink(
                R.string.title_favorite_search,
                jp.toastkid.lib.R.string.open
            ) {
                viewModel.openFavoriteSearch()
            }

            val favoriteSearchRepository =
                remember { RepositoryFactory().favoriteSearchRepository(context) }

            viewModel.favoriteSearchItems().forEach { favoriteSearch ->
                SearchItemContent(
                    favoriteSearch.query,
                    favoriteSearch.category,
                    {
                        keyboardController?.hide()
                        viewModel.searchWithCategory(
                            favoriteSearch.query ?: "",
                            favoriteSearch.category ?: "",
                            it
                        )
                    },
                    {
                        favoriteSearchRepository.delete(favoriteSearch)
                        viewModel.removeFavoriteSearchItems(favoriteSearch)
                    }
                )
            }
        }

        if (viewModel.enableShowSearchHistories()) {
            HeaderWithLink(
                R.string.title_search_history,
                jp.toastkid.lib.R.string.open,
                viewModel::openSearchHistory
            )

            viewModel.searchHistories().forEach { searchHistory ->
                SearchItemContent(
                    searchHistory.query,
                    searchHistory.category,
                    {
                        keyboardController?.hide()
                        viewModel.searchWithCategory(
                            searchHistory.query ?: "",
                            searchHistory.category ?: "",
                            it
                        )
                    },
                    {
                        searchHistoryRepository.delete(searchHistory)
                        viewModel.removeSearchHistories(searchHistory)
                    },
                    searchHistory.timestamp
                )
            }
        }

        if (viewModel.showSuggestions()) {
            Header(R.string.title_search_suggestion)

            FlowRow(modifier = Modifier.padding(start = 8.dp, end = 8.dp)) {
                viewModel.suggestions().forEach {
                    ItemCard {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.combinedClickable(
                                true,
                                onClick = {
                                    keyboardController?.hide()
                                    viewModel.putQuery(it)
                                    viewModel.search(it)
                                },
                                onLongClick = {
                                    viewModel.searchOnBackground(it)
                                }
                            )
                        ) {
                            Text(
                                text = it,
                                fontSize = 16.sp,
                                modifier = Modifier
                                    .wrapContentWidth()
                            )
                            val onSurfaceColor = MaterialTheme.colorScheme.onSurface
                            Text(
                                text = stringResource(id = R.string.plus),
                                color = MaterialTheme.colorScheme.surface,
                                fontSize = 20.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .width(36.dp)
                                    .height(32.dp)
                                    .drawBehind { drawRect(onSurfaceColor) }
                                    .clickable { viewModel.putQuery("$it ") }
                            )
                        }
                    }
                }
            }
        }

        if (viewModel.useTrend()) {
            HeaderWithLink(R.string.hourly_trends, jp.toastkid.lib.R.string.open) {
                viewModel.search("https://trends.google.co.jp/trends/trendingsearches/realtime")
            }

            FlowRow(modifier = Modifier.padding(start = 8.dp, end = 8.dp)) {
                viewModel.trends().forEach {
                    ItemCard {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.combinedClickable(
                                true,
                                onClick = {
                                    keyboardController?.hide()
                                    viewModel.search(it.link)
                                },
                                onLongClick = {
                                    viewModel.searchOnBackground(it.link)
                                }
                            )
                        ) {
                            EfficientImage(
                                model = it.image,
                                contentDescription = it.title,
                                modifier = Modifier.width(40.dp)
                            )
                            Text(
                                text = it.title,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                fontSize = 16.sp,
                                modifier = Modifier
                                    .wrapContentWidth()
                                    .padding(start = 4.dp)
                            )

                            Text(
                                text = stringResource(id = R.string.plus),
                                color = colorResource(id = jp.toastkid.lib.R.color.white),
                                fontSize = 20.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .width(36.dp)
                                    .height(32.dp)
                                    .drawBehind { drawRect(Color(0xFFAAAAAA)) }
                                    .clickable { viewModel.putQuery("${it.title} ") }
                            )
                        }
                    }
                }
            }

        }
    }
}

@Composable
private fun UrlCard(currentTitle: String?, currentUrl: String, setInput: (String) -> Unit) {
    val context = LocalContext.current

    Surface(
        shadowElevation = 4.dp,
        modifier = Modifier
            .padding(horizontal = 8.dp)
            .padding(bottom = 8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = currentTitle ?: "",
                    fontSize = 15.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(start = 4.dp, end = 4.dp)
                )
                Text(
                    text = currentUrl,
                    color = colorResource(id = jp.toastkid.lib.R.color.link_blue),
                    fontSize = 14.sp,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(start = 4.dp, end = 4.dp)
                )
            }
            Icon(
                painterResource(id = jp.toastkid.lib.R.drawable.ic_share),
                contentDescription = stringResource(id = jp.toastkid.lib.R.string.share),
                tint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier
                    .width(32.dp)
                    .clickable {
                        context.startActivity(
                            ShareIntentFactory().invoke(
                                currentUrl,
                                currentTitle
                            )
                        )
                    }
            )
            Icon(
                painterResource(id = jp.toastkid.lib.R.drawable.ic_clip),
                contentDescription = stringResource(id = jp.toastkid.lib.R.string.clip),
                tint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier
                    .width(32.dp)
                    .clickable {
                        Clipboard.clip(context, currentUrl)
                        val activity = context as? ComponentActivity ?: return@clickable
                        ViewModelProvider(activity)
                            .get(ContentViewModel::class.java)
                            .snackShort(
                                context.getString(
                                    jp.toastkid.lib.R.string.message_clip_to,
                                    currentUrl
                                )
                            )
                    }
            )
            Icon(
                painterResource(id = R.drawable.ic_edit_black),
                contentDescription = stringResource(id = jp.toastkid.lib.R.string.edit),
                tint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier
                    .width(32.dp)
                    .clickable { setInput(currentUrl) }
            )
        }
    }
}

@Composable
private fun Header(headerTextId: Int) {
    Surface(
        shadowElevation = 4.dp,
        modifier = Modifier
            .padding(start = 8.dp, end = 8.dp)
    ) {
        Text(
            text = stringResource(id = headerTextId),
            modifier = Modifier
                .padding(start = 8.dp, end = 8.dp)
                .fillMaxWidth()
                .padding(8.dp)
        )
    }
}

@Composable
private fun HeaderWithLink(headerTextId: Int, linkTextId: Int, onLinkClick: () -> Unit) {
    Surface(
        shadowElevation = 4.dp,
        modifier = Modifier
            .padding(start = 8.dp, end = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .padding(start = 8.dp, end = 8.dp)
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Text(
                stringResource(id = headerTextId),
                modifier = Modifier
                    .align(Alignment.CenterStart)
            )
            Text(
                stringResource(id = linkTextId),
                color = colorResource(id = jp.toastkid.lib.R.color.link_blue),
                modifier = Modifier
                    .clickable(onClick = onLinkClick)
                    .align(Alignment.CenterEnd)
            )
        }
    }
}

@Composable
private fun ItemCard(content: @Composable () -> Unit) {
    Surface(
        shadowElevation = 4.dp,
        content = content,
        modifier = Modifier.padding(2.dp)
    )
}