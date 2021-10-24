package jp.toastkid.yobidashi.search

import android.content.Context
import android.net.Uri
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import jp.toastkid.lib.BrowserViewModel
import jp.toastkid.lib.Urls
import jp.toastkid.lib.preference.PreferenceApplier
import jp.toastkid.search.UrlFactory
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.search.history.SearchHistoryInsertion
import kotlinx.coroutines.Job

/**
 * Search action shortcut.
 *
 * @param activityContext Activity's context
 * @param category Search Category
 * @param query Search query (or URL)
 * @param currentUrl Current URL for site-search
 * @param onBackground
 * @param saveHistory
 * @param viewModelSupplier
 * @param preferenceApplierSupplier
 * @param urlFactory
 * @author toastkidjp
 */
class SearchAction(
        private val activityContext: Context,
        private val category: String,
        private val query: String,
        private val currentUrl: String? = null,
        private val onBackground: Boolean = false,
        private val saveHistory: Boolean = true,
        private val viewModelSupplier: (Context) -> BrowserViewModel? = {
            (activityContext as? FragmentActivity)?.let {
                ViewModelProvider(it).get(BrowserViewModel::class.java)
            }
        },
        private val preferenceApplierSupplier: (Context) -> PreferenceApplier = {
            PreferenceApplier(activityContext)
        },
        private val urlFactory: UrlFactory = UrlFactory()
) {

    /**
     * Invoke action.
     *
     * @return job which invoked search-history insertion
     */
    operator fun invoke(): Job {
        val disposable = insertToSearchHistory()

        val validatedUrl = Urls.isValidUrl(query)

        withInternalBrowser(validatedUrl)
        return disposable
    }

    private fun insertToSearchHistory(): Job =
            if (preferenceApplierSupplier(activityContext).isEnableSearchHistory
                && isNotUrl(query) && saveHistory) {
                SearchHistoryInsertion.make(activityContext, category, query).insert()
            } else {
                Job()
            }

    /**
     * Check passed query is not URL.
     *
     * @param query
     */
    private fun isNotUrl(query: String): Boolean = !query.contains("://")

    /**
     * Action with internal browser.
     *
     * @param validatedUrl passed query is URL.
     */
    private fun withInternalBrowser(validatedUrl: Boolean) {
        val browserViewModel = viewModelSupplier(activityContext)

        if (validatedUrl) {
            openUri(browserViewModel, Uri.parse(query))
            return
        }

        val searchUri = urlFactory(category, query, currentUrl)
        openUri(browserViewModel, searchUri)
    }

    private fun openUri(browserViewModel: BrowserViewModel?, uri: Uri) {
        if (onBackground)
            browserViewModel?.openBackground(
                    activityContext.getString(R.string.title_tab_background_search, query),
                    uri
            )
        else
            browserViewModel?.open(uri)
    }

}
