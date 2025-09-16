package com.zero.study.provider

import android.content.ContentProvider
import android.content.ContentValues
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import android.content.UriMatcher
import android.database.Cursor
import android.database.MatrixCursor
import android.net.Uri
import android.util.Log

class HookSwitchProvider : ContentProvider() {
    companion object {
        const val AUTHORITY = "com.zero.study.HookSwitchProvider"
        const val PATH_SWITCH = "hook_enabled"
        private const val CODE_SWITCH = 1
        private val uriMatcher = UriMatcher(UriMatcher.NO_MATCH).apply {
            addURI(AUTHORITY, PATH_SWITCH, CODE_SWITCH) // 匹配URI
        }
    }

    private lateinit var sp: SharedPreferences

    override fun onCreate(): Boolean {
        context?.let {
            sp = it.getSharedPreferences("XposedHookLocal", MODE_PRIVATE)
            Log.d("zzz", "HookSwitchProvider onCreate: $it")
        }
        return true
    }

    override fun query(
        uri: Uri,
        projection: Array<String>?,
        selection: String?,
        selectionArgs: Array<String>?,
        sortOrder: String?
    ): Cursor? {
        Log.d("zzz", "HookSwitchProvider query: $uri")
        return when (uriMatcher.match(uri)) {
            CODE_SWITCH -> {
                // 确保 sp 已经初始化
                if (!::sp.isInitialized) {
                    context?.let {
                        sp = it.getSharedPreferences("XposedHookLocal", MODE_PRIVATE)
                    }
                }

                val isEnabled = sp.getBoolean("isHookEnabled", false)
                Log.d("zzz", "HookSwitchProvider query result: $isEnabled")
                val cursor = MatrixCursor(arrayOf("is_enabled"))
                cursor.addRow(arrayOf(if (isEnabled) 1 else 0))
                cursor
            }

            else -> {
                Log.e("zzz", "HookSwitchProvider unknown URI: $uri")
                throw IllegalArgumentException("未知URI: $uri")
            }
        }
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? = null
    override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<String>?): Int =
        0

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?): Int = 0
    override fun getType(uri: Uri): String? = null
}