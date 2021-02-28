package jp.toastkid.yobidashi.libs.network

import android.webkit.CookieManager
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl

/**
 * For sharing [WebView]'s cookie.
 *
 * @author toastkidjp
 */
object WebViewCookieHandler : CookieJar {

    private const val DELIMITER = ";"

    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        val urlString = url.toString()
        val cookieManager = CookieManager.getInstance()
        cookies.forEach { cookieManager.setCookie(urlString, it.toString()) }
    }

    override fun loadForRequest(url: HttpUrl): List<Cookie> {
        val urlString = url.toString()
        val cookieManager = CookieManager.getInstance()
        val cookiesString = cookieManager.getCookie(urlString)

        return if (cookiesString != null && cookiesString.isNotEmpty()) {
            cookiesString
                    .split(DELIMITER)
                    .mapNotNull { Cookie.parse(url, it) }
        } else {
            emptyList()
        }
    }
}