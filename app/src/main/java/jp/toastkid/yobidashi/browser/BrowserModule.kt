package jp.toastkid.yobidashi.browser

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.webkit.ValueCallback
import android.webkit.WebSettings
import android.webkit.WebView
import android.widget.FrameLayout
import androidx.core.view.get
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import jp.toastkid.lib.AppBarViewModel
import jp.toastkid.lib.ContentViewModel
import jp.toastkid.lib.Urls
import jp.toastkid.lib.preference.PreferenceApplier
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.browser.archive.Archive
import jp.toastkid.yobidashi.browser.archive.IdGenerator
import jp.toastkid.yobidashi.browser.archive.auto.AutoArchive
import jp.toastkid.yobidashi.browser.block.AdRemover
import jp.toastkid.yobidashi.browser.download.image.AllImageDownloaderService
import jp.toastkid.yobidashi.browser.page_information.PageInformationExtractor
import jp.toastkid.yobidashi.browser.reader.ReaderModeUseCase
import jp.toastkid.yobidashi.browser.webview.AlphaConverter
import jp.toastkid.yobidashi.browser.webview.CustomViewSwitcher
import jp.toastkid.yobidashi.browser.webview.CustomWebView
import jp.toastkid.yobidashi.browser.webview.DarkModeApplier
import jp.toastkid.yobidashi.browser.webview.GlobalWebViewPool
import jp.toastkid.yobidashi.browser.webview.WebSettingApplier
import jp.toastkid.yobidashi.browser.webview.WebViewFactoryUseCase
import jp.toastkid.yobidashi.browser.webview.WebViewStateUseCase
import jp.toastkid.yobidashi.browser.webview.factory.WebChromeClientFactory
import jp.toastkid.yobidashi.browser.webview.factory.WebViewClientFactory
import jp.toastkid.yobidashi.libs.Toaster
import jp.toastkid.yobidashi.libs.network.DownloadAction
import jp.toastkid.yobidashi.libs.network.NetworkChecker
import jp.toastkid.yobidashi.libs.network.WifiConnectionChecker
import jp.toastkid.yobidashi.main.MainActivity
import jp.toastkid.yobidashi.rss.suggestion.RssAddingSuggestion

/**
 * @author toastkidjp
 */
class BrowserModule(
        private val context: Context,
        private val webViewContainer: FrameLayout?
) {

    private val preferenceApplier = PreferenceApplier(context)

    private val rssAddingSuggestion = RssAddingSuggestion(preferenceApplier)

    private val faviconApplier: FaviconApplier = FaviconApplier(context)

    private val readerModeUseCase by lazy { ReaderModeUseCase() }

    private val htmlSourceExtractionUseCase by lazy { HtmlSourceExtractionUseCase() }

    private var customViewSwitcher: CustomViewSwitcher? = null

    private val adRemover: AdRemover = AdRemover.make(context.assets)

    private val autoArchive = AutoArchive.make(context)

    private var browserHeaderViewModel: BrowserHeaderViewModel? = null

    private var loadingViewModel: LoadingViewModel? = null

    /**
     * Animation of slide up bottom.
     */
    private val slideUpFromBottom
            = AnimationUtils.loadAnimation(context, R.anim.slide_up)

    private val slideDown
            = AnimationUtils.loadAnimation(context, R.anim.slide_down)

    private val idGenerator = IdGenerator()

    private var lastId = ""

    private var contentViewModel: ContentViewModel? = null

    private val webViewFactory: WebViewFactoryUseCase

    private val darkThemeApplier = DarkModeApplier()

    private val alphaConverter = AlphaConverter()

    private val webViewStateUseCase = WebViewStateUseCase.make(context)

    init {
        GlobalWebViewPool.resize(preferenceApplier.poolSize)

        customViewSwitcher = CustomViewSwitcher({ context }, { currentView() })

        if (context is MainActivity) {
            val viewModelProvider = ViewModelProvider(context)
            browserHeaderViewModel = viewModelProvider.get(BrowserHeaderViewModel::class.java)
            loadingViewModel = viewModelProvider.get(LoadingViewModel::class.java)
            contentViewModel = viewModelProvider.get(ContentViewModel::class.java)
        }

        webViewFactory = WebViewFactoryUseCase(
                webViewClientFactory = WebViewClientFactory(
                        contentViewModel,
                        adRemover,
                        faviconApplier,
                        preferenceApplier,
                        browserHeaderViewModel,
                        rssAddingSuggestion,
                        loadingViewModel,
                        { currentView() },
                        { lastId }
                ),
                webChromeClientFactory = WebChromeClientFactory(
                        browserHeaderViewModel,
                        faviconApplier,
                        customViewSwitcher
                )
        )
    }

    fun loadWithNewTab(uri: Uri, tabId: String) {
        lastId = tabId
        if (replaceWebView(tabId)) {
            loadUrl(uri.toString())
        }
    }

    private fun loadUrl(url: String) {
        if (url.isEmpty()) {
            return
        }

        val context: Context = context

        val currentView = currentView() ?: return

        if (currentView.url.isNullOrEmpty()
                && Urls.isValidUrl(url)
                && NetworkChecker.isNotAvailable(context)
        ) {
            autoArchive.load(currentView, idGenerator.from(url)) {
                contentViewModel?.snackShort("Load archive.")
            }
            return
        }

        if (preferenceApplier.wifiOnly && WifiConnectionChecker.isNotConnecting(context)) {
            Toaster.tShort(context, R.string.message_wifi_not_connecting)
            return
        }

        currentView.loadUrl(url)
    }

    private fun replaceWebView(tabId: String): Boolean {
        browserHeaderViewModel?.resetContent()

        val currentWebView = getWebView(tabId)
        if (webViewContainer?.childCount != 0) {
            val previousView = webViewContainer?.get(0)
            if (currentWebView == previousView) {
                return false
            }
        }

        setWebView(currentWebView)
        return currentWebView?.url.isNullOrBlank()
    }

    private fun setWebView(webView: WebView?) {
        webViewContainer?.removeAllViews()
        webView?.let {
            it.onResume()
            (it.parent as? ViewGroup)?.removeAllViews()
            darkThemeApplier(it, preferenceApplier.useDarkMode())
            webViewContainer?.addView(it)
            updateBackButtonState(it.canGoBack())
            updateForwardButtonState(it.canGoForward())
            browserHeaderViewModel?.nextTitle(it.title)
            browserHeaderViewModel?.nextUrl(it.url)

            webViewContainer?.startAnimation(slideUpFromBottom)

            val activity = webViewContainer?.context
            if (activity is FragmentActivity
                    && ScreenMode.find(preferenceApplier.browserScreenMode()) != ScreenMode.FULL_SCREEN) {
                ViewModelProvider(activity).get(AppBarViewModel::class.java).show()
            }
        }

        reloadWebViewSettings()
    }

    private fun updateBackButtonState(newState: Boolean) {
        browserHeaderViewModel?.setBackButtonEnability(newState)
    }

    private fun updateForwardButtonState(newState: Boolean) {
        browserHeaderViewModel?.setForwardButtonEnability(newState)
    }

    /**
     * Simple delegation to [WebView].
     */
    fun reload() {
        if (preferenceApplier.wifiOnly && WifiConnectionChecker.isNotConnecting(context)) {
            Toaster.tShort(context, R.string.message_wifi_not_connecting)
            return
        }
        currentView()?.reload()
    }

    fun find(keyword: String?) {
        keyword ?: return
        currentView()?.findAllAsync(keyword)
    }

    fun findUp() {
        currentView()?.findNext(false)
    }

    fun findDown() {
        currentView()?.findNext(true)
    }

    fun pageUp() {
        currentView()?.pageUp(true)
    }

    fun pageDown() {
        currentView()?.pageDown(true)
    }

    fun back() = currentView()?.let {
        return if (it.canGoBack()) {
            it.goBack()
            updateBackButtonState(it.canGoBack())
            updateForwardButtonState(it.canGoForward())
            true
        } else false
    } ?: false

    fun forward() = currentView()?.let {
        if (it.canGoForward()) {
            it.goForward()
        }

        updateBackButtonState(it.canGoBack())
        updateForwardButtonState(it.canGoForward())
    }

    /**
     * Save archive file.
     */
    fun saveArchive() {
        val currentView = currentView() ?: return
        Archive.save(currentView)
    }

    /**
     * Save archive file.
     */
    fun saveArchiveForAutoArchive() {
        val webView = currentView()
        autoArchive.save(webView, idGenerator.from(webView?.url))
    }

    /**
     * Reload [WebSettings].
     */
    private fun reloadWebViewSettings() {
        WebSettingApplier(preferenceApplier).invoke(currentView()?.settings)
    }

    fun resetUserAgent(userAgentText: String) {
        currentView()?.settings?.userAgentString = userAgentText
        reload()
    }

    /**
     * Is disable Pull-to-Refresh?
     *
     * @return is disable Pull-to-Refresh
     */
    fun disablePullToRefresh(): Boolean =
            (currentView() as? CustomWebView)?.let { !it.enablePullToRefresh || it.scrollY != 0 } ?: false

    /**
     * Stop loading in current tab.
     */
    fun stopLoading() {
        currentView()?.stopLoading()
    }

    fun onResume() {
        GlobalWebViewPool.onResume()
    }

    fun onPause() {
        GlobalWebViewPool.storeStates(context)
        GlobalWebViewPool.onPause()
    }

    fun onDestroy() {
        webViewContainer?.removeAllViews()
    }

    /**
     * Return current [WebView].
     *
     * @return [WebView]
     */
    private fun currentView(): WebView? {
        return GlobalWebViewPool.getLatest()
    }

    /**
     * Return current [WebView]'s URL.
     *
     * @return URL string (Nullable)
     */
    fun currentUrl(): String? = currentView()?.url

    /**
     * Return current [WebView]'s title.
     *
     * @return title (NonNull)
     */
    fun currentTitle(): String = currentView()?.title ?: ""

    /**
     * Get [WebView] with tab ID.
     *
     * @param tabId Tab's ID.
     * @return [WebView]
     */
    private fun getWebView(tabId: String?): WebView? {
        if (!GlobalWebViewPool.containsKey(tabId) && tabId != null) {
            GlobalWebViewPool.put(tabId, makeWebView())
        }
        val webView = GlobalWebViewPool.get(tabId)
        webViewStateUseCase.restore(webView, tabId)
        return webView
    }

    private fun makeWebView(): WebView {
        return webViewFactory(context)
    }

    fun onSaveInstanceState(outState: Bundle) {
        currentView()?.saveState(outState)
    }

    fun makeCurrentPageInformation(): Bundle = PageInformationExtractor().invoke(currentView())

    /**
     * Resize [GlobalWebViewPool].
     *
     * @param poolSize
     */
    fun resizePool(poolSize: Int) {
        GlobalWebViewPool.resize(poolSize)
    }

    fun applyNewAlpha() {
        GlobalWebViewPool.applyNewAlpha(alphaConverter.readBackground(context))
    }

    fun makeShareMessage() = "${currentTitle()}${System.lineSeparator()}${currentUrl()}"

    fun invokeContentExtraction(callback: ValueCallback<String>) {
        readerModeUseCase(currentView(), callback)
    }

    fun invokeHtmlSourceExtraction(callback: ValueCallback<String>) {
        htmlSourceExtractionUseCase(currentView(), callback)
    }

    fun downloadAllImages() {
        AllImageDownloaderService(DownloadAction(context)).invoke(currentView())
    }

}