package jp.toastkid.web

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.core.net.toUri
import jp.toastkid.lib.storage.FilesDir
import java.io.File

/**
 * Favicon applier.
 *
 * @author toastkidjp
 */
class FaviconApplier(
        context: Context,
        filesDirFactory: (Context, String) -> FilesDir = { c, n -> FilesDir(c, n) }
) {

    /**
     * Favicon file directory.
     */
    private val favicons: FilesDir = filesDirFactory(context, "favicons")

    /**
     * Assign file.
     *
     * @param urlStr URL string
     */
    fun assignFile(urlStr: String): File = favicons.assignNewFile("${urlStr.toUri().host}.png")

    /**
     * Make file path.
     *
     * @param urlStr URL string
     */
    fun makePath(urlStr: String): String = assignFile(urlStr).absolutePath

    fun load(uri: Uri?): Bitmap? {
        if (uri == null) {
            return null
        }

        return BitmapFactory.decodeFile(makePath(uri.toString()))
    }
}