/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.image.list

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import jp.toastkid.image.Image

/**
 * @author toastkidjp
 */
class ImageViewerFragmentViewModel : ViewModel() {

    private val _onClick = MutableLiveData<String>()

    val onClick: LiveData<String> = _onClick

    fun click(name: String) {
        _onClick.postValue(name)
    }

    private val _onLongClick = MutableLiveData<String>()

    val onLongClick: LiveData<String> = _onLongClick

    fun longClick(name: String) {
        _onLongClick.postValue(name)
    }

    private val _refresh = MutableLiveData<Unit>()

    val refresh: LiveData<Unit> = _refresh

    fun refresh() {
        _refresh.postValue(Unit)
    }

    private val _images = MutableLiveData<List<Image>>()

    val images: LiveData<List<Image>> = _images

    fun submitImages(images: List<Image>) {
        _images.postValue(images)
    }

}