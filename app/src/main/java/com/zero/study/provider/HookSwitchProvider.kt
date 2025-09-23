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
        const val PATH_SWITCH = "hook_enabled"
        private const val CODE_SWITCH = 1
    }

    private lateinit var sp: SharedPreferences
    private var uriMatcher: UriMatcher? = null
    private var authority: String? = null

    override fun onCreate(): Boolean {
        context?.let {
            authority = it.packageName + ".HookSwitchProvider"
            uriMatcher = UriMatcher(UriMatcher.NO_MATCH).apply {
                addURI(authority, PATH_SWITCH, CODE_SWITCH)
            }
            sp = it.getSharedPreferences("XposedHookLocal", MODE_PRIVATE)
        }
        return true
    }

    override fun query(uri: Uri, projection: Array<String>?, selection: String?, selectionArgs: Array<String>?, sortOrder: String?): Cursor? {
        Log.d("zzz", "query: $uri")
        val matchResult = uriMatcher?.match(uri)
        if (matchResult == CODE_SWITCH) {
            val isEnabled = sp.getBoolean("isHookEnabled", false)
            val cursor = MatrixCursor(arrayOf("is_enabled"))
            cursor.addRow(arrayOf(if (isEnabled) 1 else 0))
            return cursor
        } else {
            throw IllegalArgumentException("未知URI: $uri")
        }
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? = null
    override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<String>?): Int =
        0

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?): Int = 0
    override fun getType(uri: Uri): String? = null
}