package jp.toastkid.web.bookmark

import jp.toastkid.yobidashi.browser.bookmark.model.Bookmark

/**
 * Bookmark exporter.
 *
 * @author toastkidjp
 */
class Exporter(private val bookmarks: Iterable<Bookmark?>) {

    operator fun invoke(): String {
        val builder = StringBuilder()
                .append("<!DOCTYPE NETSCAPE-Bookmark-file-1>\n")
                .append("<META HTTP-EQUIV=\"Content-Type\" CONTENT=\"text/html; charset=UTF-8\">\n")
                .append("<TITLE>Bookmarks</TITLE>\n")
                .append("<H1>ブックマークメニュー</H1><DL><p>\n")
        bookmarks.filter { "root" == it?.parent }
                .forEach {
                    if (it?.folder ?: false) { convertDirectory(builder, it) }
                    else { convertBookmarkItem(builder, it) }
                }
        builder.append("</DL>")
        return builder.toString()
    }

    private fun convertDirectory(builder: StringBuilder, item: Bookmark?) {
        item?.let { itemBookmark ->
            builder.append("<DT><H3>${itemBookmark.title}</H3>\n")
                    .append("<DL><p>\n")
            getDirBookmarks(itemBookmark.title).forEach {
                if (it == null) {
                    return@forEach
                }
                if (it.folder) {
                    convertDirectory(builder, it)
                } else {
                    convertBookmarkItem(builder, it)
                }
            }
            builder.append("</DL></p>\n")
        }
    }

    private fun convertBookmarkItem(builder: StringBuilder, item: Bookmark?) {
        item?.let { builder.append("<DT><A href='${it.url}'>${it.title}</A>\n") }
    }

    private fun getDirBookmarks(parent: String): List<Bookmark?> =
            bookmarks.filter { parent == it?.parent }
}