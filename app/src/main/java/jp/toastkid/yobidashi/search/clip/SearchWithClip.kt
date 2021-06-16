package jp.toastkid.yobidashi.search.clip

import android.content.ClipboardManager
import android.content.Context
import android.view.View
import androidx.core.net.toUri
import jp.toastkid.lib.BrowserViewModel
import jp.toastkid.lib.Urls
import jp.toastkid.lib.preference.ColorPair
import jp.toastkid.lib.preference.PreferenceApplier
import jp.toastkid.search.UrlFactory
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.libs.Toaster
import jp.toastkid.yobidashi.libs.network.NetworkChecker

/**
 * Search action with clipboard text.
 * Initialize with ClipboardManager, parent view, and color pair.
 *
 * @param clipboardManager For monitoring clipboard.
 * @param parent Use for showing snackbar.
 * @param colorPair Use for showing snackbar.
 * @param browserViewModel [BrowserViewModel].
 *
 * @author toastkidjp
 */
class SearchWithClip(
    private val clipboardManager: ClipboardManager,
    private val parent: View,
    private val colorPair: ColorPair,
    private val browserViewModel: BrowserViewModel?,
    private val preferenceApplier: PreferenceApplier
) {

    /**
     * Last clopped epoch ms.
     */
    private var lastClipped: Long = 0L

    /**
     * [ClipboardManager.OnPrimaryClipChangedListener].
     */
    private val listener: ClipboardManager.OnPrimaryClipChangedListener by lazy {
        ClipboardManager.OnPrimaryClipChangedListener{
            if (isInvalidCondition() || NetworkChecker.isNotAvailable(parent.context)) {
                return@OnPrimaryClipChangedListener
            }

            val firstItem = clipboardManager.primaryClip?.getItemAt(0)
                    ?: return@OnPrimaryClipChangedListener

            val text = firstItem.text
            if (text.isNullOrEmpty()
                || (Urls.isInvalidUrl(text.toString()) && LENGTH_LIMIT <= text.length)
                || preferenceApplier.lastClippedWord() == text
            ) {
                return@OnPrimaryClipChangedListener
            }

            preferenceApplier.setLastClippedWord(text.toString())

            val context = parent.context
            Toaster.snackLong(
                parent,
                context.getString(R.string.message_clip_search, text),
                R.string.title_search_action,
                { searchOrBrowse(context, text) },
                colorPair
            )
        }
    }

    /**
     * If it is invalid condition, return true.
     *
     * @return If it is invalid condition, return true.
     */
    private fun isInvalidCondition(): Boolean {
        return (!preferenceApplier.enableSearchWithClip
                || !clipboardManager.hasPrimaryClip()
                || (System.currentTimeMillis() - lastClipped) < DISALLOW_INTERVAL_MS)
    }

    /**
     * Invoke action.
     */
    operator fun invoke() {
        clipboardManager.addPrimaryClipChangedListener(listener)
    }

    /**
     * Open search result or url.
     *
     * @param context
     * @param text
     */
    private fun searchOrBrowse(context: Context, text: CharSequence) {
        val query = text.toString()

        val url =
                if (Urls.isValidUrl(query)) query
                else UrlFactory()(preferenceApplier.getDefaultSearchEngine()
                        ?: jp.toastkid.search.SearchCategory.getDefaultCategoryName(), query).toString()
        browserViewModel?.preview(url.toUri())
    }

    /**
     * Unregister listener.
     */
    fun dispose() {
        clipboardManager.removePrimaryClipChangedListener(listener)
    }

    companion object {

        /**
         * Disallow interval ms.
         */
        private const val DISALLOW_INTERVAL_MS: Long = 500L

        /**
         * Limit of text length.
         */
        private const val LENGTH_LIMIT = 40
    }
}
