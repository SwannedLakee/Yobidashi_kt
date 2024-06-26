/*
 * Copyright (c) 2024 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.yobidashi4.markdown.domain.service

import android.net.Uri
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import io.mockk.verify
import jp.toastkid.lib.ContentViewModel
import jp.toastkid.markdown.domain.model.InternalLinkScheme
import jp.toastkid.markdown.domain.service.LinkBehaviorService
import kotlinx.coroutines.Dispatchers
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * @author toastkidjp
 */
class LinkBehaviorServiceTest {

    @InjectMockKs
    private lateinit var linkBehaviorService: LinkBehaviorService

    @MockK
    private lateinit var contentViewModel: ContentViewModel

    @MockK
    private lateinit var exists: (String) -> Boolean

    @MockK
    private lateinit var internalLinkScheme: InternalLinkScheme

    @Suppress("unused")
    private val ioDispatcher = Dispatchers.Unconfined

    @Before
    fun setUp() {
        MockKAnnotations.init(this)

        every { contentViewModel.open(any()) }.just(Runs)
        every { internalLinkScheme.isInternalLink(any()) }.returns(true)
        every { internalLinkScheme.extract(any()) }.returns("yahoo")
        coEvery { contentViewModel.newArticle(any()) }.just(Runs)
        coEvery { contentViewModel.snackShort(any<String>()) }.just(Runs)
    }

    @Test
    fun testNullUrl() {
        linkBehaviorService.invoke(null)

        verify(exactly = 0) { contentViewModel.open(any()) }
        verify(exactly = 0) { contentViewModel.newArticle(any()) }
    }

    @Test
    fun testEmptyUrl() {
        linkBehaviorService.invoke("")

        verify(exactly = 0) { contentViewModel.open(any()) }
        verify(exactly = 0) { contentViewModel.newArticle(any()) }
    }

    @Test
    fun testWebUrl() {
        mockkStatic(Uri::class)
        every { Uri.parse(any()) }.returns(mockk())
        every { internalLinkScheme.isInternalLink(any()) }.returns(false)

        linkBehaviorService.invoke("https://www.yahoo.co.jp")

        verify(exactly = 1) { contentViewModel.open(any()) }
        verify(exactly = 0) { contentViewModel.newArticle(any()) }
    }

    @Test
    fun testArticleUrlDoesNotExists() {
        coEvery { exists(any()) }.answers { false }

        linkBehaviorService.invoke("internal-article://yahoo")

        verify(exactly = 0) { contentViewModel.open(any()) }
        coVerify(exactly = 0) { contentViewModel.newArticle(any()) }
        coVerify(exactly = 1) { contentViewModel.snackShort(any<String>())}
    }

    @Test
    fun testArticleUrl() {
        coEvery { exists(any()) }.answers { true }

        linkBehaviorService.invoke("internal-article://yahoo")

        verify(exactly = 0) { contentViewModel.open(any()) }
        coVerify(exactly = 1) { contentViewModel.newArticle(any()) }
        coVerify(exactly = 0) { contentViewModel.snackShort(any<String>())}
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

}