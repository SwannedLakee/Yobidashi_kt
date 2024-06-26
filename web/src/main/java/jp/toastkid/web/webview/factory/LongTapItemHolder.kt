/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.web.webview.factory

import android.os.Bundle

/**
 * @author toastkidjp
 */
data class LongTapItemHolder(var title: String = "", var anchor: String = "") {

    fun reset() {
        title = ""
        anchor = ""
    }

    fun extract(bundle: Bundle) {
        title = bundle.getString(KEY_TITLE)?.trim() ?: ""
        anchor = bundle.getString(KEY_URL) ?: ""
    }

    companion object {

        private const val KEY_TITLE = "title"

        private const val KEY_URL = "url"

    }

}