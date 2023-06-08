/*
 * Copyright (c) 2023 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.yobidashi.browser.webview.usecase

import android.net.Uri
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import io.mockk.verify
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ApproachFallbackUrlExtractorTest {

    @InjectMockKs
    private lateinit var approachFallbackUrlExtractor: ApproachFallbackUrlExtractor

    @MockK
    private lateinit var uri: Uri

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun invoke() {
        every { uri.getQueryParameter(any()) } returns "https://www.yahoo.co.jp"
        mockkStatic(Uri::class)
        every { Uri.decode("https://www.yahoo.co.jp") } returns "https://www.yahoo.co.jp"

        approachFallbackUrlExtractor.invoke(uri) {
            assertEquals("https://www.yahoo.co.jp", it)
            verify { uri.getQueryParameter(any()) }
            verify { Uri.decode("https://www.yahoo.co.jp") }
        }
    }

    @Test
    fun isTarget() {
        assertTrue(approachFallbackUrlExtractor.isTarget("approach.yahoo.co.jp"))
        assertFalse(approachFallbackUrlExtractor.isTarget("other.yahoo.co.jp"))
    }

}