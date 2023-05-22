/*
 * Copyright (c) 2021 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.yobidashi.search.clip

import android.content.ClipboardManager
import android.view.View
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.unmockkAll
import io.mockk.verify
import jp.toastkid.lib.ContentViewModel
import jp.toastkid.lib.preference.ColorPair
import jp.toastkid.lib.preference.PreferenceApplier
import org.junit.After
import org.junit.Before
import org.junit.Test

class SearchWithClipTest {

    @InjectMockKs
    private lateinit var searchWithClip: SearchWithClip

    @MockK
    private lateinit var clipboardManager: ClipboardManager

    @MockK
    private lateinit var parent: View

    @MockK
    private lateinit var colorPair: ColorPair

    @MockK
    private lateinit var contentViewModel: ContentViewModel

    @MockK
    private lateinit var preferenceApplier: PreferenceApplier

    @Before
    fun setUp() {
        MockKAnnotations.init(this)

        every { clipboardManager.addPrimaryClipChangedListener(any()) }.returns(Unit)
        every { clipboardManager.removePrimaryClipChangedListener(any()) }.returns(Unit)
        every { preferenceApplier.lastClippedWord() }.returns("last")
        every { preferenceApplier.setLastClippedWord(any()) }.returns(Unit)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun testInvoke() {
        searchWithClip.invoke()

        verify(exactly = 1) { clipboardManager.addPrimaryClipChangedListener(any()) }
    }

    @Test
    fun testDispose() {
        searchWithClip.dispose()

        verify(exactly = 1) { clipboardManager.removePrimaryClipChangedListener(any()) }
    }

}