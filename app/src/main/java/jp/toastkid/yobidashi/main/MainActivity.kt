/*
 * Copyright (c) 2022 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.yobidashi.main

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.lifecycle.ViewModelProvider
import jp.toastkid.lib.ContentViewModel
import jp.toastkid.lib.preference.PreferenceApplier
import jp.toastkid.ui.theme.AppTheme
import jp.toastkid.web.webview.GlobalWebViewPool
import jp.toastkid.yobidashi.main.ui.Content

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val preferenceApplier = PreferenceApplier(this)

        val contentViewModel = ViewModelProvider(this).get(ContentViewModel::class.java)
        contentViewModel.initializeWith(preferenceApplier)

        setContent {
            AppTheme(
                contentViewModel.colorPair(),
                isSystemInDarkTheme() || preferenceApplier.useDarkMode()
            ) {
                Content()
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        GlobalWebViewPool.storeStates(outState)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        GlobalWebViewPool.restoreStates(savedInstanceState)
    }

    override fun onResume() {
        super.onResume()
        GlobalWebViewPool.onResume()
    }

    override fun onPause() {
        super.onPause()
        GlobalWebViewPool.onPause()
    }

    override fun onDestroy() {
        GlobalWebViewPool.dispose()
        super.onDestroy()
    }

}
