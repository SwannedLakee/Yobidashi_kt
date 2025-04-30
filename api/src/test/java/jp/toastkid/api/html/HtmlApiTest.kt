/*
 * Copyright (c) 2021 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.api.html

import android.webkit.URLUtil
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import io.mockk.verify
import jp.toastkid.api.lib.HttpClientFactory
import okhttp3.Call
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.ResponseBody
import org.junit.After
import org.junit.Before
import org.junit.Test

class HtmlApiTest {

    private lateinit var htmlApi: HtmlApi

    @MockK
    private lateinit var httpClient: OkHttpClient

    @MockK
    private lateinit var call: Call

    @MockK
    private lateinit var response: Response

    @MockK
    private lateinit var body: ResponseBody

    @Before
    fun setUp() {
        MockKAnnotations.init(this)

        mockkConstructor(HttpClientFactory::class)
        every { anyConstructed<HttpClientFactory>().withTimeout(any()) }.returns(httpClient)
        every { httpClient.newCall(any()) }.returns(call)
        every { call.execute() }.returns(response)
        every { response.isSuccessful } returns true
        every { response.body } returns body
        every { body.string() } returns "test"

        htmlApi = HtmlApi()

        mockkStatic(URLUtil::class)
        every { URLUtil.isNetworkUrl(any()) }.returns(true)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun testInvoke() {
        htmlApi.invoke("https://www.yahoo.co.jp")

        verify(exactly = 1) { anyConstructed<HttpClientFactory>().withTimeout(any()) }
        verify(exactly = 1) { httpClient.newCall(any()) }
        verify(exactly = 1) { call.execute() }
    }

    @Test
    fun testUrlIsBlank() {
        htmlApi.invoke(" ")

        verify(exactly = 1) { anyConstructed<HttpClientFactory>().withTimeout(any()) }
        verify(exactly = 0) { httpClient.newCall(any()) }
        verify(exactly = 0) { call.execute() }
    }

    @Test
    fun testUrlIsInvalid() {
        every { URLUtil.isNetworkUrl(any()) }.returns(false)

        htmlApi.invoke("ttps://www.yahoo.co.jp")

        verify(exactly = 1) { anyConstructed<HttpClientFactory>().withTimeout(any()) }
        verify(exactly = 0) { httpClient.newCall(any()) }
        verify(exactly = 0) { call.execute() }
    }

}