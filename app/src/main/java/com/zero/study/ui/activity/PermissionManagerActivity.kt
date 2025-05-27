package com.zero.study.ui.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.zero.base.util.PermissionUtils.applyOverlay
import com.zero.base.util.PermissionUtils.hasOverlayPermission
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * @author Admin
 */
class PermissionManagerActivity : AppCompatActivity() {

    private var hasOverlay: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (hasOverlayPermission(this@PermissionManagerActivity)) {
            Toast.makeText(this@PermissionManagerActivity, "Overlay Access", Toast.LENGTH_SHORT).show()
            finishAndRemoveTask()
        } else {
            applyOverlay(this@PermissionManagerActivity)
            lifecycleScope.launch {
                delay(200)
                PermissionOverlayHelpActivity.start(this@PermissionManagerActivity)
            }
            checkPermission()

        }
    }


    private fun checkPermission() {
        lifecycleScope.launch {
            while (!hasOverlay) {
                Log.d("ZZZ", "checkPermission: false")
                if (hasOverlayPermission(this@PermissionManagerActivity)) {
                    Log.d("ZZZ", "checkPermission: true")
                    finishAndRemoveTask()
                    lifecycleScope.cancel()
                    hasOverlay = true
                    break
                }
                delay(200)
            }
        }
    }

    public override fun onRestart() {
        super.onRestart()
        finishAndRemoveTask()
    }

    public override fun onDestroy() {
        super.onDestroy()
        lifecycleScope.cancel()
    }


    companion object {
        fun start(context: Context) {
            val intent = Intent(context, PermissionManagerActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        }
    }
}
