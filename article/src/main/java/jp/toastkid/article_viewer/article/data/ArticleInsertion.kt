/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.article_viewer.article.data

import android.content.Context
import jp.toastkid.article_viewer.article.Article
import jp.toastkid.article_viewer.tokenizer.NgramTokenizer
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * @author toastkidjp
 */
class ArticleInsertion(context: Context, private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO) {

    private val repository = AppDatabase.find(context).articleRepository()

    private val tokenizer = NgramTokenizer()

    operator fun invoke(title: String?, content: String?) {
        if (title.isNullOrBlank() || content.isNullOrBlank()) {
            return
        }

        val article = makeArticle(title, content) ?: return

        CoroutineScope(ioDispatcher).launch {
            repository.insert(article)
        }
    }

    private fun makeArticle(title: String, content: String): Article? {
        val article = Article(DEFAULT_ID)
        article.title = title
        article.contentText = content
        article.bigram = tokenizer.invoke(content, BI_GRAM) ?: return null
        article.length = content.length
        article.lastModified = System.currentTimeMillis()
        return article
    }

    companion object {

        private const val DEFAULT_ID = 0

        private const val BI_GRAM = 2

    }
}