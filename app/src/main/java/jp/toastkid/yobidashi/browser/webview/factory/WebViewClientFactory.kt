/*
 * Copyright (c) 2020 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.yobidashi.browser.webview.factory

import android.annotation.TargetApi
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.net.http.SslError
import android.os.Build
import android.webkit.SslErrorHandler
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.fragment.app.FragmentActivity
import jp.toastkid.lib.ContentViewModel
import jp.toastkid.lib.preference.PreferenceApplier
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.browser.BrowserHeaderViewModel
import jp.toastkid.yobidashi.browser.FaviconApplier
import jp.toastkid.yobidashi.browser.LoadingViewModel
import jp.toastkid.yobidashi.browser.block.AdRemover
import jp.toastkid.yobidashi.browser.history.ViewHistoryInsertion
import jp.toastkid.yobidashi.browser.tls.TlsErrorDialogFragment
import jp.toastkid.yobidashi.browser.tls.TlsErrorMessageGenerator
import jp.toastkid.yobidashi.browser.webview.GlobalWebViewPool
import jp.toastkid.yobidashi.libs.intent.IntentFactory
import jp.toastkid.yobidashi.rss.suggestion.RssAddingSuggestion
import jp.toastkid.yobidashi.tab.History
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber

class WebViewClientFactory(
    private val contentViewModel: ContentViewModel?,
    private val adRemover: AdRemover,
    private val faviconApplier: FaviconApplier,
    private val preferenceApplier: PreferenceApplier,
    private val browserHeaderViewModel: BrowserHeaderViewModel? = null,
    private val rssAddingSuggestion: RssAddingSuggestion? = null,
    private val loadingViewModel: LoadingViewModel? = null,
    private val currentView: () -> WebView? = { null }
) {

    /**
     * Add onPageFinished and onPageStarted.
     */
    operator fun invoke(): WebViewClient = object : WebViewClient() {

        override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
            super.onPageStarted(view, url, favicon)

            browserHeaderViewModel?.updateProgress(0)
            browserHeaderViewModel?.nextUrl(url)

            rssAddingSuggestion?.invoke(view, url)
            browserHeaderViewModel?.setBackButtonEnability(view.canGoBack())
        }

        override fun onPageFinished(view: WebView, url: String?) {
            super.onPageFinished(view, url)

            val title = view.title ?: ""
            val urlStr = url ?: ""

            val tabId = GlobalWebViewPool.getTabId(view)
            if (tabId?.isNotBlank() == true) {
                CoroutineScope(Dispatchers.Main).launch {
                    loadingViewModel?.finished(tabId, History.make(title, urlStr))
                }
            }

            browserHeaderViewModel?.updateProgress(100)
            browserHeaderViewModel?.stopProgress(true)

            try {
                if (view == currentView()) {
                    browserHeaderViewModel?.nextTitle(title)
                    browserHeaderViewModel?.nextUrl(urlStr)
                }
            } catch (e: Exception) {
                Timber.e(e)
            }

            if (preferenceApplier.saveViewHistory
                    && title.isNotEmpty()
                    && urlStr.isNotEmpty()
            ) {
                ViewHistoryInsertion
                        .make(
                                view.context,
                                title,
                                urlStr,
                                faviconApplier.makePath(urlStr)
                        )
                        .invoke()
            }
        }

        override fun onReceivedError(
                view: WebView, request: WebResourceRequest, error: WebResourceError) {
            super.onReceivedError(view, request, error)

            browserHeaderViewModel?.updateProgress(100)
            browserHeaderViewModel?.stopProgress(true)
        }

        override fun onReceivedSslError(view: WebView?, handler: SslErrorHandler?, error: SslError?) {
            super.onReceivedSslError(view, handler, error)

            handler?.cancel()

            val context = view?.context ?: return
            if (context !is FragmentActivity || context.isFinishing) {
                return
            }

            TlsErrorDialogFragment
                    .make(TlsErrorMessageGenerator().invoke(context, error))
                    .show(
                            context.supportFragmentManager,
                            TlsErrorDialogFragment::class.java.simpleName
                    )
        }

        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
        override fun shouldInterceptRequest(view: WebView, request: WebResourceRequest): WebResourceResponse? =
                if (preferenceApplier.adRemove) {
                    adRemover(request.url.toString())
                } else {
                    super.shouldInterceptRequest(view, request)
                }

        @Suppress("OverridingDeprecatedMember")
        @TargetApi(Build.VERSION_CODES.KITKAT_WATCH)
        override fun shouldInterceptRequest(view: WebView, url: String): WebResourceResponse? =
                if (preferenceApplier.adRemove) {
                    adRemover(url)
                } else {
                    @Suppress("DEPRECATION")
                    super.shouldInterceptRequest(view, url)
                }

        @Suppress("DEPRECATION")
        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
        override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean =
                shouldOverrideUrlLoading(view, request?.url?.toString())

        @Suppress("OverridingDeprecatedMember", "DEPRECATION")
        override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean =
                url?.let {
                    val context: Context? = view?.context
                    val uri: Uri = Uri.parse(url)
                    when (uri.scheme) {
                        "market", "intent" -> {
                            try {
                                context?.startActivity(Intent.parseUri(url, Intent.URI_INTENT_SCHEME))
                                true
                            } catch (e: ActivityNotFoundException) {
                                Timber.w(e)

                                context?.let {
                                    contentViewModel?.snackShort(context.getString(R.string.message_cannot_launch_app))
                                }
                                true
                            }
                        }
                        "tel" -> {
                            context?.startActivity(IntentFactory.dial(uri))
                            view?.reload()
                            true
                        }
                        "mailto" -> {
                            context?.startActivity(IntentFactory.mailTo(uri))
                            view?.reload()
                            true
                        }
                        else -> {
                            super.shouldOverrideUrlLoading(view, url)
                        }
                    }
                } ?: super.shouldOverrideUrlLoading(view, url)
    }
}