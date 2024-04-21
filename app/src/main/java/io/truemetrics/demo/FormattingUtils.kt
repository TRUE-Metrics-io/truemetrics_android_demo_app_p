package io.truemetrics.demo

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

object FormattingUtils {

    private val dateTimeFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault()).also {
        it.timeZone = TimeZone.getTimeZone("UTC")
    }

    fun Date.formatUtc(): String {
        return dateTimeFormat.format(this)
    }
}