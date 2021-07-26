/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.article_viewer.article.detail

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.annotation.UiThread
import androidx.core.widget.addTextChangedListener
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import jp.toastkid.article_viewer.R
import jp.toastkid.article_viewer.article.ArticleRepository
import jp.toastkid.article_viewer.article.data.AppDatabase
import jp.toastkid.article_viewer.article.detail.markdown.MarkdownConverterProviderUseCase
import jp.toastkid.article_viewer.article.detail.subhead.SubheadDialogFragment
import jp.toastkid.article_viewer.article.detail.subhead.SubheadDialogFragmentViewModel
import jp.toastkid.article_viewer.bookmark.Bookmark
import jp.toastkid.article_viewer.databinding.AppBarContentViewerBinding
import jp.toastkid.article_viewer.databinding.FragmentContentBinding
import jp.toastkid.lib.AppBarViewModel
import jp.toastkid.lib.BrowserViewModel
import jp.toastkid.lib.ContentScrollable
import jp.toastkid.lib.ContentViewModel
import jp.toastkid.lib.TabListViewModel
import jp.toastkid.lib.color.LinkColorGenerator
import jp.toastkid.lib.preference.PreferenceApplier
import jp.toastkid.lib.tab.OnBackCloseableTabUiFragment
import jp.toastkid.lib.view.TextViewHighlighter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/**
 * @author toastkidjp
 */
class ContentViewerFragment : Fragment(), ContentScrollable, OnBackCloseableTabUiFragment {

    private lateinit var binding: FragmentContentBinding

    private lateinit var appBarBinding: AppBarContentViewerBinding

    private lateinit var textViewHighlighter: TextViewHighlighter

    private lateinit var repository: ArticleRepository

    private val subheads = mutableListOf<String>()

    private val disposables = Job()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_content,
            container,
            false
        )
        appBarBinding = DataBindingUtil.inflate(inflater, R.layout.app_bar_content_viewer, container, false)
        appBarBinding.fragment = this
        appBarBinding.tabListViewModel = activity?.let {
            ViewModelProvider(it).get(TabListViewModel::class.java)
        }
        textViewHighlighter = TextViewHighlighter(binding.content)
        repository = AppDatabase.find(binding.root.context).articleRepository()

        activity?.let {
            ContextMenuInitializer(
                    binding.content,
                    ViewModelProvider(it).get(BrowserViewModel::class.java)
            ).invoke()
        }

        val linkBehaviorService = makeLinkBehaviorService()

        val linkMovementMethod = ContentLinkMovementMethod {
            linkBehaviorService?.invoke(it)
        }
        binding.content.movementMethod = linkMovementMethod

        setHasOptionsMenu(true)
        return binding.root
    }

    private fun makeLinkBehaviorService(): LinkBehaviorService? {
        val activity = activity ?: return null
        val viewModelProvider = ViewModelProvider(activity)
        return LinkBehaviorService(
                viewModelProvider.get(ContentViewModel::class.java),
                viewModelProvider.get(BrowserViewModel::class.java),
                { repository.exists(it) > 0 }
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        ViewModelProvider(this)
                .get(SubheadDialogFragmentViewModel::class.java)
                .subhead
                .observe(viewLifecycleOwner, { })
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        binding.content.linksClickable = true

        appBarBinding.input.addTextChangedListener {
            search(it.toString())
        }

        val activity = activity ?: return
        ViewModelProvider(activity).get(TabListViewModel::class.java)
                .tabCount
                .observe(activity, { appBarBinding.tabCount.text = it.toString() })
    }

    override fun onResume() {
        super.onResume()
        val preferenceApplier = PreferenceApplier(binding.root.context)
        binding.contentScroll.setBackgroundColor(preferenceApplier.editorBackgroundColor())

        val editorFontColor = preferenceApplier.editorFontColor()
        binding.content.setTextColor(editorFontColor)
        binding.content.setLinkTextColor(LinkColorGenerator().invoke(editorFontColor))
        binding.content.highlightColor = preferenceApplier.editorHighlightColor(Color.CYAN)

        appBarBinding.searchResult.setTextColor(preferenceApplier.fontColor)
        appBarBinding.input.setTextColor(preferenceApplier.fontColor)
        appBarBinding.tabIcon.setColorFilter(preferenceApplier.fontColor)
        appBarBinding.tabCount.setTextColor(preferenceApplier.fontColor)
        appBarBinding.subhead.setColorFilter(preferenceApplier.fontColor)

        activity?.let {
            ViewModelProvider(it).get(AppBarViewModel::class.java)
                    .replace(appBarBinding.root)
        }
    }

    @UiThread
    fun loadContent(title: String) {
        appBarBinding.searchResult.text = title

        val context = binding.root.context

        ContentLoaderUseCase(
                repository,
                MarkdownConverterProviderUseCase()(context),
                binding.content,
                subheads
        ).invoke(title)
    }

    fun tabList() {
        activity?.let {
            ViewModelProvider(it).get(ContentViewModel::class.java).switchTabList()
        }
    }

    private fun search(keyword: String?) {
        textViewHighlighter(keyword)
    }

    override fun toTop() {
        binding.contentScroll.smoothScrollTo(0, 0)
    }

    fun showSubheads() {
        if (subheads.isEmpty()) {
            return
        }

        val dialogFragment = SubheadDialogFragment.make(subheads)
        dialogFragment.setTargetFragment(this, 1)
        dialogFragment.show(parentFragmentManager, SubheadDialogFragment::class.java.canonicalName)
    }

    override fun toBottom() {
        binding.contentScroll.smoothScrollTo(0, binding.content.measuredHeight)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_content_viewer, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_add_to_bookmark -> {
                CoroutineScope(Dispatchers.IO).launch {
                    val title = appBarBinding.searchResult.text.toString()
                    val article = repository.findFirst(title) ?: return@launch
                    AppDatabase.find(appBarBinding.root.context).bookmarkRepository().add(Bookmark(article.id))
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onDetach() {
        disposables.cancel()
        super.onDetach()
    }

}