package com.zero.base.ext

import android.content.Context
import com.google.gson.Gson

/**
 * @date:2024/12/11 17:05
 * @path:com.zero.base.ext.GsonExt
 */
inline fun <reified T> Gson.fromJson(json: String): T = fromJson(json, T::class.java)

fun Context.readJson(fileName: String): String =
    assets.open(fileName).bufferedReader().use { it.readText() }

