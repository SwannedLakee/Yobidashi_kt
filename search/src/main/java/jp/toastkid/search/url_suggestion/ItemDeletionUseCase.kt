/*
 * Copyright (c) 2021 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.search.url_suggestion

import androidx.annotation.WorkerThread
import jp.toastkid.yobidashi.browser.UrlItem
import jp.toastkid.yobidashi.browser.bookmark.model.Bookmark
import jp.toastkid.yobidashi.browser.bookmark.model.BookmarkRepository
import jp.toastkid.yobidashi.browser.history.ViewHistory
import jp.toastkid.yobidashi.browser.history.ViewHistoryRepository

/**
 * @author toastkidjp
 */
class ItemDeletionUseCase(
    private val bookmarkRepository: BookmarkRepository,
    private val viewHistoryRepository: ViewHistoryRepository
) {

    @WorkerThread
    operator fun invoke(item: UrlItem) {
        when (item) {
            is Bookmark -> bookmarkRepository.delete(item)
            is ViewHistory -> viewHistoryRepository.delete(item)
            else -> Unit
        }
    }

}