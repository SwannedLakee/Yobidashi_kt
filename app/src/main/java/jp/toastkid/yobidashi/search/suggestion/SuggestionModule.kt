package jp.toastkid.yobidashi.search.suggestion

import android.view.LayoutInflater
import androidx.core.view.isVisible
import com.google.android.flexbox.AlignItems
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import jp.toastkid.lib.preference.PreferenceApplier
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.databinding.ModuleSearchSuggestionBinding
import jp.toastkid.yobidashi.libs.Toaster
import jp.toastkid.yobidashi.libs.network.NetworkChecker
import jp.toastkid.yobidashi.search.SearchFragmentViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Facade of search suggestion module.
 * Initialize with binding object.
 *
 * @param binding Data binding object
 * @param queryPutter Use for query inserting
 * @param searchCallback Callback on search
 * @param searchBackgroundCallback Callback for background search
 * @param onClick Callback on click
 *
 * @author toastkidjp
 */
class SuggestionModule(
        private val binding: ModuleSearchSuggestionBinding,
        viewModel: SearchFragmentViewModel
) {

    /**
     * Suggest ModuleAdapter.
     */
    private val adapter: Adapter = Adapter(
            LayoutInflater.from(binding.root.context),
            viewModel
    )

    /**
     * Suggestion API.
     */
    private val suggestionApi = SuggestionApi()

    /**
     * Cache.
     */
    private val cache = HashMap<String, List<String>>(SUGGESTION_CACHE_CAPACITY)

    /**
     * Last subscription's lastSubscription.
     */
    private var lastSubscription: Job? = null

    var enable: Boolean = true

    init {
        val layoutManager = FlexboxLayoutManager(binding.root.context)
        layoutManager.flexDirection = FlexDirection.ROW
        layoutManager.flexWrap = FlexWrap.WRAP
        layoutManager.justifyContent = JustifyContent.FLEX_START
        layoutManager.alignItems = AlignItems.STRETCH

        initializeSearchSuggestionList(layoutManager)
    }

    private fun initializeSearchSuggestionList(layoutManager: FlexboxLayoutManager) {
        binding.searchSuggestions.layoutManager = layoutManager
        binding.searchSuggestions.adapter = adapter
    }

    /**
     * Clear suggestion items.
     */
    fun clear() {
        adapter.clear()
        adapter.notifyDataSetChanged()
    }

    /**
     * Request web API.
     *
     * @param key
     */
    fun request(key: String) {
        lastSubscription?.cancel()

        if (cache.containsKey(key)) {
            val cachedList = cache[key] ?: return
            lastSubscription = replace(cachedList)
            return
        }

        val context = binding.root.context
        if (NetworkChecker.isNotAvailable(context)) {
            return
        }

        if (PreferenceApplier(context).wifiOnly && NetworkChecker.isUnavailableWiFi(context)) {
            Toaster.tShort(context, R.string.message_wifi_not_connecting)
            return
        }

        suggestionApi.fetchAsync(key) { suggestions ->
            if (suggestions.isEmpty()) {
                CoroutineScope(Dispatchers.Main).launch { hide() }
                return@fetchAsync
            }
            cache[key] = suggestions
            lastSubscription = replace(suggestions)
        }
    }

    /**
     * Use for voice search.
     *
     * @param words Recognizer result words.
     */
    internal fun addAll(words: List<String>) {
        CoroutineScope(Dispatchers.Main).launch {
            withContext(Dispatchers.Default) {
                words.forEach { adapter.add(it) }
            }
            adapter.notifyDataSetChanged()
        }
    }

    /**
     * Replace suggestions with specified items.
     *
     * @param suggestions
     * @return [Job]
     */
    private fun replace(suggestions: Iterable<String>): Job {
        return CoroutineScope(Dispatchers.Main).launch {
            withContext(Dispatchers.Default) {
                adapter.clear()
                suggestions.forEach { adapter.add(it) }
            }
            show()
            adapter.notifyDataSetChanged()
        }
    }

    /**
     * Show this module.
     */
    fun show() {
        if (!binding.root.isVisible && enable) {
            runOnMainThread { binding.root.isVisible = true }
        }
    }

    /**
     * Hide this module.
     */
    fun hide() {
        if (binding.root.isVisible) {
            runOnMainThread { binding.root.isVisible = false }
        }
    }

    private fun runOnMainThread(action: () -> Unit) =
            CoroutineScope(Dispatchers.Main).launch { action() }

    /**
     * Dispose last subscription.
     */
    fun dispose() {
        lastSubscription?.cancel()
    }

    companion object {

        /**
         * Suggest cache capacity.
         */
        private const val SUGGESTION_CACHE_CAPACITY = 30
    }
}
