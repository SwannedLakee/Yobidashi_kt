package jp.toastkid.lib

import android.annotation.TargetApi
import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import java.io.File


/**
 * File object extractor from [Uri].
 *
 * @author toastkidjp
 */
class FileExtractorFromUri {

    /**
     * Extract [File] object from [Uri]. This method is nullable.
     *
     * @param context [Context]
     * @param uri [Uri]
     * @return [File] (Nullable)
     */
    operator fun invoke(context: Context, uri: Uri): String? {
        when {
            DocumentsContract.isDocumentUri(context, uri) -> {
                return getForKitKat(context, uri)
            }
            "content".equals(uri.scheme, ignoreCase = true) -> {
                return getDataColumn(context, uri, null, null)
            }
            "file".equals(uri.scheme, ignoreCase = true) -> {
                return uri.path
            }
            else -> return null
        }
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private fun getForKitKat(context: Context, uri: Uri): String? {
        when (uri.authority) {
            "com.android.externalstorage.documents" -> {// ExternalStorageProvider
                val docId = DocumentsContract.getDocumentId(uri)
                val split = docId.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                val type = split[0]
                return when {
                    "primary".equals(type, ignoreCase = true) ->
                        context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)?.resolve(split[1])?.absolutePath
                    "home".equals(type, ignoreCase = true) ->
                        context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)?.resolve("documents/${split[1]}")?.absolutePath
                    else ->
                        "/storage/$type/${split[1]}"
                }
            }
            "com.android.providers.downloads.documents" -> {// DownloadsProvider
                val id = DocumentsContract.getDocumentId(uri)
                val contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), java.lang.Long.valueOf(id))
                return getDataColumn(context, contentUri, null, null)
            }
            "com.android.providers.media.documents" -> {// MediaProvider
                val docId = DocumentsContract.getDocumentId(uri)
                val split = docId.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                val contentUri: Uri? = MediaStore.Files.getContentUri("external")
                val selection = "_id=?"
                val selectionArgs = arrayOf(split[1])
                return getDataColumn(context, contentUri, selection, selectionArgs)
            }
            else -> return ""
        }
    }

    private fun getDataColumn(
            context: Context,
            uri: Uri?,
            selection: String?,
            selectionArgs: Array<String>?
    ): String? {
        if (uri == null) {
            return null
        }

        var cursor: Cursor? = null
        val projection = arrayOf(MediaStore.Files.FileColumns.DATA)
        try {
            cursor = context.contentResolver.query(
                    uri, projection, selection, selectionArgs, null)
            if (cursor != null && cursor.moveToFirst()) {
                val columnIndex = cursor.getColumnIndexOrThrow(projection[0])
                return cursor.getString(columnIndex)
            }
        } finally {
            cursor?.close()
        }
        return null
    }
}