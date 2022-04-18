package jp.toastkid.lib.preference

import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.widget.TextView
import androidx.annotation.ColorInt

/**
 * Color pair of toolbar and so on...
 *
 * @author toastkidjp
 */
class ColorPair(
        @param:ColorInt private val bgColor:   Int,
        @param:ColorInt private val fontColor: Int
) {

    @ColorInt fun bgColor():   Int = bgColor

    @ColorInt fun fontColor(): Int = fontColor

    /**
     * Set background and text color.
     *
     * @param tv
     */
    fun setTo(tv: TextView?) {
        tv?.setBackgroundColor(bgColor)
        tv?.setTextColor(fontColor)
        tv?.compoundDrawables?.forEach {
            it?.colorFilter = PorterDuffColorFilter(fontColor, PorterDuff.Mode.SRC_IN)
        }
    }

}
