/*
 * Copyright (c) 2021 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */

package jp.toastkid.search.history

import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.unmockkAll
import io.mockk.verify
import jp.toastkid.data.repository.factory.RepositoryFactory
import jp.toastkid.yobidashi.search.history.SearchHistoryRepository
import kotlinx.coroutines.Dispatchers
import org.junit.After
import org.junit.Before
import org.junit.Test

class SearchHistoryInsertionTest {

    private lateinit var searchHistoryInsertion: SearchHistoryInsertion

    @MockK
    private lateinit var repository: SearchHistoryRepository

    @Before
    fun setUp() {
        MockKAnnotations.init(this)

        mockkConstructor(RepositoryFactory::class)
        every { anyConstructed<RepositoryFactory>().searchHistoryRepository(any()) }.returns(repository)
        coEvery { repository.insert(any()) }.just(Runs)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun testInsert() {
        searchHistoryInsertion = SearchHistoryInsertion.make(
                mockk(),
                "test-category",
                "test-query",
            Dispatchers.Unconfined
        )

        searchHistoryInsertion.insert()

        verify(exactly = 1) { anyConstructed<RepositoryFactory>().searchHistoryRepository(any()) }
        coVerify(exactly = 1) { repository.insert(any()) }
    }

    @Test
    fun testInsertWithEmptyCategory() {
        searchHistoryInsertion = SearchHistoryInsertion.make(
                mockk(),
                "",
                "test-query",
            Dispatchers.Unconfined
        )

        searchHistoryInsertion.insert()

        verify(exactly = 1) { anyConstructed<RepositoryFactory>().searchHistoryRepository(any()) }
        coVerify(exactly = 0) { repository.insert(any()) }
    }

    @Test
    fun testInsertWithEmptyQuery() {
        searchHistoryInsertion = SearchHistoryInsertion.make(
                mockk(),
                "test-category",
                "",
            Dispatchers.Unconfined
        )

        searchHistoryInsertion.insert()

        verify(exactly = 1) { anyConstructed<RepositoryFactory>().searchHistoryRepository(any()) }
        coVerify(exactly = 0) { repository.insert(any()) }
    }

}