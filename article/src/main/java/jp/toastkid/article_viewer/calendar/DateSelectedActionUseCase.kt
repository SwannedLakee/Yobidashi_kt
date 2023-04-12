/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.article_viewer.calendar

import jp.toastkid.article_viewer.article.ArticleRepository
import jp.toastkid.lib.ContentViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * @author toastkidjp
 */
class DateSelectedActionUseCase(
    private val repository: ArticleRepository,
    private val viewModel: ContentViewModel?,
    private val mainDispatcher: CoroutineDispatcher = Dispatchers.Main,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {

    private val disposables = Job()

    operator fun invoke(year: Int, month: Int, date: Int, background: Boolean = false) {
        CoroutineScope(mainDispatcher).launch(disposables) {
            val article = withContext(ioDispatcher) {
                repository.findFirst(TitleFilterGenerator()(year, month + 1, date))
            } ?: return@launch

            if (background) {
                viewModel?.newArticleOnBackground(article.title)
            } else {
                viewModel?.newArticle(article.title)
            }
        }
    }

    fun dispose() {
        disposables.cancel()
    }
}