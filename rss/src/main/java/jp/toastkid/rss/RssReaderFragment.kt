/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.rss

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.core.net.toUri
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import jp.toastkid.lib.BrowserViewModel
import jp.toastkid.lib.ContentScrollable
import jp.toastkid.lib.ContentViewModel
import jp.toastkid.lib.fragment.CommonFragmentAction
import jp.toastkid.lib.preference.PreferenceApplier
import jp.toastkid.lib.view.RecyclerViewScroller
import jp.toastkid.rss.api.RssReaderApi
import jp.toastkid.rss.databinding.FragmentRssReaderBinding
import jp.toastkid.rss.list.Adapter
import jp.toastkid.rss.setting.RssSettingFragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * @author toastkidjp
 */
class RssReaderFragment : Fragment(), CommonFragmentAction, ContentScrollable {

    private lateinit var binding: FragmentRssReaderBinding

    private var viewModel: RssReaderFragmentViewModel? = null

    private val disposables = Job()

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        binding = DataBindingUtil.inflate(inflater, LAYOUT_ID, container, false)
        setHasOptionsMenu(true)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val context = view.context

        val fragmentActivity = activity ?: return
        viewModel = ViewModelProvider(this).get(RssReaderFragmentViewModel::class.java)
        observeViewModelEvent(fragmentActivity)

        val adapter = Adapter(LayoutInflater.from(context), viewModel)
        binding.rssList.adapter = adapter
        binding.rssList.layoutManager =
                LinearLayoutManager(context, RecyclerView.VERTICAL, false)

        val readRssReaderTargets = PreferenceApplier(fragmentActivity).readRssReaderTargets()

        if (readRssReaderTargets.isEmpty()) {
            ViewModelProvider(fragmentActivity).get(ContentViewModel::class.java)
                .snackShort(R.string.message_rss_reader_launch_failed)
            activity?.supportFragmentManager?.popBackStack()
            return
        }

        CoroutineScope(Dispatchers.IO).launch(disposables) {
            readRssReaderTargets.asFlow()
                    .map { RssReaderApi().invoke(it) }
                    .collect {
                        withContext(Dispatchers.Main) {
                            val items = it?.items
                            adapter.addAll(items)
                            adapter.notifyDataSetChanged()
                        }
                    }
        }
    }

    private fun observeViewModelEvent(fragmentActivity: FragmentActivity) {
        viewModel?.itemClick?.observe(viewLifecycleOwner, Observer {
            val event = it?.getContentIfNotHandled() ?: return@Observer

            val browserViewModel = ViewModelProvider(fragmentActivity)
                    .get(BrowserViewModel::class.java)
            if (event.second) {
                browserViewModel.openBackground(event.first.toUri())
            } else {
                browserViewModel.open(event.first.toUri())
                activity?.supportFragmentManager?.popBackStack()
            }
        })
    }

    override fun toTop() {
        RecyclerViewScroller.toTop(binding.rssList, binding.rssList.adapter?.itemCount ?: 0)
    }

    override fun toBottom() {
        RecyclerViewScroller.toBottom(binding.rssList, binding.rssList.adapter?.itemCount ?: 0)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.rss_reader, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_rss_setting) {
            val activity = activity ?: return true
            ViewModelProvider(activity)
                    .get(ContentViewModel::class.java)
                    .nextFragment(RssSettingFragment::class.java)
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun pressBack(): Boolean {
        activity?.supportFragmentManager?.popBackStack()
        return true
    }

    override fun onDetach() {
        super.onDetach()
        disposables.cancel()
    }

    companion object {
        @LayoutRes
        private val LAYOUT_ID = R.layout.fragment_rss_reader
    }
}