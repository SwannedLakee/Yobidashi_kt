package jp.toastkid.web.webview

import android.webkit.WebView
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Test

/**
 * @author toastkidjp
 */
class SelectedTextExtractorTest {

    @Test
    fun test() {
        val selectedTextExtractor = SelectedTextExtractor()
        val webView = mockk<WebView>()
        every { webView.evaluateJavascript(any(), any()) }.answers {  }

        selectedTextExtractor.withAction(webView, {})
        verify(atMost = 1) { webView.evaluateJavascript(any(), any()) }
    }

}