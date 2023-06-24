package jp.toastkid.web.tls

import android.content.Context
import android.net.http.SslCertificate
import android.net.http.SslError
import android.text.format.DateFormat
import jp.toastkid.web.R
import java.util.Date

/**
 * @author toastkidjp
 */
class TlsErrorMessageGenerator {

    operator fun invoke(context: Context, error: SslError?): String {
        if (error == null) {
            return ""
        }

        val cert: SslCertificate = error.certificate

        return context.getString(R.string.message_ssl_error_first_line) +
                makeCause(error, context, cert) +
                System.lineSeparator() +
                "URL: ${error.url}"
    }

    private fun makeCause(error: SslError, context: Context, cert: SslCertificate): String {
        return when (error.primaryError) {
            SslError.SSL_EXPIRED ->
                context.getString(R.string.message_ssl_error_expired) + dateToString(cert.validNotAfterDate)
            SslError.SSL_IDMISMATCH ->
                context.getString(R.string.message_ssl_error_id_mismatch) + cert.issuedTo.cName
            SslError.SSL_NOTYETVALID ->
                context.getString(R.string.message_ssl_error_not_yet_valid) + dateToString(cert.validNotBeforeDate)
            SslError.SSL_UNTRUSTED ->
                context.getString(R.string.message_ssl_error_untrusted) + cert.issuedBy.dName
            else ->
                context.getString(R.string.message_ssl_error_unknown)
        }
    }

    private fun dateToString(date: Date?) =
        if (date == null) "" else DateFormat.format("yyyy/MM/dd HH:mm:ss", date)

}