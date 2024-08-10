/*
 * Copyright (c) 2021 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.api.html

import android.webkit.URLUtil
import jp.toastkid.api.lib.HttpClientFactory
import okhttp3.Request
import okhttp3.Response

/**
 * @author toastkidjp
 */
class HtmlApi {

    private val httpClient = HttpClientFactory().withTimeout(3)

    /**
     * This return value([Response]) must be closed.
     * <pre>
     *     htmlApi.invoke("https://www.yahoo.com").use { /* Something of usage. */ }
     * </pre>
     */
    operator fun invoke(url: String?): Response? {
        if (url.isNullOrBlank() || (!URLUtil.isNetworkUrl(url))) {
            return null
        }

        return httpClient
                .newCall(makeRequest(url))
                .execute()
    }

    private fun makeRequest(url: String): Request =
            Request.Builder()
                    .url(url)
                    .header(HEADER_NAME_USER_AGENT, HEADER_VALUE_USER_AGENT)
                    .build()

    companion object {

        private const val HEADER_NAME_USER_AGENT = "User-Agent"

        private const val HEADER_VALUE_USER_AGENT =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64)" +
                    " AppleWebKit/537.36 (KHTML, like Gecko)" +
                    " Chrome/51.0.2704.79 Safari/537.36 Edge/14.14393"
    }
}