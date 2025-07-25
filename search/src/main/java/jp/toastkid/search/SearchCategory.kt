package jp.toastkid.search

import android.net.Uri
import android.webkit.URLUtil
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.net.toUri
import java.util.Locale

/**
 * Web search category.
 *
 * @author toastkidjp
 */
enum class SearchCategory(
        @StringRes val id: Int,
        @DrawableRes val iconId: Int,
        private val host: String,
        private val generator: (l: String, h: String, q: String) -> String = { _, h, q ->  h + Uri.encode(q) }
) {
    GOOGLE(
            R.string.google,
            R.drawable.ic_google,
            "https://www.google.com/search?q="
    ),
    GOOGLE_SIMPLE(
        R.string.google_simple,
        R.drawable.ic_google,
        "https://www.google.com/search?udm=14&q="
    ),
    YAHOO(
            R.string.search_category_yahoo,
            R.drawable.ic_yahoo,
            "https://search.yahoo.com/search?p="
    ),
    YAHOO_JAPAN_REALTIME_SEARCH(
            R.string.search_category_yahoo_japan_realtime_search,
            R.drawable.ic_yahoo_japan_realtime_search,
            "https://search.yahoo.co.jp/realtime/search?p="
    ),
    YAHOO_JAPAN(
            R.string.search_category_yahoo_japan,
            R.drawable.ic_yahoo_japan_logo,
            "https://search.yahoo.co.jp/search?p="
    ),
    SITE_SEARCH(
        R.string.title_site_search_by_google,
        R.drawable.ic_site_search,
        "https://www.google.com/search?q="
    ),
    WOLFRAM_ALPHA(
            R.string.wolfram_alpha,
            R.drawable.ic_wolfram_alpha,
            "https://www.wolframalpha.com/input/?i="
    ),
    BING(
            R.string.bing,
            R.drawable.ic_bing_logo,
            "https://www.bing.com/search?q="
    ),
    DUCKDUCKGO(
            R.string.search_category_web,
            R.drawable.ic_duckduckgo,
            "https://duckduckgo.com/?ia=web&q="
    ),
    DUCKDUCKGO_LITE(
        R.string.search_category_duckduckgo_lite,
        R.drawable.ic_duckduckgo,
        "https://lite.duckduckgo.com/lite/?ia=web&q="
    ),
    AOL(
            R.string.aol,
            R.drawable.ic_aol,
            "https://www.aolsearch.com/search?s_chn=prt_bon-mobile&q="
    ),
    ASK_COM(
            R.string.search_category_ask_com,
            R.drawable.ic_ask_com,
            "https://www.ask.com/web?q="
    ),
    QWANT(
            R.string.search_category_qwant,
            R.drawable.ic_qwant,
            "https://www.qwant.com/?q="
    ),
    GMX(
            R.string.search_category_gmx,
            R.drawable.ic_gmx,
            "https://search.gmx.com/web?q="
    ),
    ECOSIA(
            R.string.search_category_ecosia,
            R.drawable.ic_ecosia,
            "https://www.ecosia.org/search?q="
    ),
    FINDX(
            R.string.search_category_findx,
            R.drawable.ic_findx,
            "https://www.findx.com/search?q="
    ),
    STARTPAGE(
            R.string.search_category_startpage,
            R.drawable.ic_startpage,
            "https://www.startpage.com/sp/search?q="
    ),
    TEOMA(
            R.string.search_category_teoma,
            R.drawable.ic_teoma,
            "https://www.teoma.com/web?q="
    ),
    INFO_COM(
            R.string.search_category_info_com,
            R.drawable.ic_info_com,
            "https://www.info.com/serp?q="
    ),
    LOOKSMART(
            R.string.search_category_looksmart,
            R.drawable.ic_looksmart,
            "https://results.looksmart.com/serp?q="
    ),
    MOJEEK(
            R.string.search_category_mojeek,
            R.drawable.ic_mojeek,
            "https://www.mojeek.com/search?q="
    ),
    PRIVACY_WALL(
            R.string.search_category_privacy_wall,
            R.drawable.ic_privacywall,
            "https://www.privacywall.org/search/secure/?q="
    ),
    BRAVE(
        R.string.search_category_brave,
        R.drawable.ic_brave,
        "https://search.brave.com/search?source=web&q="
    ),
    ALOHA_FIND(
            R.string.search_category_aloha_find,
            R.drawable.ic_alohafind,
            "https://alohafind.com/search/?q="
    ),
    GIVERO(
            R.string.givero,
            R.drawable.ic_givero,
            "https://www.givero.com/search?q="
    ),
    YOU_COM(
        R.string.you_com,
        R.drawable.ic_you_com,
        "https://you.com/search?q="
    ),
    YANDEX(
            R.string.search_category_yandex,
            R.drawable.ic_yandex,
            "https://www.yandex.com/search/?text="
    ),
    RAMBLER(
            R.string.search_category_rambler,
            R.drawable.ic_rambler,
            "https://nova.rambler.ru/search?query="
    ),
    GOO(
            R.string.goo,
            R.drawable.ic_goo,
            "https://search.goo.ne.jp/web.jsp?IE=UTF-8&OE=UTF-8&MT="
    ),
    SIFY(
        R.string.sify,
        R.drawable.ic_sify,
        "http://search.sify.com/search.php?q="
    ),
    NAVER(
            R.string.naver,
            R.drawable.ic_naver,
            "https://m.search.naver.com/search.naver?ie=utf8&query="
    ),
    DAUM(
            R.string.daum,
            R.drawable.ic_daum,
            "https://search.daum.net/search?w=tot&q="
    ),
    SCHOLAR(
            R.string.search_category_google_scholar,
            R.drawable.ic_google_scholar,
            "https://scholar.google.com/scholar?q="
    ),
    IMAGE(
            R.string.search_category_image,
            R.drawable.ic_image_search,
            "https://www.google.co.jp/search?site=imghp&tbm=isch&q="
    ),
    YOUTUBE(
            R.string.search_category_youtube,
            R.drawable.ic_video,
            "https://m.youtube.com/results?search_query="
    ),
    BBC(
            R.string.search_category_bbc,
            R.drawable.ic_bbc,
            "https://www.bbc.co.uk/search?q="
    ),
    WIKIPEDIA(
            R.string.search_category_wikipedia,
            R.drawable.ic_wikipedia,
            "https://%s.m.wikipedia.org/w/index.php?search=",
            { l, h, q -> String.format(h, l) + Uri.encode(q) }
    ),
    WEBSTER(
            R.string.search_category_webster,
            R.drawable.ic_webster,
            "https://www.merriam-webster.com/dictionary/"
    ),
    WEBLIO(
            R.string.search_category_weblio,
            R.drawable.ic_weblio,
            "https://ejje.weblio.jp/content/"
    ),
    DICTIONARY_COM(
        R.string.search_category_dictionary_com,
        R.drawable.ic_dictionary_com,
        "https://www.dictionary.com/browse/"
    ),
    INTERNET_ARCHIVE(
            R.string.search_category_internet_archive,
            R.drawable.ic_internet_archive,
            "https://archive.org/search.php?query=",
            { l, h, q -> String.format(h, l) + Uri.encode(q) }
    ),
    WAYBACK_MACHINE(
            R.string.wayback_machine,
            R.drawable.ic_wayback_machine,
            "https://web.archive.org/web/*/"
    ),
    LINKED_IN(
            R.string.linked_in,
            R.drawable.ic_linked_in,
            "https://www.linkedin.com/jobs/search?keywords="
    ),
    X(
            R.string.search_category_twitter,
            R.drawable.ic_x,
            "https://www.x.com/search?src=typd&q="
    ),
    REDDIT(
        R.string.search_category_reddit,
        R.drawable.ic_reddit,
        "https://www.reddit.com/search/?q="
    ),
    FACEBOOK(
            R.string.search_category_facebook,
            R.drawable.ic_facebook,
            "https://m.facebook.com/public/"
    ),
    INSTAGRAM(
            R.string.search_category_instagram,
            R.drawable.ic_instagram_logo,
            "https://www.instagram.com/explore/tags/"
    ),
    FLICKR(
            R.string.search_category_flickr,
            R.drawable.ic_flickr,
            "https://www.flickr.com/search/?text="
    ),
    PX500(
        R.string.search_category_500px,
        R.drawable.ic_500px,
        "https://500px.com/search?type=photos&sort=relevance&q="
    ),
    WIKIMEDIA_COMMONS(
            R.string.search_category_wikimedia_commons,
            R.drawable.ic_wikimedia_commons,
            "https://commons.wikimedia.org/w/index.php?search="
    ),
    MAP(
            R.string.search_category_map,
            R.drawable.ic_map,
            "https://www.google.co.jp/maps/place/"
    ),
    OPEN_STREET_MAP(
            R.string.search_category_open_street_map,
            R.drawable.ic_openstreetmap,
            "https://www.openstreetmap.org/search?query="
    ),
    OPEN_WEATHER_MAP(
            R.string.search_category_open_weather,
            R.drawable.ic_open_weather,
            "https://openweathermap.org/find?q="
    ),
    APPS(
            R.string.search_category_apps,
            R.drawable.ic_google_play,
            "https://play.google.com/store/search?q="
    ),
    YELP(
            R.string.yelp,
            R.drawable.ic_yelp,
            "https://www.yelp.com/search?find_desc="
    ),
    ESPN(
            R.string.espn,
            R.drawable.ic_espn,
            "https://www.espn.com/search/_/q/"
    ),
    IMDB(
            R.string.imdb,
            R.drawable.ic_imdb,
            "https://www.imdb.com/find?q="
    ),
    GUTENBERG(
            R.string.gutenberg,
            R.drawable.ic_local_library_black,
            "http://www.gutenberg.org/ebooks/search/?query="
    ),
    AMAZON(
            R.string.search_category_shopping,
            R.drawable.ic_amazon,
            "https://www.amazon.co.jp/s/ref=nb_sb_noss?field-keywords=",
            { l, h, q ->
                if (Locale.JAPANESE.language == l) {
                    h + Uri.encode(q)
                } else {
                    "https://www.amazon.com/s/ref=nb_sb_noss?field-keywords=" + Uri.encode(q)
                }
            }
    ),
    EBAY(
        R.string.search_category_ebay,
        R.drawable.ic_ebay,
        "https://www.ebay.com/sch/i.html?_nkw="
    ),
    QUORA(
            R.string.search_category_quora,
            R.drawable.ic_quora,
            "https://www.quora.com/search?q=",
            { l, h, q ->
                if (Locale.JAPANESE.language == l) {
                    "https://jp.quora.com/search?q=${Uri.encode(q)}"
                } else {
                    "$h${Uri.encode(q)}"
                }
            }
    ),
    TECHNICAL_QA(
            R.string.search_category_stack_overflow,
            R.drawable.ic_stackoverflow,
            "https://stackoverflow.com/search?q="
    ),
    BGR(
            R.string.search_category_bgr,
            R.drawable.ic_bgr,
            "https://bgr.com/?s=%s",
            { _, h, q -> String.format(h, Uri.encode(q)) }
    ),
    HACKER_NEWS(
            R.string.search_category_hacker_news,
            R.drawable.ic_hacker_news,
            "https://hn.algolia.com/?q="
    ),
    TECHNOLOGY(
            R.string.search_category_technology,
            R.drawable.ic_techcrunch,
            "http://jp.techcrunch.com/search/",
            {l, h, q ->
                if (Locale.JAPANESE.language == l) {
                    h + Uri.encode(q)
                }
                "https://techcrunch.com/search/" + Uri.encode(q)
            }
    ),
    GIZMODE(
        R.string.search_category_gizmode,
        R.drawable.ic_gizmodo,
        "https://gizmodo.com/search?q="
    ),
    LIFEHACKER(
        R.string.search_category_lifehacker,
        R.drawable.ic_lifehacker,
        "https://lifehacker.com/search?q="
    ),
    GITHUB(
            R.string.search_category_github,
            R.drawable.ic_github,
            "https://github.com/search?utf8=%E2%9C%93&type=&q="
    ),
    MVNREPOSITORY(
            R.string.search_category_mvnrepository,
            R.drawable.ic_mvn,
            "https://mvnrepository.com/search?q="
    ),
    ANDROID_DEVELOPER(
        R.string.search_category_android_developer,
        R.drawable.ic_android_developer,
        "https://developer.android.com/s/results?q="
    ),
    MEDIUM(
            R.string.medium,
            R.drawable.ic_medium,
            "https://medium.com/search?q="
    ),
    TUMBLR(
            R.string.tumblr,
            R.drawable.ic_tumblr,
            "https://www.tumblr.com/search/"
    ),
    TED(
            R.string.ted,
            R.drawable.ic_ted,
            "https://www.ted.com/search?q="
    ),
    SLIDESHARE(
            R.string.slideshare,
            R.drawable.ic_slideshare,
            "https://www.slideshare.net/search/slideshow?q="
    ),
    SPEAKERDECK(
            R.string.speakerdeck,
            R.drawable.ic_speakerdeck,
            "https://cse.google.com/cse?cx=010150859881542981030%3Ahqhxyxpwtc4&q="
    ),
    ECONOMIST(
            R.string.search_category_economist,
            R.drawable.ic_the_economist_logo,
            "https://www.economist.com/search?q="
    ),
    FT(
            R.string.financial_times,
            R.drawable.ic_financial_times,
            "https://www.ft.com/search?q="
    ),
    MORNINGSTAR(
            R.string.morningstar,
            R.drawable.ic_morningstar,
            "https://www.morningstar.com/search?query="
    ),
    FORBES(
        R.string.forbes,
        R.drawable.ic_forbes,
        "https://www.forbes.com/search?q="
    ),
    MY_INDEX(
            R.string.my_index,
            R.drawable.ic_myindex,
            "https://myindex.jp/search.php?w="
    ),
    SEEKING_ALPHA(
        R.string.seeking_alpha,
        R.drawable.ic_seeking_alpha,
        "https://seekingalpha.com/search?q="
    ),
    YAHOO_JAPAN_FINANCE(
            R.string.yahoo_japan_finance,
            R.drawable.ic_yahoo_japan_finance,
            "https://info.finance.yahoo.co.jp/search/?query="
    ),
    BUZZFEED(
            R.string.buzzfeed,
            R.drawable.ic_buzzfeed,
            "https://www.buzzfeed.com/jp/search?q="
    ),
    LIVEJOURNAL(
            R.string.livejournal,
            R.drawable.ic_livejournal,
            "https://www.livejournal.com/gsearch/?engine=google&cx=partner-pub-5600223439108080:3711723852&q="
    ),
    POLITICO(
        R.string.politico,
        R.drawable.ic_politico,
        "https://www.politico.com/search?q="
    ),
    GOOGLE_NEWS(
        R.string.google_news,
        R.drawable.ic_google_news,
        "https://news.google.com/search?q="
    ),
    JAPAN_SEARCH(
        R.string.search_category_japan_search,
        R.drawable.ic_japan_search,
        "https://jpsearch.go.jp/csearch/jps-cross?csid=jps-cross&keyword="
    )
    ;

    /**
     * Make search URL with query.
     *
     * @param query Query string
     * @param currentUrl
     * @return Search result URL
     */
    fun make(query: String, currentUrl: String?): String {
        if (this == SITE_SEARCH && currentUrl != null) {
            return SiteSearchUrlGenerator().invoke(currentUrl, query)
        }
        return generate(
                Locale.getDefault().language,
                host,
                query
        )
    }

    /**
     * Generate URL.
     *
     * @param l Locale string
     * @param h Host of search result
     * @param q Search query
     *
     * @return Search result URL
     */
    private fun generate(l: String, h: String, q: String): String = generator(l, h, q)

    companion object {

        private val hostAndCategories =
            entries
                .filter { it != SITE_SEARCH && it != MAP && it != IMAGE }.associateBy {
                    val key =
                        if (it == YAHOO_JAPAN_REALTIME_SEARCH) "search.yahoo.co.jp/realtime" else it.host.toUri().host
                    key
                }

        fun findByUrlOrNull(url: String?): SearchCategory? {
            val key = makeKey(url)
            return if (key.isNullOrEmpty()) null else hostAndCategories[key]
        }

        private fun makeKey(url: String?): String? {
            if (url.isNullOrBlank() || URLUtil.isValidUrl(url).not()) {
                return null
            }

            val uri = url.toUri()
            if (url.contains("/realtime/").not()) {
                return uri.host
            }

            val pathSegments = uri.pathSegments
            val firstPath = if (pathSegments.size >= 2) pathSegments[0] ?: "" else ""
            return "${uri.host}/$firstPath"
        }

        /**
         * Find [SearchCategory] by search category.
         *
         * @param category Search category
         * @return [SearchCategory]
         */
        fun findByCategory(category: String?): SearchCategory {
            val locale = Locale.getDefault()
            val target = category?.uppercase(locale) ?: ""
            return entries.find { it.name == target } ?: getDefault()
        }

        /**
         * Find index in values by search category string form.
         *
         * @param category Search category string form
         *
         * @return index
         */
        fun findIndex(category: String): Int {
            val locale = Locale.getDefault()
            return entries.find { it.name == category.uppercase(locale) } ?.ordinal
                    ?: getDefault().ordinal
        }

        /**
         * Get default object.
         *
         * @return GOOGLE
         */
        fun getDefault(): SearchCategory = GOOGLE

        /**
         * Get default category name.
         *
         * @return "GOOGLE"
         */
        fun getDefaultCategoryName(): String = getDefault().name
    }
}