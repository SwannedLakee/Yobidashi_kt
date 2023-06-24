package jp.toastkid.web.bookmark

import android.content.Context
import androidx.annotation.WorkerThread
import androidx.core.net.toUri
import jp.toastkid.data.repository.factory.RepositoryFactory
import jp.toastkid.lib.storage.FilesDir
import jp.toastkid.web.FaviconFolderProviderService
import jp.toastkid.web.icon.WebClipIconLoader
import jp.toastkid.yobidashi.browser.bookmark.model.Bookmark
import jp.toastkid.yobidashi.browser.bookmark.model.BookmarkRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

/**
 * Bookmark initializer.
 *
 * @author toastkidjp
 */
class BookmarkInitializer(
    private val bookmarkRepository: BookmarkRepository,
    private val favicons: FilesDir,
    private val webClipIconLoader: WebClipIconLoader,
    private val mainDispatcher: CoroutineDispatcher = Dispatchers.Main,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {

    /**
     * Default bookmarks.
     */
    private val defaultBookmarks: Map<String, Map<String, String>> = mapOf(
            "Recommended" to mapOf(
                "Google Translate" to "https://translate.google.com/",
                "DeepL" to "https://www.deepl.com/translator",
                "YouTube" to "https://www.youtube.com/",
                "AccuWeather" to "https://www.accuweather.com/",
                "Wikipedia" to "https://${Locale.getDefault().language}.wikipedia.org/",
                "Google Map" to "https://www.google.co.jp/maps/",
                "Yelp" to "https://www.yelp.com/",
                "Amazon" to "https://www.amazon.com/",
                "Project Gutenberg" to "http://www.gutenberg.org/",
                "Expedia" to "https://www.expedia.com",
                "Slashdot" to "https://m.slashdot.org"
            ),
            "Search" to mapOf(
                    "Google" to "https://www.google.com/",
                    "Bing" to "https://www.bing.com/",
                    "Yahoo!" to "https://www.yahoo.com/",
                    "Yahoo! JAPAN" to "https://www.yahoo.co.jp/"
            ),
            "Finance" to mapOf(
                    "Google Finance" to "https://www.google.com/finance",
                    "Yahoo Finance" to "https://finance.yahoo.com/",
                    "Financial Times" to "https://www.ft.com/",
                    "THE WALL STREET JOURNAL" to "https://www.wsj.com"
            ),
            "SNS" to mapOf(
                    "Instagram" to "https://www.instagram.com/",
                    "Twitter" to "https://twitter.com/",
                    "Facebook" to "https://www.facebook.com/"
            )
    )

    /**
     * Invoke action.
     *
     * @param context
     */
    operator fun invoke(onComplete: () -> Unit = {}): Job {
        return CoroutineScope(mainDispatcher).launch {
            withContext(ioDispatcher) {
                addBookmarks(bookmarkRepository, favicons)
            }

            onComplete()

            withContext(ioDispatcher) {
                defaultBookmarks.values.flatMap { it.values }.forEach {
                    @Suppress("DeferredResultUnused")
                    async { webClipIconLoader.invoke(it) }
                }
            }
        }
    }

    @WorkerThread
    private fun addBookmarks(bookmarkRepository: BookmarkRepository, favicons: FilesDir) {
        defaultBookmarks.forEach {
            val parent = it.key

            bookmarkRepository.add(makeFolder(parent))

            it.value.entries.forEach { entry ->
                bookmarkRepository.add(makeItem(entry, favicons, parent))
            }
        }
    }

    private fun makeItem(entry: Map.Entry<String, String>, favicons: FilesDir, parent: String) =
            Bookmark().also {
                it.title = entry.key
                it.url = entry.value
                it.favicon =
                        favicons.assignNewFile("${entry.value.toUri().host}.png")
                                .absolutePath
                it.parent = parent
                it.folder = false
            }

    private fun makeFolder(parent: String) =
            Bookmark().also {
                it.title = parent
                it.parent = Bookmark.getRootFolderName()
                it.folder = true
            }

    companion object {

        fun from(context: Context) =
            BookmarkInitializer(
                RepositoryFactory().bookmarkRepository(context),
                FaviconFolderProviderService().invoke(context),
                WebClipIconLoader.from(context)
            )

    }
}