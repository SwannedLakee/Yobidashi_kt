/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.article_viewer.article.detail.subhead

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import jp.toastkid.article_viewer.article.detail.ContentViewerFragment

/**
 * @author toastkidjp
 */
class SubheadDialogFragment : BottomSheetDialogFragment() {

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        val context = context ?: return null
        val recyclerView = RecyclerView(context)
        val findFragmentByTag =
            parentFragmentManager.findFragmentByTag(ContentViewerFragment::class.java.canonicalName)
                ?: return null
        val viewModel = ViewModelProvider(findFragmentByTag)
            .get(SubheadDialogFragmentViewModel::class.java)
        recyclerView.adapter =
                Adapter(layoutInflater, viewModel)
                        .also { it.addAll(arguments?.getStringArrayList(KEY_EXTRA_ITEM)) }
        recyclerView.layoutManager =
                LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        return recyclerView
    }

    companion object {

        private const val KEY_EXTRA_ITEM = "items"

        fun make(items: List<String>) = SubheadDialogFragment().also {
            it.arguments = bundleOf(KEY_EXTRA_ITEM to items)
        }

    }

}