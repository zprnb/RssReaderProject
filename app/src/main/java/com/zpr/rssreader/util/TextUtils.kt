package com.zpr.rssreader.util

import android.text.Html

object TextUtils {

    fun htmlToPlainText(value: String?): String {
        if (value.isNullOrBlank()) return ""
        return Html.fromHtml(value, Html.FROM_HTML_MODE_LEGACY).toString().trim()
    }
}
