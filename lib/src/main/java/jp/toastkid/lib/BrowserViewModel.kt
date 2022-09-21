/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.lib

import android.net.Uri
import android.os.Message
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.input.nestedscroll.NestedScrollDispatcher
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import jp.toastkid.lib.lifecycle.Event
import jp.toastkid.lib.model.LoadInformation
import jp.toastkid.lib.view.swiperefresh.SwipeRefreshState

/**
 * @author toastkidjp
 */
class BrowserViewModel : ViewModel() {

    private val _preview =  MutableLiveData<Event<Uri>>()

    val preview: LiveData<Event<Uri>> = _preview

    fun preview(uri: Uri) {
        _preview.postValue(Event(uri))
    }

    private val _open = MutableLiveData<Event<Uri>>()

    val open: LiveData<Event<Uri>> = _open

    fun open(uri: Uri) {
        _open.postValue(Event(uri))
    }

    private val _openBackground = MutableLiveData<Event<Uri>>()

    val openBackground: LiveData<Event<Uri>> = _openBackground

    fun openBackground(uri: Uri) {
        _openBackground.postValue(Event(uri))
    }

    // TODO: Use appropriate data class.
    private val _openBackgroundWithTitle = MutableLiveData<Event<Pair<String, Uri>>>()

    val openBackgroundWithTitle: LiveData<Event<Pair<String, Uri>>> = _openBackgroundWithTitle

    fun openBackground(title: String, uri: Uri) {
        _openBackgroundWithTitle.postValue(Event(title to uri))
    }

    private val _openNewWindow = MutableLiveData<Event<Message?>>()

    val openNewWindow: LiveData<Event<Message?>> = _openNewWindow

    fun openNewWindow(resultMessage: Message?) {
        _openNewWindow.postValue(Event(resultMessage))
    }

    private val _download = MutableLiveData<Event<String>>()

    val download: LiveData<Event<String>> = _download

    fun download(url: String) {
        _download.postValue(Event(url))
    }

    private val _error = mutableStateOf("")
    val openErrorDialog = mutableStateOf(false)

    val error: State<String> = _error

    fun setError(text: String) {
        _error.value = text
        openErrorDialog.value = true
    }

    fun clearError() {
        _error.value = ""
        openErrorDialog.value = false
    }

    private val _longTapActionParameters =
        mutableStateOf(Triple<String?, String?, String?>(null, null, null))
    val openLongTapDialog = mutableStateOf(false)

    val longTapActionParameters: State<Triple<String?, String?, String?>> = _longTapActionParameters

    fun setLongTapParameters(title: String?, anchor: String?, imageUrl: String?) {
        _longTapActionParameters.value = Triple(title, anchor, imageUrl)
        openLongTapDialog.value = true
    }

    fun clearLongTapParameters() {
        _longTapActionParameters.value = Triple(null, null, null)
        openLongTapDialog.value = false
    }

    private val _switchWebViewToCurrent = MutableLiveData<Event<String>>()

    val switchWebViewToCurrent: LiveData<Event<String>> = _switchWebViewToCurrent

    fun switchWebViewToCurrent(tabId: String) {
        _switchWebViewToCurrent.postValue(Event(tabId))
    }

    //TODO WIP

    private val _title = mutableStateOf("")
    val title: State<String> = _title

    fun nextTitle(nextTitle: String?) {
        if (nextTitle.isNullOrBlank()) {
            return
        }
        _title.value = nextTitle
    }

    private val _url = mutableStateOf("")
    val url: State<String> = _url

    fun nextUrl(nextUrl: String?) {
        if (nextUrl.isNullOrBlank()) {
            return
        }
        _url.value = nextUrl
    }

    private val _enableForward = mutableStateOf(false)

    val enableForward: State<Boolean> = _enableForward

    fun setForwardButtonIsEnabled(newState: Boolean) {
        _enableForward.value = newState
    }

    private val _enableBack = mutableStateOf(false)

    val enableBack: State<Boolean> = _enableBack

    fun setBackButtonIsEnabled(newState: Boolean) {
        _enableBack.value = newState
    }

    private val _progress = mutableStateOf(0)

    val progress: State<Int> = _progress

    fun updateProgress(newProgress: Int) {
        _progress.value = newProgress
    }

    private val _stopProgress = MutableLiveData(Event(false))

    val stopProgress: LiveData<Event<Boolean>> = _stopProgress

    fun stopProgress(stop: Boolean) {
        _stopProgress.postValue(Event(stop))
    }

    private val _onPageFinished =
        MutableLiveData<LoadInformation>()

    val onPageFinished: LiveData<LoadInformation> = _onPageFinished

    fun finished(tabId: String, title: String, url: String) =
        _onPageFinished.postValue(LoadInformation(tabId, title, url))

    private val _search = MutableLiveData<Event<String>>()

    val search: LiveData<Event<String>> = _search

    fun search(query: String) {
        _search.postValue(Event(query))
    }

    private val nestedScrollDispatcher = NestedScrollDispatcher()

    fun nestedScrollDispatcher() = nestedScrollDispatcher

    val swipeRefreshState = mutableStateOf<SwipeRefreshState?>(null)

    fun initializeSwipeRefreshState(refreshTriggerPx: Float) {
        swipeRefreshState.value = SwipeRefreshState(false, refreshTriggerPx)
    }

    fun showSwipeRefreshIndicator() =
        swipeRefreshState.value?.isSwipeInProgress == true
                || swipeRefreshState.value?.isRefreshing == true

    fun calculateSwipingProgress(refreshTriggerPx: Float) =
        if (swipeRefreshState.value?.isRefreshing == false) ((swipeRefreshState.value?.indicatorOffset ?: 0f) / refreshTriggerPx).coerceIn(0f, 1f) else progress.value.toFloat() / 100f

}