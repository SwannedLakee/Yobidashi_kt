/*
 * Copyright (c) 2021 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.web.usecase

import android.webkit.WebView
import android.widget.FrameLayout
import androidx.activity.ComponentActivity
import androidx.collection.LruCache
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.mockkObject
import io.mockk.unmockkAll
import io.mockk.verify
import jp.toastkid.lib.BrowserViewModel
import jp.toastkid.lib.preference.PreferenceApplier
import jp.toastkid.web.ScreenMode
import jp.toastkid.web.webview.DarkModeApplier
import jp.toastkid.web.webview.GlobalWebViewPool
import jp.toastkid.web.webview.WebSettingApplier
import jp.toastkid.web.webview.WebViewPool
import jp.toastkid.web.webview.WebViewStateUseCase
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * @author toastkidjp
 */
class WebViewReplacementUseCaseTest {

    @InjectMockKs
    private lateinit var webViewReplacementUseCase: WebViewReplacementUseCase

    @MockK
    private lateinit var webViewContainer: FrameLayout

    @MockK
    private lateinit var webViewStateUseCase: WebViewStateUseCase

    @MockK
    private lateinit var makeWebView: () -> WebView

    @MockK
    private lateinit var browserViewModel: BrowserViewModel

    @MockK
    private lateinit var preferenceApplier: PreferenceApplier

    @MockK
    private lateinit var darkThemeApplier: DarkModeApplier

    @MockK
    private lateinit var webView: WebView

    @MockK
    private lateinit var context: ComponentActivity

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        every { webViewContainer.getChildCount() }.returns(0)
        every { webViewContainer.getChildAt(any()) }.returns(webView)
        every { webViewContainer.addView(any()) }.returns(Unit)
        every { webViewContainer.removeAllViews() }.returns(Unit)
        every { webViewContainer.startAnimation(any()) }.returns(Unit)
        every { webViewContainer.getContext() }.returns(context)

        every { webView.onResume() }.returns(Unit)
        every { webView.getParent() }.returns(webViewContainer)
        every { webView.getSettings() }.returns(mockk())
        every { webView.canGoBack() }.returns(false)
        every { webView.canGoForward() }.returns(false)
        every { webView.getTitle() }.returns("test title")
        every { webView.getUrl() }.returns("https://www.yahoo.co.jp")
        every { darkThemeApplier.invoke(any(), any()) }.returns(Unit)
        every { preferenceApplier.useDarkMode() }.returns(true)
        every { preferenceApplier.browserScreenMode() }.returns("fixed")

        every { browserViewModel.setBackButtonIsEnabled(any()) }.returns(Unit)
        every { browserViewModel.setForwardButtonIsEnabled(any()) }.returns(Unit)
        every { browserViewModel.nextTitle(any()) }.returns(Unit)
        every { browserViewModel.nextUrl(any()) }.returns(Unit)
        every { makeWebView.invoke() }.returns(webView)
        every { webViewStateUseCase.restore(any(), any()) }.returns(Unit)

        mockkObject(ScreenMode)
        every { ScreenMode.find(any()) }.returns(ScreenMode.FULL_SCREEN) // TODO Other case

        mockkConstructor(WebViewPool::class)
        mockkConstructor(LruCache::class)

        mockkObject(GlobalWebViewPool)
        every { GlobalWebViewPool.containsKey(any()) }.returns(false)
        every { GlobalWebViewPool.put(any(), any()) }.returns(Unit)
        every { GlobalWebViewPool.get(any()) }.returns(webView)
        every { GlobalWebViewPool.getLatest() }.returns(webView)

        mockkConstructor(WebSettingApplier::class)
        every { anyConstructed<WebSettingApplier>().invoke(any()) }.returns(Unit)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun testInvoke() {
        webViewReplacementUseCase.invoke("test-id")

        verify(exactly = 1) { webViewContainer.getChildCount() }
        verify(exactly = 0) { webViewContainer.getChildAt(any()) }
        verify(exactly = 1) { webViewContainer.getContext() }
        verify(atLeast = 1) { webView.onResume() }
        verify(atLeast = 1) { webView.getParent() }
        verify(atLeast = 1) { webView.getSettings() }
        verify(atLeast = 1) { webView.canGoBack() }
        verify(atLeast = 1) { webView.canGoForward() }
        verify(atLeast = 1) { webView.getTitle() }
        verify(atLeast = 1) { webView.getUrl() }
        verify(exactly = 1) { darkThemeApplier.invoke(any(), any()) }
        verify(exactly = 1) { preferenceApplier.useDarkMode() }
        verify(exactly = 1) { preferenceApplier.browserScreenMode() }
        verify(exactly = 1) { browserViewModel.setBackButtonIsEnabled(any()) }
        verify(exactly = 1) { browserViewModel.setForwardButtonIsEnabled(any()) }
        verify(exactly = 1) { browserViewModel.nextTitle(any()) }
        verify(exactly = 1) { browserViewModel.nextUrl(any()) }
        verify(exactly = 1) { makeWebView.invoke() }
        verify(exactly = 1) { webViewStateUseCase.restore(any(), any()) }
        verify(exactly = 1) { GlobalWebViewPool.containsKey(any()) }
        verify(exactly = 1) { GlobalWebViewPool.put(any(), any()) }
        verify(exactly = 1) { GlobalWebViewPool.get(any()) }
        verify(exactly = 1) { GlobalWebViewPool.getLatest() }
        verify(exactly = 1) { anyConstructed<WebSettingApplier>().invoke(any()) }
    }

    @Test
    fun testChildCountIsNotZero() {
        every { webViewContainer.getChildCount() }.returns(1)

        webViewReplacementUseCase.invoke("test-id")

        verify(exactly = 1) { webViewContainer.getChildCount() }
        verify(exactly = 1) { webViewContainer.getChildAt(any()) }
        verify(exactly = 1) { GlobalWebViewPool.containsKey("test-id") }
        verify(exactly = 1) { GlobalWebViewPool.put("test-id", any()) }
    }

    @Test
    fun testContainsTabId() {
        mockkObject(GlobalWebViewPool)
        every { GlobalWebViewPool.containsKey(any()) }.returns(true)

        webViewReplacementUseCase.invoke("test-id")

        verify(exactly = 1) { GlobalWebViewPool.containsKey("test-id") }
        verify(exactly = 0) { GlobalWebViewPool.put(any(), any()) }
    }

}