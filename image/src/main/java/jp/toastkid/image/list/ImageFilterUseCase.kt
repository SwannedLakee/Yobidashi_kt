/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.image.list

import jp.toastkid.image.Image

/**
 * @author toastkidjp
 */
internal class ImageFilterUseCase(
    private val submitImages: (List<Image>) -> Unit,
    private val imageLoaderUseCase: ImageLoaderUseCase,
    private val imageLoader: ImageLoader
) {

    operator fun invoke(keyword: String?) {
        if (keyword.isNullOrBlank()) {
            imageLoaderUseCase()
            return
        }

        val newList = imageLoader.filterBy(keyword)
        submitImages(newList)

        imageLoaderUseCase.clearCurrentBucket()
    }

}