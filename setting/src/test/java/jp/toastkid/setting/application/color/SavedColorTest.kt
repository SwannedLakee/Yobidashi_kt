package jp.toastkid.setting.application.color

import android.graphics.Color
import io.mockk.MockKAnnotations
import jp.toastkid.yobidashi.settings.color.SavedColor
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

/**
 * @author toastkidjp
 */
class SavedColorTest {

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
    }

    @Test
    fun test() {
        val savedColor = SavedColor.make(Color.BLACK, Color.WHITE)

        assertEquals(Color.BLACK, savedColor.bgColor)
        assertEquals(Color.WHITE, savedColor.fontColor)
    }
}