package jp.toastkid.yobidashi.browser.download.image

import android.webkit.WebView
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import jp.toastkid.lib.network.DownloadAction
import org.junit.Before
import org.junit.Test

/**
 * @author toastkidjp
 */
class AllImageDownloaderUseCaseTest {

    @MockK
    private lateinit var downloadAction: DownloadAction

    @MockK
    private lateinit var webView: WebView

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
    }

    @Test
    fun testWebViewIsNull() {
        every { downloadAction.invoke(any<Collection<String>>()) }.answers { Unit }

        AllImageDownloaderUseCase(downloadAction).invoke(null)

        verify(exactly = 0) { downloadAction.invoke(any<Collection<String>>()) }
    }

    @Test
    fun testNotContainsAnyImageUrl() {
        every { downloadAction.invoke(any<Collection<String>>()) }.answers { Unit }
        every { webView.evaluateJavascript(any(), any()) }.answers { Unit }

        AllImageDownloaderUseCase(downloadAction).invoke(webView)

        verify(exactly = 1) { webView.evaluateJavascript(any(), any()) }
        verify(exactly = 0) { downloadAction.invoke(any<Collection<String>>()) }
    }

}