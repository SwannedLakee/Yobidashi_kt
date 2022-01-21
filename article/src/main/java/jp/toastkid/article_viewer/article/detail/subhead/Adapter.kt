/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.article_viewer.article.detail.subhead

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.ListAdapter
import jp.toastkid.article_viewer.R
import jp.toastkid.lib.view.list.CommonItemCallback

/**
 *
 * @param layoutInflater Use for inflating item view
 * @param viewModel Use for sending click event
 *
 * @author toastkidjp
 */
class Adapter(
        private val layoutInflater: LayoutInflater,
        private val viewModel: SubheadDialogFragmentViewModel
) : ListAdapter<String, ViewHolder>(
    CommonItemCallback.with({ a, b -> a.hashCode() == b.hashCode() }, { a, b -> a == b })
) {

    private val subheads = mutableListOf<String>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
                layoutInflater.inflate(ITEM_ID, parent, false)
                        as? TextView ?: TextView(parent.context)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.setText(item)
        holder.itemView.setOnClickListener {
            viewModel.subhead(item)
        }
    }

    fun addAll(items: List<String>?) {
        submitList(items)
    }

    companion object {

        @LayoutRes
        private val ITEM_ID = R.layout.item_subhead_dialog

    }
}