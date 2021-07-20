/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.search

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.annotation.ColorInt
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import jp.toastkid.lib.AppBarViewModel
import jp.toastkid.lib.ContentViewModel
import jp.toastkid.lib.preference.ColorPair
import jp.toastkid.lib.preference.PreferenceApplier
import jp.toastkid.lib.view.EditTextColorSetter
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.databinding.AppBarSearchBinding
import jp.toastkid.yobidashi.databinding.FragmentSearchBinding
import jp.toastkid.yobidashi.databinding.ModuleSearchFavoriteBinding
import jp.toastkid.yobidashi.databinding.ModuleSearchHistoryBinding
import jp.toastkid.yobidashi.databinding.ModuleSearchSuggestionBinding
import jp.toastkid.yobidashi.databinding.ModuleSearchUrlBinding
import jp.toastkid.yobidashi.databinding.ModuleUrlSuggestionBinding
import jp.toastkid.yobidashi.libs.Inputs
import jp.toastkid.yobidashi.libs.network.NetworkChecker
import jp.toastkid.yobidashi.search.category.SearchCategoryAdapter
import jp.toastkid.yobidashi.search.favorite.FavoriteSearchFragment
import jp.toastkid.yobidashi.search.favorite.FavoriteSearchModule
import jp.toastkid.yobidashi.search.history.HistoryModule
import jp.toastkid.yobidashi.search.history.SearchHistoryFragment
import jp.toastkid.yobidashi.search.suggestion.SuggestionModule
import jp.toastkid.yobidashi.search.trend.HourlyTrendModule
import jp.toastkid.yobidashi.search.url.UrlModule
import jp.toastkid.yobidashi.search.url_suggestion.UrlSuggestionModule
import jp.toastkid.yobidashi.search.voice.VoiceSearch
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber

/**
 * @author toastkidjp
 */
class SearchFragment : Fragment() {

    /**
     * View binder.
     */
    private var binding: FragmentSearchBinding? = null

    /**
     * Favorite search module.
     */
    private var favoriteModule: FavoriteSearchModule? = null

    /**
     * History module.
     */
    private var historyModule: HistoryModule? = null

    /**
     * Suggestion module.
     */
    private var suggestionModule: SuggestionModule? = null

    /**
     * Current URL module.
     */
    private var urlModule: UrlModule? = null

    /**
     * Suggestion module.
     */
    private var urlSuggestionModule: UrlSuggestionModule? = null

    private var hourlyTrendModule: HourlyTrendModule? = null

    /**
     * Does use voice search?
     */
    private var useVoice: Boolean = true

    private lateinit var preferenceApplier: PreferenceApplier

    private var contentViewModel: ContentViewModel? = null

    private var appBarViewModel: AppBarViewModel? = null

    private var headerBinding: AppBarSearchBinding? = null

    private var currentTitle: String? = null

    private var currentUrl: String? = null

    /**
     * Use for handling input action.
     */
    private val channel = Channel<String>()

    /**
     * Disposables.
     */
    private val disposables: Job by lazy { Job() }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val context = context
                ?: return super.onCreateView(inflater, container, savedInstanceState)

        binding = DataBindingUtil.inflate(inflater, LAYOUT_ID, container, false)

        currentTitle = arguments?.getString(EXTRA_KEY_TITLE)
        currentUrl = arguments?.getString(EXTRA_KEY_URL)

        headerBinding = DataBindingUtil.inflate(inflater, R.layout.app_bar_search, container, false)
        headerBinding?.fragment = this
        headerBinding?.searchCategories?.let {
            val searchCategoryAdapter = SearchCategoryAdapter(context)
            it.adapter = searchCategoryAdapter
            val categoryName = (jp.toastkid.search.SearchCategory.findByUrlOrNull(currentUrl)?.name
                    ?: PreferenceApplier(context).getDefaultSearchEngine())
                    ?: jp.toastkid.search.SearchCategory.getDefaultCategoryName()
            val index = searchCategoryAdapter.findIndex(categoryName)
            if (index != -1) {
                it.setSelection(index)
            }
        }

        preferenceApplier = PreferenceApplier(context)

        activity?.also {
            val activityViewModelProvider = ViewModelProvider(it)
            appBarViewModel = activityViewModelProvider.get(AppBarViewModel::class.java)
            contentViewModel = activityViewModelProvider.get(ContentViewModel::class.java)
        }

        initFavoriteModule()

        initHistoryModule()

        applyColor()

        urlModule = UrlModule(
                binding?.urlModule as ModuleSearchUrlBinding,
                this::setTextAndMoveCursorToEnd
        )

        setListenerForKeyboardHiding()

        initSearchInput()

        headerBinding?.searchBar?.transitionName = "share"

        contentViewModel?.snackShort(R.string.message_search_on_background)

        setHasOptionsMenu(true)

        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val viewModel = ViewModelProvider(this).get(SearchFragmentViewModel::class.java)
        viewModel.search
                .observe(viewLifecycleOwner, Observer { event ->
                    val pair = event?.getContentIfNotHandled() ?: return@Observer
                    search(extractCurrentSearchCategory(), pair.first, pair.second)
                })
        viewModel.putQuery
                .observe(viewLifecycleOwner, Observer { event ->
                    val query = event?.getContentIfNotHandled() ?: return@Observer
                    setTextAndMoveCursorToEnd(query)
                })

        suggestionModule = SuggestionModule(
                binding?.suggestionModule as ModuleSearchSuggestionBinding,
                viewModel
        )

        urlSuggestionModule = UrlSuggestionModule(
                binding?.urlSuggestionModule as ModuleUrlSuggestionBinding,
                viewModel
        )

        hourlyTrendModule = HourlyTrendModule(
                binding?.hourlyTrendModule,
                viewModel
        )
        hourlyTrendModule?.request()

        val query = arguments?.getString(EXTRA_KEY_QUERY) ?: ""
        setInitialQuery(query)
    }

    private fun setInitialQuery(query: String?) {
        headerBinding?.searchInput?.let { input ->
            input.setText(query)
            input.selectAll()
            suggest(query ?: "")
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setListenerForKeyboardHiding() {
        binding?.scroll?.setOnTouchListener { _, _ ->
            hideKeyboard()
            false
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)

        inflater.inflate(R.menu.search_menu, menu)

        menu.findItem(R.id.suggestion_check)?.isChecked = preferenceApplier.isEnableSuggestion
        menu.findItem(R.id.history_check)?.isChecked = preferenceApplier.isEnableSearchHistory
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        R.id.double_quote -> {
            val queryOrEmpty = headerBinding?.searchInput?.text?.toString()
            if (queryOrEmpty?.isNotBlank() == true) {
                setTextAndMoveCursorToEnd("\"$queryOrEmpty\"")
            }
            true
        }
        R.id.set_default_search_cagegory -> {
            val categoryName = preferenceApplier.getDefaultSearchEngine()
                ?: jp.toastkid.search.SearchCategory.getDefaultCategoryName()
            val index = (headerBinding?.searchCategories?.adapter as? SearchCategoryAdapter)
                ?.findIndex(categoryName) ?: -1
            if (index != -1) {
                headerBinding?.searchCategories?.setSelection(index)
            }
            true
        }
        R.id.suggestion_check -> {
            preferenceApplier.switchEnableSuggestion()
            item.isChecked = preferenceApplier.isEnableSuggestion
            true
        }
        R.id.history_check -> {
            preferenceApplier.switchEnableSearchHistory()
            item.isChecked = preferenceApplier.isEnableSearchHistory
            true
        }
        R.id.open_favorite_search -> {
            activity?.also {
                ViewModelProvider(it)
                        .get(ContentViewModel::class.java)
                        .nextFragment(FavoriteSearchFragment::class.java)
            }
            true
        }
        R.id.open_search_history -> {
            activity?.also {
                ViewModelProvider(it)
                        .get(ContentViewModel::class.java)
                        .nextFragment(SearchHistoryFragment::class.java)
            }
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    private fun initFavoriteModule() {
        CoroutineScope(Dispatchers.Main).launch(disposables) {
            favoriteModule = withContext(Dispatchers.Default) {
                FavoriteSearchModule(
                        binding?.favoriteModule as ModuleSearchFavoriteBinding,
                        { fav -> search(fav.category ?: "", fav.query ?: "") },
                        this@SearchFragment::hideKeyboard,
                        { setTextAndMoveCursorToEnd("${it.query} ") }
                )
            }
        }
    }

    private fun setTextAndMoveCursorToEnd(text: String) {
        headerBinding?.searchInput?.also {
            it.setText(text)
            it.setSelection(text.length)
        }
    }

    /**
     * Initialize history module asynchronously.
     */
    @SuppressLint("SetTextI18n")
    private fun initHistoryModule() {
        CoroutineScope(Dispatchers.Main).launch(disposables) {
            historyModule = withContext(Dispatchers.Default) {
                HistoryModule(
                        binding?.historyModule as ModuleSearchHistoryBinding,
                        { history -> search(history.category as String, history.query as String) }, // TODO use elvis
                        { searchHistory ->
                            headerBinding?.searchInput?.setText("${searchHistory.query} ")
                            headerBinding?.searchInput?.setSelection(
                                    headerBinding?.searchInput?.text.toString().length)
                        }
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()

        suggestionModule?.enable = preferenceApplier.isEnableSuggestion
        historyModule?.enable = preferenceApplier.isEnableSearchHistory
        favoriteModule?.enable = preferenceApplier.isEnableFavoriteSearch
        urlModule?.enable = preferenceApplier.isEnableUrlModule()
        urlSuggestionModule?.enable = preferenceApplier.isEnableViewHistory
        hourlyTrendModule?.setEnable(preferenceApplier.isEnableTrendModule())

        val headerView = headerBinding?.root ?: return
        appBarViewModel?.replace(headerView)

        urlModule?.onResume()

        showKeyboard()
    }

    private fun showKeyboard() {
        activity?.let {
            val input = headerBinding?.searchInput ?: return@let
            if (!input.requestFocus()) {
                return@let
            }

            val inputMethodManager =
                    it.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
                            ?: return@let

            input.postDelayed(
                    { inputMethodManager.showSoftInput(input, InputMethodManager.SHOW_IMPLICIT) },
                    100L
            )
        }
    }

    /**
     * Initialize search input.
     */
    private fun initSearchInput() {
        headerBinding?.searchInput?.let {
            it.setOnEditorActionListener { v, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    search(extractCurrentSearchCategory(), v.text.toString())
                }
                true
            }
            it.addTextChangedListener(object : TextWatcher {

                override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) = Unit

                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                    CoroutineScope(Dispatchers.Default).launch(disposables) { channel.send(s.toString()) }
                }

                override fun afterTextChanged(s: Editable) = Unit
            })

            invokeSuggestion()
        }
    }

    private fun invokeSuggestion() {
        CoroutineScope(Dispatchers.Default).launch(disposables) {
            channel.receiveAsFlow()
                    .distinctUntilChanged()
                    .debounce(400)
                    .collect {
                        withContext(Dispatchers.Main) { suggest(it) }
                    }
        }
    }

    private fun suggest(key: String) {
        if (key.isEmpty()|| key == currentUrl) {
            urlModule?.switch(currentTitle, currentUrl)
        } else {
            urlModule?.hide()
        }

        setActionButtonState(key.isEmpty())

        if (preferenceApplier.isEnableSearchHistory) {
            historyModule?.query(key)
        }

        favoriteModule?.query(key)
        urlSuggestionModule?.query(key)

        if (preferenceApplier.isDisableSuggestion) {
            suggestionModule?.clear()
            return
        }

        suggestionModule?.request(key)
    }

    /**
     * Set action button state.
     *
     * @param useVoiceSearch
     */
    private fun setActionButtonState(useVoiceSearch: Boolean) {
        this.useVoice = useVoiceSearch
        (if (useVoiceSearch) R.drawable.ic_mic else R.drawable.ic_search_white)
                .also { headerBinding?.searchAction?.setImageResource(it) }
    }

    /**
     * Apply color to views.
     */
    private fun applyColor() {
        val colorPair : ColorPair = preferenceApplier.colorPair()
        @ColorInt val fontColor: Int = colorPair.fontColor()
        EditTextColorSetter().invoke(headerBinding?.searchInput, fontColor)

        headerBinding?.also {
            it.searchAction.setColorFilter(fontColor)
            it.searchClear.setColorFilter(fontColor)
            it.searchInputBorder.setBackgroundColor(fontColor)
        }
    }

    fun invokeSearch() {
        if (useVoice) {
            invokeVoiceSearch()
            return
        }
        search(
                extractCurrentSearchCategory(),
                headerBinding?.searchInput?.text.toString()
        )
    }

    private fun invokeVoiceSearch() {
        try {
            context?.let {
                startActivityForResult(VoiceSearch().makeIntent(it), VoiceSearch.REQUEST_CODE)
            }
        } catch (e: ActivityNotFoundException) {
            Timber.e(e)
            VoiceSearch().suggestInstallGoogleApp(binding?.root as View, preferenceApplier.colorPair())
        }
    }

    fun invokeBackgroundSearch(): Boolean {
        if (useVoice) {
            return true
        }

        search(
                extractCurrentSearchCategory(),
                headerBinding?.searchInput?.text.toString(),
                true
        )
        return true
    }

    /**
     * Open search result.
     *
     * @param category search category
     * @param query    search query
     * @param onBackground
     */
    @Suppress("NOTHING_TO_INLINE")
    private inline fun search(category: String, query: String, onBackground: Boolean = false) {
        val context = context ?: return
        if (NetworkChecker.isNotAvailable(context)) {
            contentViewModel?.snackShort("Network is not available...")
            return
        }

        SearchAction(context, category, query, currentUrl, onBackground).invoke()
    }

    fun clearInput() {
        headerBinding?.searchInput?.setText("")
    }

    /**
     * Hide software keyboard.
     * TODO should deactivate
     */
    private fun hideKeyboard() {
        headerBinding?.searchInput?.let { Inputs.hideKeyboard(it) }
    }

    private fun extractCurrentSearchCategory() =
            headerBinding?.searchCategories?.selectedItem.toString()

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode != Activity.RESULT_OK) {
            return
        }

        when (requestCode) {
            VoiceSearch.REQUEST_CODE -> {
                val result = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                if (result == null || result.size == 0) {
                    return
                }
                suggestionModule?.clear()
                suggestionModule?.addAll(result)
                suggestionModule?.show()

                CoroutineScope(Dispatchers.Default).launch(disposables) {
                    delay(200)
                    withContext(Dispatchers.Main) {
                        val top = binding?.suggestionModule?.root?.top ?: 0
                        binding?.scroll?.smoothScrollTo(0, top)
                    }
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        hideKeyboard()
    }

    override fun onDetach() {
        hideKeyboard()
        disposables.cancel()
        channel.cancel()
        favoriteModule?.dispose()
        historyModule?.dispose()
        suggestionModule?.dispose()
        hourlyTrendModule?.dispose()
        super.onDetach()
    }

    companion object {

        /**
         * Layout ID.
         */
        @LayoutRes
        private const val LAYOUT_ID = R.layout.fragment_search


        /**
         * Extra key of query.
         */
        private const val EXTRA_KEY_QUERY = "query"

        /**
         * Extra key of title.
         */
        private const val EXTRA_KEY_TITLE = "title"

        /**
         * Extra key of URL.
         */
        private const val EXTRA_KEY_URL = "url"

        /**
         * Make launch [Intent].
         */
        fun makeWith(title: String? = null, url: String? = null) =
                SearchFragment()
                        .also { fragment ->
                            fragment.arguments = Bundle().also { bundle ->
                                title?.let {
                                    bundle.putString(EXTRA_KEY_TITLE, it)
                                }
                                url?.let {
                                    bundle.putString(EXTRA_KEY_URL, it)
                                }
                            }
                        }

        /**
         * Make launcher [Intent] with query.
         *
         * @param query Query
         * @param title Title
         * @param url URL
         */
        fun makeWithQuery(query: String, title: String?, url: String? = null) =
                makeWith(title, url).also { fragment ->
                    fragment.arguments?.putString(EXTRA_KEY_QUERY, query)
                }

    }
}