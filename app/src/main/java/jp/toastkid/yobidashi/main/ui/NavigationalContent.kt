/*
 * Copyright (c) 2022 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.yobidashi.main.ui

import android.net.Uri
import android.os.Bundle
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.core.net.toUri
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.composable
import jp.toastkid.about.view.AboutThisAppUi
import jp.toastkid.article_viewer.article.detail.view.ArticleContentUi
import jp.toastkid.article_viewer.article.list.view.ArticleListUi
import jp.toastkid.barcode.view.BarcodeReaderUi
import jp.toastkid.editor.view.EditorTabUi
import jp.toastkid.image.view.ImageListUi
import jp.toastkid.loan.view.LoanCalculatorUi
import jp.toastkid.number.NumberPlaceUi
import jp.toastkid.pdf.view.PdfViewerUi
import jp.toastkid.rss.view.RssReaderListUi
import jp.toastkid.todo.view.board.TaskBoardUi
import jp.toastkid.todo.view.list.TaskListUi
import jp.toastkid.yobidashi.BuildConfig
import jp.toastkid.yobidashi.browser.archive.view.ArchiveListUi
import jp.toastkid.yobidashi.browser.bookmark.view.BookmarkListUi
import jp.toastkid.yobidashi.browser.history.view.ViewHistoryListUi
import jp.toastkid.yobidashi.browser.view.WebTabUi
import jp.toastkid.yobidashi.calendar.view.CalendarUi
import jp.toastkid.yobidashi.search.favorite.FavoriteSearchListUi
import jp.toastkid.yobidashi.search.history.SearchHistoryListUi
import jp.toastkid.yobidashi.search.view.SearchInputUi
import jp.toastkid.yobidashi.settings.view.screen.SettingTopUi
import jp.toastkid.yobidashi.tab.TabAdapter
import jp.toastkid.yobidashi.tab.model.EditorTab
import jp.toastkid.yobidashi.tab.model.PdfTab
import jp.toastkid.yobidashi.tab.model.WebTab

@OptIn(ExperimentalAnimationApi::class)
@Composable
internal fun NavigationalContent(
    navigationHostController: NavHostController,
    tabs: TabAdapter
) {
    AnimatedNavHost(
        navController = navigationHostController,
        startDestination = "empty",
        modifier = Modifier
            .fillMaxSize()
    ) {
        composable("empty")  {

        }
        tabComposable("tab/web/current") {
            val currentTab = tabs.currentTab() as? WebTab ?: return@tabComposable
            WebTabUi(currentTab.latest.url().toUri(), currentTab.id())
        }
        tabComposable("tab/pdf/current") {
            val currentTab = tabs.currentTab() as? PdfTab ?: return@tabComposable
            PdfViewerUi(currentTab.getUrl().toUri())
        }
        tabComposable("tab/article/list") {
            ArticleListUi()
        }
        tabComposable("tab/article/content/{title}") {
            val title = it?.getString("title") ?: return@tabComposable
            ArticleContentUi(title)
        }
        tabComposable("tab/editor/current") {
            val currentTab = tabs.currentTab() as? EditorTab ?: return@tabComposable
            EditorTabUi(currentTab.path)
        }
        composable("web/bookmark/list") {
            BookmarkListUi()
        }
        composable("web/history/list") {
            ViewHistoryListUi()
        }
        composable("web/archive/list") {
            ArchiveListUi()
        }
        composable("tool/barcode_reader") {
            BarcodeReaderUi()
        }
        composable("tool/image/list") {
            ImageListUi()
        }
        composable("tool/rss/list") {
            RssReaderListUi()
        }
        composable("tool/number/place") {
            NumberPlaceUi()
        }
        composable("tool/task/list") {
            TaskListUi()
        }
        composable("tool/task/board") {
            TaskBoardUi()
        }
        composable("tool/loan") {
            LoanCalculatorUi()
        }
        composable("tab/calendar") {
            CalendarUi()
        }
        composable("setting/top") {
            SettingTopUi()
        }
        composable("search/top") {
            SearchInputUi()
        }
        composable("search/with/?query={query}&title={title}&url={url}") {
            val query = Uri.decode(it.arguments?.getString("query"))
            val title = Uri.decode(it.arguments?.getString("title"))
            val url = Uri.decode(it.arguments?.getString("url"))
            SearchInputUi(query, title, url)
        }
        composable("search/history/list") {
            SearchHistoryListUi()
        }
        composable("search/favorite/list") {
            FavoriteSearchListUi()
        }
        composable("about") {
            AboutThisAppUi(BuildConfig.VERSION_NAME)
        }
    }

}

@OptIn(ExperimentalAnimationApi::class)
private fun NavGraphBuilder.tabComposable(route: String, content: @Composable (Bundle?) -> Unit) {
    composable(
        route,
        enterTransition = {
            slideInVertically(initialOffsetY = { it })
        },
        exitTransition = {
            slideOutVertically(targetOffsetY = { it })
        }
    ) {
        content(it.arguments)
    }
}