package jp.toastkid.search

import android.net.Uri
import androidx.core.net.toUri

/**
 * @author toastkidjp
 */
class SearchQueryExtractor {

    private val commonQueryParameterNames = setOf("q", "query", "text", "word")

    operator fun invoke(url: String?) = invoke(url?.toUri())

    operator fun invoke(uri: Uri?): String? {
        val host = uri?.host ?: return null
        return when {
            host.startsWith("www.google.")
                    or host.startsWith("play.google.")
                    or host.startsWith("www.bing.")
                    or host.endsWith("developer.android.com")
                    or host.endsWith("scholar.google.com")
                    or host.endsWith("www.aolsearch.com")
                    or host.endsWith("www.ask.com")
                    or host.endsWith("twitter.com")
                    or host.endsWith("stackoverflow.com")
                    or host.endsWith("github.com")
                    or host.endsWith("mvnrepository.com")
                    or host.endsWith("searchcode.com")
                    or host.equals("www.qwant.com")
                    or host.equals("www.reddit.com")
                    or host.equals("www.economist.com")
                    or host.equals("www.ft.com")
                    or host.equals("www.startpage.com")
                    or host.equals("www.imdb.com")
                    or host.equals("duckduckgo.com")
                    or host.endsWith("medium.com")
                    or host.endsWith("ted.com")
                    or host.endsWith(".slideshare.net")
                    or host.endsWith("cse.google.com")
                    or host.endsWith(".buzzfeed.com")
                    or host.endsWith("openweathermap.org")
                    or host.endsWith(".quora.com")
                    or host.endsWith(".livejournal.com")
                    or host.endsWith("search.daum.net")
                    or host.endsWith(".teoma.com")
                    or host.endsWith("www.info.com")
                    or host.endsWith("results.looksmart.com")
                    or host.equals("www.privacywall.org")
                    or host.equals("alohafind.com")
                    or host.equals("www.mojeek.com")
                    or host.equals("www.ecosia.org")
                    or host.equals("www.findx.com")
                    or host.equals("www.bbc.co.uk")
                    or host.equals("hn.algolia.com")
                    or host.endsWith("search.gmx.com")
                    or host.equals("search.sify.com")
                    or host.equals("www.givero.com") ->
                uri.getQueryParameter("q")
            host.startsWith("www.amazon.") ->
                uri.getQueryParameter("field-keywords")
            host.endsWith(".linkedin.com") ->
                uri.getQueryParameter("keywords")
            host.contains("yandex.") ->
                uri.getQueryParameter("text")
            host.endsWith(".youtube.com") ->
                uri.getQueryParameter("search_query")
            host.startsWith("www.flickr.") ->
                uri.getQueryParameter("text")
            host.endsWith(".yelp.com") ->
                uri.getQueryParameter("find_desc")
            host.equals("www.tumblr.com")
                    or host.equals("ejje.weblio.jp")
                    or host.equals("web.archive.org")-> uri.lastPathSegment
            host.endsWith("archive.org")
                    or host.endsWith("search.naver.com")
                    or host.endsWith("www.morningstar.com")
                    or host.endsWith("info.finance.yahoo.co.jp")
                    or host.endsWith(".rambler.ru") ->
                uri.getQueryParameter("query")
            host.endsWith(".wikipedia.org")
                or host.endsWith(".wikimedia.org") ->
                if (uri.queryParameterNames.contains("search")) {
                    uri.getQueryParameter("search")
                } else {
                    // "/wiki/"'s length.
                    Uri.decode(uri.encodedPath?.substring(6))
                }
            host.endsWith("search.yahoo.com")
                    or host.endsWith("search.yahoo.co.jp") ->
                uri.getQueryParameter("p")
            host.endsWith("www.baidu.com") ->
                uri.getQueryParameter("wd")
            host.endsWith("myindex.jp") ->
                uri.getQueryParameter("w")
            host == "www.wolframalpha.com" ->
                uri.getQueryParameter("i")
            host == "search.goo.ne.jp" ->
                uri.getQueryParameter("MT")
            host == "bgr.com" ->
                uri.getQueryParameter("s")?.replace("#$".toRegex(), "")
            host.endsWith("facebook.com")
                    or host.equals("www.merriam-webster.com")
                    or host.equals("www.instagram.com")
                    or host.equals("www.espn.com") ->
                Uri.decode(uri.lastPathSegment)
            else -> uri.getQueryParameter(
                    commonQueryParameterNames
                            .find { uri.queryParameterNames.contains(it) } ?: ""
            )
        }
    }
}