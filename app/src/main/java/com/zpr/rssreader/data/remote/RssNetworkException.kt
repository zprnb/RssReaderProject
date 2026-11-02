package com.zpr.rssreader.data.remote

class RssNetworkException(
    message: String,
    cause: Throwable? = null
) : Exception(message, cause)
