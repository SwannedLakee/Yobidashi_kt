package jp.toastkid.pdf

import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer

/**
 * PDF content image factory. This class can use only Android L and upper L.
 *
 * @author toastkidjp
 */
class PdfImageFactory {

    /**
     * Invoke action.
     *
     * @param currentPage current PDF page
     * @return non-null bitmap
     */
    operator fun invoke(currentPage: PdfRenderer.Page): Bitmap {
        val bitmap: Bitmap = Bitmap.createBitmap(
                currentPage.width * 2, currentPage.height * 2, Bitmap.Config.ARGB_8888)
        currentPage.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
        currentPage.close()
        return bitmap
    }

}