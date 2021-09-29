package jp.toastkid.yobidashi.settings.background.load

import android.app.Activity
import android.graphics.Bitmap
import android.net.Uri
import android.view.View
import androidx.core.graphics.drawable.toBitmap
import androidx.fragment.app.FragmentActivity
import coil.imageLoader
import coil.request.ImageRequest
import jp.toastkid.lib.preference.ColorPair
import jp.toastkid.lib.preference.PreferenceApplier
import jp.toastkid.lib.storage.FilesDir
import jp.toastkid.lib.window.WindowRectCalculatorCompat
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.libs.Toaster
import jp.toastkid.yobidashi.settings.background.ImageDialogFragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.IOException

/**
 * Action of loaded new background image.
 *
 * @param uri Image file URI.
 * @param parent Snackbar's parent.
 * @param colorPair Color pair.
 * @param onLoadedAction On loaded action.
 *
 * @author toastkidjp
 */
internal class LoadedAction (
        private val uri: Uri?,
        private val parent: View,
        private val colorPair: ColorPair,
        private val onLoadedAction: () -> Unit,
        private val fileDir: String
) {

    /**
     * For fixing rotated image.
     */
    private val rotatedImageFixing = RotatedImageFixing()

    /**
     * Invoke action.
     */
    operator fun invoke() {
        if (uri == null) {
            return
        }

        val context = parent.context

        CoroutineScope(Dispatchers.Main).launch {
            val bitmap = try {
                withContext(Dispatchers.IO) {
                    val image = context.imageLoader
                            .execute(ImageRequest.Builder(context).data(uri).build())
                            .drawable
                            ?.toBitmap()

                    val fixedImage = rotatedImageFixing(context.contentResolver, image, uri)
                    fixedImage?.let {
                        val displaySize = WindowRectCalculatorCompat().invoke(context as? Activity) ?: return@let
                        ImageStoreService(
                                FilesDir(context, fileDir),
                                PreferenceApplier(context)
                        )(it, uri, displaySize)
                    }
                    fixedImage
                }
            } catch (e: IOException) {
                Timber.e(e)
                informFailed()
                return@launch
            }

            onLoadedAction()
            bitmap?.let { informDone(it) }
        }
    }

    /**
     * Inform failed.
     */
    private fun informFailed() {
        Toaster.snackShort(parent, R.string.message_failed_read_image, colorPair)
    }

    /**
     * Inform done with action.
     *
     * @param image
     */
    private fun informDone(image: Bitmap) {
        Toaster.snackLong(
                parent,
                R.string.message_done_set_image,
                R.string.display,
                { v ->
                    val viewContext = v.context
                    if (viewContext is FragmentActivity) {
                        ImageDialogFragment.withBitmap(image)
                                .show(
                                        viewContext.supportFragmentManager,
                                        ImageDialogFragment::class.java.simpleName
                                )
                    }
                },
                colorPair
        )
    }

}
