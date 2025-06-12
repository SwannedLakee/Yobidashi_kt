/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.image.list

import android.provider.MediaStore
import jp.toastkid.image.R

/**
 * @author toastkidjp
 */
enum class Sort(
    val bucketSort: String,
    val imageSort: String,
    val titleId: Int
) {
    DATE(
        "${MediaStore.Images.Media.DATE_MODIFIED} DESC",
        "${MediaStore.Images.Media.DATE_MODIFIED} DESC",
        R.string.title_sort_by_date
    ),
    DATE_ASC(
        "${MediaStore.Images.Media.DATE_MODIFIED} ASC",
        "${MediaStore.Images.Media.DATE_MODIFIED} ASC",
        R.string.title_sort_by_date_ask
    ),
    NAME(
        "${MediaStore.Images.Media.BUCKET_DISPLAY_NAME} ASC",
        "${MediaStore.Images.Media.DISPLAY_NAME} ASC",
        R.string.title_sort_by_name
    ),
    ITEM_COUNT(
        "${MediaStore.Images.Media.DATE_MODIFIED} DESC",
        "${MediaStore.Images.Media.DATE_MODIFIED} DESC",
        R.string.title_sort_by_count
    );

    companion object {

        fun default() = DATE

        fun findByName(name: String?) =
                if (name.isNullOrBlank()) default()
                else entries.firstOrNull { it.name == name } ?: default()
    }
}