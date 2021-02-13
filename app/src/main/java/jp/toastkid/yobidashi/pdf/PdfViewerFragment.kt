/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.pdf

import android.content.res.ColorStateList
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import androidx.annotation.LayoutRes
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import jp.toastkid.lib.AppBarViewModel
import jp.toastkid.lib.ContentScrollable
import jp.toastkid.lib.preference.ColorPair
import jp.toastkid.lib.preference.PreferenceApplier
import jp.toastkid.lib.tab.OnBackCloseableTabUiFragment
import jp.toastkid.lib.view.EditTextColorSetter
import jp.toastkid.lib.view.RecyclerViewScroller
import jp.toastkid.yobidashi.CommonFragmentAction
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.databinding.AppBarPdfViewerBinding
import jp.toastkid.yobidashi.databinding.FragmentPdfViewerBinding

/**
 * @author toastkidjp
 */
class PdfViewerFragment : Fragment(), OnBackCloseableTabUiFragment, CommonFragmentAction, ContentScrollable {

    /**
     * Data binding object.
     */
    private lateinit var binding: FragmentPdfViewerBinding

    private lateinit var appBarBinding: AppBarPdfViewerBinding

    /**
     * Adapter.
     */
    private lateinit var adapter: Adapter

    /**
     * LayoutManager.
     */
    private lateinit var layoutManager: LinearLayoutManager

    private var appBarViewModel: AppBarViewModel? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        binding = DataBindingUtil.inflate(
                inflater,
                LAYOUT_ID,
                container,
                false
        )
        appBarBinding = DataBindingUtil.inflate(
                inflater,
                APP_BAR_CONTENT_LAYOUT_ID,
                container,
                false
        )
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = Adapter(LayoutInflater.from(context), context?.contentResolver)
        layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)

        binding.pdfImages.adapter = adapter
        binding.pdfImages.layoutManager = layoutManager
        PagerSnapHelper().attachToRecyclerView(binding.pdfImages)

        appBarBinding.seek.addOnChangeListener { _, value, _ ->
            appBarBinding.input.setText((value.toInt() + 1).toString())
        }
        appBarBinding.input.addTextChangedListener(object: TextWatcher {
            override fun afterTextChanged(p0: Editable?) = Unit

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) = Unit

            override fun onTextChanged(inputText: CharSequence?, p1: Int, p2: Int, p3: Int) {
                inputText?.let {
                    val newIndex = try {
                        Integer.parseInt(it.toString()) - 1
                    } catch (e: NumberFormatException) {
                        -1
                    }

                    if (newIndex == -1) {
                        return@let
                    }

                    scrollTo(newIndex)
                }
            }

        })

        activity?.let {
            appBarViewModel = ViewModelProvider(it).get(AppBarViewModel::class.java)
        }

        arguments?.let { arguments ->
            arguments.getParcelable<Uri>(KEY_URI)?.also { load(it) }
            arguments.getInt(KEY_SCROLL_Y).also { scrollTo(it) }
        }
    }

    /**
     * Load PDF content from [Uri].
     *
     * @param uri
     */
    private fun load(uri: Uri) {
        adapter.load(uri)
        binding.pdfImages.scheduleLayoutAnimation()
        appBarBinding.seek.valueTo = (adapter.itemCount - 1).toFloat()
    }

    /**
     * Scroll to specified position.
     *
     * @param position
     */
    private fun scrollTo(position: Int) {
        layoutManager.scrollToPosition(getSafeIndex(position))
    }

    /**
     * Get safe index.
     *
     * @param index
     */
    private fun getSafeIndex(index: Int): Int =
            if (index < 0 || adapter.itemCount < index) 0 else index

    /**
     * Animate root view.
     *
     * @param animation
     */
    fun animate(animation: Animation) {
        binding.root.startAnimation(animation)
    }

    override fun toTop() {
        RecyclerViewScroller.toTop(binding.pdfImages, adapter.itemCount)
    }

    override fun toBottom() {
        RecyclerViewScroller.toBottom(binding.pdfImages, adapter.itemCount)
    }

    override fun onResume() {
        super.onResume()
        appBarViewModel?.replace(appBarBinding.root)
        applyColor(PreferenceApplier(appBarBinding.root.context).colorPair())
    }

    /**
     * Apply color to views.
     *
     * @param colorPair
     */
    private fun applyColor(colorPair: ColorPair) {
        appBarBinding.appBar.setBackgroundColor(colorPair.bgColor())
        appBarBinding.seek.thumbTintList = ColorStateList.valueOf(colorPair.fontColor())
        appBarBinding.seek.trackActiveTintList = ColorStateList.valueOf(colorPair.fontColor())
        EditTextColorSetter().invoke(appBarBinding.input, colorPair.fontColor())
    }

    fun setInitialArguments(uri: Uri?, scrolled: Int) {
        arguments = bundleOf(KEY_URI to uri, KEY_SCROLL_Y to scrolled)
    }

    companion object {

        @LayoutRes
        private const val LAYOUT_ID = R.layout.fragment_pdf_viewer

        @LayoutRes
        private const val APP_BAR_CONTENT_LAYOUT_ID = R.layout.app_bar_pdf_viewer

        private const val KEY_URI = "uri"

        private const val KEY_SCROLL_Y = "scrollY"

    }
}