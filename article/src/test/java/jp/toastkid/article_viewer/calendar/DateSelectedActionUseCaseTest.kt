package jp.toastkid.article_viewer.calendar

import io.mockk.MockKAnnotations
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockkConstructor
import io.mockk.unmockkAll
import jp.toastkid.article_viewer.article.Article
import jp.toastkid.article_viewer.article.ArticleRepository
import jp.toastkid.lib.ContentViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * @author toastkidjp
 */
class DateSelectedActionUseCaseTest {

    @MockK
    private lateinit var repository: ArticleRepository

    @MockK
    private lateinit var viewModel: ContentViewModel

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        Dispatchers.setMain(Dispatchers.Unconfined)
    }

    @ExperimentalCoroutinesApi
    @Test
    fun test() {
        every { repository.findFirst(any()) }.answers { Article(1).also { it.title = "test" } }
        every { viewModel.newArticle(any()) }.answers { Unit }

        mockkConstructor(TitleFilterGenerator::class)
        every { anyConstructed<TitleFilterGenerator>().invoke(any(), any(), any()) }.answers { "test" }

        val dateSelectedActionService = DateSelectedActionUseCase(repository, viewModel)
        dateSelectedActionService.invoke(2020, 0, 22)

        coVerify(exactly = 1) { repository.findFirst(any()) }
        coVerify(exactly = 1) { viewModel.newArticle(any()) }
        coVerify(exactly = 1) { anyConstructed<TitleFilterGenerator>().invoke(any(), any(), any()) }
    }

    @After
    fun tearDown() {
        unmockkAll()
        Dispatchers.resetMain()
    }

}