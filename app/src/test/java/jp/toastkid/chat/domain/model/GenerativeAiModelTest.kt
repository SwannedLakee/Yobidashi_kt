package jp.toastkid.chat.domain.model

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GenerativeAiModelTest {

    @Test
    fun image() {
        assertFalse(GenerativeAiModel.GEMINI_2_5_FLASH.image())

        assertTrue(
            GenerativeAiModel.entries
                .none(GenerativeAiModel::image)
        )
    }

}
