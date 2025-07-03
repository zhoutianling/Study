package com.zero.base.activity

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebView
import com.zero.library_base.databinding.ActivityWebBinding

class WebActivity : BaseActivity<ActivityWebBinding>(ActivityWebBinding::inflate) {


    override fun initView() {
        val webTitle = intent.getStringExtra(EXTRA_WEB_TITLE)
        val webUrl = intent.getStringExtra(EXTRA_WEB_URL)
        binding.tvTitle.text = webTitle
        binding.tvTitle.isSelected = true
        if (webUrl != null) {
            binding.webView.loadUrl(webUrl)
        }
    }

    override fun initData() {
    }

    override fun addListener() {
        binding.ivBack.setOnClickListener { v: View? -> finish() }
        binding.webView.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView, newProgress: Int) {
                if (newProgress == 100) {
                    Log.d("zzz", "onProgressChanged:$newProgress ")
                }
            }
        }
    }


    override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_BACK -> {
                if (binding.webView.canGoBack()) {
                    binding.webView.goBack()
                } else {
                    finish()
                }
            }
        }
        return super.onKeyUp(keyCode, event)
    }

    override fun onDestroy() {
        binding.webView.clearCache(true)
        binding.webView.removeAllViews()
        binding.webView.destroy()
        super.onDestroy()
    }

    companion object {
        const val EXTRA_WEB_TITLE: String = "extra_web_title"

        const val EXTRA_WEB_URL: String = "extra_web_url"

        fun startWebView(context: Context, title: String? = "", url: String? = "") {
            context.startActivity(Intent(context, WebActivity::class.java).apply {
                putExtra(EXTRA_WEB_TITLE, title)
                putExtra(EXTRA_WEB_URL, url)
            })
        }
    }
}
