/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.web.webview

import android.app.Activity
import android.content.Context
import android.media.MediaPlayer
import android.os.Build
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsets
import android.view.WindowManager
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.widget.FrameLayout
import android.widget.VideoView
import androidx.core.content.ContextCompat
import timber.log.Timber

/**
 * [WebView]'s custom view switcher.
 *
 * @param contextSupplier Use for making parent view.
 * @param currentWebViewSupplier Use for getting current [WebView]
 * @author toastkidjp
 */
class CustomViewSwitcher(
        private val contextSupplier: () -> Context,
        private val currentWebViewSupplier: () -> View?
) {

    /**
     * Custom view container.
     */
    private var customViewContainer: FrameLayout? = null

    /**
     * Holding for controlling video content.
     */
    private var videoView: VideoView? = null

    /**
     * Holding for recover previous orientation.
     */
    private var originalOrientation: Int = 0

    /**
     * Holding for disposing.
     */
    private var customViewCallback: WebChromeClient.CustomViewCallback? = null

    /**
     * Holding for disposing custom view.
     */
    private var customView: View? = null

    /**
     * Delegation from WebChromeClient.
     *
     * @param view Custom view from [WebView].
     * @param callback from [WebView]
     */
    fun onShowCustomView(view: View?, callback: WebChromeClient.CustomViewCallback?) {
        if (customView != null) {
            callback?.onCustomViewHidden()
            return
        }

        val activity = contextSupplier() as? Activity ?: return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            activity.window.insetsController?.hide(WindowInsets.Type.statusBars())
        } else {
            activity.window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }

        originalOrientation = activity.requestedOrientation

        activity.requestedOrientation = activity.requestedOrientation

        customViewContainer = FrameLayout(activity)
        customViewContainer?.setBackgroundColor(ContextCompat.getColor(activity, jp.toastkid.lib.R.color.filter_white_aa))
        view?.keepScreenOn = true

        val listener = VideoCompletionListener()
        if (view is FrameLayout) {
            val child = view.focusedChild
            if (child is VideoView) {
                videoView = child
                videoView?.setOnErrorListener(listener)
                videoView?.setOnCompletionListener(listener)
            }
        } else if (view is VideoView) {
            videoView = view
            videoView?.setOnErrorListener(listener)
            videoView?.setOnCompletionListener(listener)
        }

        customViewCallback = callback
        customView = view

        val decorView = activity.window.decorView as? FrameLayout
        decorView?.addView(customViewContainer, customViewParams)
        customViewContainer?.addView(customView, customViewParams)
        decorView?.requestLayout()

        currentWebViewSupplier()?.visibility = View.INVISIBLE
    }

    /**
     * Delegation from WebChromeClient.
     */
    fun onHideCustomView() {
        val activity = contextSupplier() as? Activity ?: return

        activity.window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        customView?.keepScreenOn = false

        if (customViewCallback != null) {
            try {
                customViewCallback?.onCustomViewHidden()
            } catch (e: Exception) {
                Timber.w("Error hiding custom view", e)
            }

            customViewCallback = null
        }

        currentWebViewSupplier()?.visibility = View.VISIBLE

        if (customViewContainer != null) {
            val parent = customViewContainer?.parent as? ViewGroup
            parent?.removeView(customViewContainer)
            customViewContainer?.removeAllViews()
        }

        customViewContainer = null
        customView = null

        videoView?.stopPlayback()
        videoView?.setOnErrorListener(null)
        videoView?.setOnCompletionListener(null)
        videoView = null

        try {
            customViewCallback?.onCustomViewHidden()
        } catch (e: Exception) {
            Timber.w(e)
        }

        customViewCallback = null
        activity.requestedOrientation = originalOrientation
    }

    /**
     * Video completion listener.
     */
    private inner class VideoCompletionListener
        : MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener {

        /**
         * Only return false.
         *
         * @param mp [MediaPlayer]
         * @param what [Int]
         * @param extra [Int]
         * @return false
         */
        override fun onError(mp: MediaPlayer, what: Int, extra: Int): Boolean = false

        /**
         * Only call onHideCustomView.
         *
         * @param mp [MediaPlayer]
         */
        override fun onCompletion(mp: MediaPlayer) = onHideCustomView()

    }

    companion object {

        /**
         * For centering video view.
         */
        private val customViewParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT,
                Gravity.CENTER
        )
    }
}