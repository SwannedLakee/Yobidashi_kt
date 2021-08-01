/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.article_viewer.article.list.menu

import jp.toastkid.article_viewer.bookmark.repository.BookmarkRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * @author toastkidjp
 */
class BookmarkListMenuPopupActionUseCase(
    private val bookmarkRepository: BookmarkRepository,
    private val deleted: () -> Unit,
    private val mainDispatcher: CoroutineDispatcher = Dispatchers.Main,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : MenuPopupActionUseCase {

    override fun addToBookmark(id: Int) = Unit

    override fun delete(id: Int) {
        CoroutineScope(mainDispatcher).launch {
            withContext(ioDispatcher) {
                bookmarkRepository.delete(id)
            }
            deleted()
        }
    }

}