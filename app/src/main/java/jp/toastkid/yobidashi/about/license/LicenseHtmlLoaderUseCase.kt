/*
 * Copyright (c) 2021 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.about.license

import android.widget.FrameLayout
import androidx.core.view.get
import androidx.core.view.isGone
import androidx.core.view.isVisible
import jp.toastkid.yobidashi.browser.webview.factory.WebViewFactory
import java.nio.charset.StandardCharsets

/**
 * @author toastkidjp
 */
internal class LicenseHtmlLoaderUseCase(
    private val webViewFactory: WebViewFactory = WebViewFactory()
) {

    operator fun invoke(container: FrameLayout) {
        container.isVisible = !container.isVisible
        if (container.isGone || container.childCount != 0) {
            container[0].scrollTo(0, 0)
            return
        }

        val content = LicenseContentLoaderUseCase(container.context.assets).invoke()

        val webView = webViewFactory.make(container.context)
        container.addView(webView)
        webView.loadDataWithBaseURL(
            null,
            content,
            MIMETYPE,
            encoding,
            null
        )
    }

    companion object {

        private const val MIMETYPE = "text/html"

        private val encoding = StandardCharsets.UTF_8.name()

    }

}