package com.zero.study.ui.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.zero.study.databinding.ActivityHelpOverlayBinding

/**
 * @author Admin
 */
class PermissionOverlayHelpActivity : AppCompatActivity() {

    private val binding: ActivityHelpOverlayBinding by lazy {
        ActivityHelpOverlayBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        binding.root.setOnClickListener {
            finish()
        }
    }

    companion object {
        @JvmStatic
        fun start(activity: Activity) {
            val intent = Intent()
            intent.setClass(activity, PermissionOverlayHelpActivity::class.java)
            activity.startActivity(intent)
        }
    }
}
