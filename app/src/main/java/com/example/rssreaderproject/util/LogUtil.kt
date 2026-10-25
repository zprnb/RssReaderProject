package com.example.rssreaderproject.util

import android.util.Log

/**
 * 日志工具类：统一日志 TAG，方便排查与现场演示
 */
object LogUtil {

    private const val TAG = Constants.TAG_PREFIX

    fun d(msg: String) = Log.d(TAG, msg)

    fun i(msg: String) = Log.i(TAG, msg)

    fun e(msg: String) = Log.e(TAG, msg)
}
