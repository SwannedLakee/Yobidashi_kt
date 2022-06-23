/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.browser.floating

import android.graphics.Bitmap
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

/**
 * @author toastkidjp
 */
class FloatingPreviewViewModel : ViewModel() {

    private val _title = MutableLiveData<String>()

    val title: LiveData<String> = _title

    fun newTitle(title: String?) {
        title?.let { _title.postValue(it) }
    }

    private val _icon = mutableStateOf<Bitmap?>(null)

    val icon: State<Bitmap?> = _icon

    fun newIcon(bitmap: Bitmap?) {
        _icon.value = bitmap
    }

    private val _url = MutableLiveData<String>()

    val url: LiveData<String> = _url

    fun newUrl(url: String?) {
        url?.let { _url.postValue(it) }
    }

    private val _progress = MutableLiveData<Int>()

    val progress: LiveData<Int> = _progress

    fun newProgress(progress: Int) {
        _progress.postValue(progress)
    }
}