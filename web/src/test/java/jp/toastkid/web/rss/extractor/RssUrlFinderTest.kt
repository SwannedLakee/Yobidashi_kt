/*
 * Copyright (c) 2021 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.web.rss.extractor

import android.view.View
import androidx.lifecycle.ViewModelStoreOwner
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.SpyK
import io.mockk.just
import io.mockk.mockk
import io.mockk.unmockkAll
import io.mockk.verify
import jp.toastkid.api.html.HtmlApi
import jp.toastkid.lib.ContentViewModel
import jp.toastkid.lib.preference.PreferenceApplier
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import okhttp3.Response
import okhttp3.ResponseBody
import org.junit.After
import org.junit.Before
import org.junit.Test

class RssUrlFinderTest {

    @InjectMockKs
    private lateinit var rssUrlFinder: RssUrlFinder

    @MockK
    private lateinit var preferenceApplier: PreferenceApplier

    @MockK
    private lateinit var urlValidator: RssUrlValidator

    @MockK
    private lateinit var rssUrlExtractor: RssUrlExtractor

    @MockK
    private lateinit var htmlApi: HtmlApi

    @MockK
    private lateinit var contentViewModelFactory: (ViewModelStoreOwner) -> ContentViewModel?

    @Suppress("unused")
    private val mainDispatcher: CoroutineDispatcher = Dispatchers.Unconfined

    @Suppress("unused")
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.Unconfined

    @SpyK(recordPrivateCalls = true)
    private var response: Response = mockk()

    @MockK
    private lateinit var body: ResponseBody

    @MockK
    private lateinit var contentViewModel: ContentViewModel

    @MockK
    private lateinit var view: View

    @MockK
    private lateinit var snackbarParentSupplier: () -> View

    @Before
    fun setUp() {
        MockKAnnotations.init(this)

        every { preferenceApplier.saveNewRssReaderTargets(any()) }.just(Runs)
        every { urlValidator.invoke(any()) }.returns(false)
        coEvery { htmlApi.invoke(any()) }.returns(response)
        coEvery { rssUrlExtractor.invoke(any()) }.returns(listOf("https://rss.yahoo.co.jp/1"))
        coEvery { response getProperty "isSuccessful" }.returns(true)
        coEvery { response getProperty "body" }.returns(body)
        coEvery { body.string() }.returns("test")
        coEvery { body.close() }.just(Runs)

        every { contentViewModelFactory.invoke(any()) }.returns(contentViewModel)
        every { contentViewModel.snackShort(any<Int>()) }.returns(mockk())
        every { contentViewModel.snackShort(any<String>()) }.returns(mockk())

        every { snackbarParentSupplier.invoke() }.returns(view)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun testSuccessfulCase() {
        rssUrlFinder.invoke("https://www.yahoo.co.jp")

        verify(exactly = 0) { preferenceApplier.saveNewRssReaderTargets(any()) }
        verify(atLeast = 1) { urlValidator.invoke(any()) }
        coVerify(exactly = 1) { htmlApi.invoke(any()) }
        coVerify(exactly = 1) { rssUrlExtractor.invoke(any()) }
        coVerify(exactly = 1) { response getProperty "isSuccessful" }
        coVerify(exactly = 1) { response getProperty "body" }
        coVerify(exactly = 1) { body.string() }
        coVerify(exactly = 1) { body.close() }

        verify(exactly = 1) { contentViewModel.snackShort(any<Int>()) }
        verify(exactly = 0) { contentViewModel.snackShort(any<String>()) }
    }

    @Test
    fun testBlankInputCase() {
        rssUrlFinder.invoke(" ")

        verify(exactly = 0) { preferenceApplier.saveNewRssReaderTargets(any()) }
        verify(exactly = 0) { urlValidator.invoke(any()) }
        coVerify(exactly = 0) { htmlApi.invoke(any()) }
        coVerify(exactly = 0) { rssUrlExtractor.invoke(any()) }
        coVerify(exactly = 0) { response getProperty "isSuccessful" }
        coVerify(exactly = 0) { response getProperty "body" }
        coVerify(exactly = 0) { body.string() }

        verify(exactly = 0) { contentViewModel.snackShort(any<Int>()) }
        verify(exactly = 0) { contentViewModel.snackShort(any<String>()) }
    }

    @Test
    fun testInputUrlIsValidCase() {
        every { urlValidator.invoke(any()) }.returns(true)

        rssUrlFinder.invoke("https://www.yahoo.co.jp")

        verify(exactly = 1) { preferenceApplier.saveNewRssReaderTargets(any()) }
        verify(exactly = 1) { urlValidator.invoke(any()) }
        coVerify(exactly = 0) { htmlApi.invoke(any()) }
        coVerify(exactly = 0) { rssUrlExtractor.invoke(any()) }
        coVerify(exactly = 0) { response getProperty "isSuccessful" }
        coVerify(exactly = 0) { response getProperty "body" }
        coVerify(exactly = 0) { body.string() }

        verify(exactly = 0) { contentViewModel.snackShort(any<Int>()) }
        verify(exactly = 1) { contentViewModel.snackShort(any<String>()) }
    }


    @Test
    fun testHtmlApiReturnsNullCase() {
        coEvery { htmlApi.invoke(any()) }.returns(null)

        rssUrlFinder.invoke("https://www.yahoo.co.jp")

        verify(exactly = 0) { preferenceApplier.saveNewRssReaderTargets(any()) }
        verify(exactly = 1) { urlValidator.invoke(any()) }
        coVerify(exactly = 1) { htmlApi.invoke(any()) }
        coVerify(exactly = 0) { rssUrlExtractor.invoke(any()) }
        coVerify(exactly = 0) { response getProperty "isSuccessful" }
        coVerify(exactly = 0) { response getProperty "body" }
        coVerify(exactly = 0) { body.string() }

        verify(exactly = 1) { contentViewModel.snackShort(any<Int>()) }
        verify(exactly = 0) { contentViewModel.snackShort(any<String>()) }
    }

    @Test
    fun testIsNotSuccessfulCase() {
        coEvery { response getProperty "isSuccessful" }.returns(false)

        rssUrlFinder.invoke("https://www.yahoo.co.jp")

        verify(exactly = 0) { preferenceApplier.saveNewRssReaderTargets(any()) }
        verify(exactly = 1) { urlValidator.invoke(any()) }
        coVerify(exactly = 1) { htmlApi.invoke(any()) }
        coVerify(exactly = 0) { rssUrlExtractor.invoke(any()) }
        coVerify(exactly = 1) { response getProperty "isSuccessful" }
        coVerify(exactly = 0) { response getProperty "body" }
        coVerify(exactly = 0) { body.string() }

        verify(exactly = 1) { contentViewModel.snackShort(any<Int>()) }
        verify(exactly = 0) { contentViewModel.snackShort(any<String>()) }
    }

}