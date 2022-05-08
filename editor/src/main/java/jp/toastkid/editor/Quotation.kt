package jp.toastkid.editor

/**
 * Converter of quotation style(Markdown).
 *
 * @author toastkidjp
 */
class Quotation {

    /**
     * Line separator.
     */
    private val lineSeparator = System.lineSeparator()

    /**
     * Invoke quotation function.
     *
     * @param str Nullable [CharSequence]
     */
    operator fun invoke(str: CharSequence?): CharSequence? {
        if (str.isNullOrEmpty()) {
            return str
        }
        return str.split(lineSeparator)
                .asSequence()
                .map { "> $it" }
                .reduce { str1, str2 -> str1 + lineSeparator + str2 }
    }
}