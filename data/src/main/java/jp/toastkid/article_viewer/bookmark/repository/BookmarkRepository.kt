/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.article_viewer.bookmark.repository

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import jp.toastkid.article_viewer.article.Article
import jp.toastkid.article_viewer.bookmark.Bookmark

/**
 * @author toastkidjp
 */
@Dao
interface BookmarkRepository {

    @Query("SELECT id FROM bookmark")
    fun allArticleIds(): List<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun add(articleId: Bookmark)

    @Query("SELECT * FROM bookmark WHERE id = :articleId")
    fun findArticleById(articleId: Int): Article

    @Query("SELECT COUNT(id) FROM bookmark")
    fun count(): Int

    @Query("DELETE FROM bookmark WHERE id = :articleId")
    fun delete(articleId: Int)

}