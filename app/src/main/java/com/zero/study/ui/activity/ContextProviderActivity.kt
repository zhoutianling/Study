package com.zero.study.ui.activity

import android.content.SharedPreferences
import android.widget.Toast
import com.zero.base.activity.BaseActivity
import com.zero.study.databinding.ActivityContentProviderBinding
import androidx.core.content.edit

/**
 * @date:2025/8/7 21:19
 * @path:com.zero.health.ui.activity.AlarmRemindActivity
 */
class ContextProviderActivity : BaseActivity<ActivityContentProviderBinding>(ActivityContentProviderBinding::inflate) {
    private lateinit var sp: SharedPreferences
    private var isHookEnabled = false
    override fun initView() {
        // 确保使用与 HookSwitchProvider 相同的文件名和键名
        sp = getSharedPreferences("XposedHookLocal", MODE_PRIVATE)
        isHookEnabled = sp.getBoolean("isHookEnabled", false)
        updateButtonText()
        binding.hookSwitch.setOnCheckedChangeListener { buttonView, isChecked ->
            if (buttonView.isPressed) {
                isHookEnabled = !isHookEnabled
                // 使用 edit 扩展函数安全地更新 SharedPreferences
                sp.edit { putBoolean("isHookEnabled", isHookEnabled) }
                updateButtonText()
                val tip = if (isHookEnabled) "Hook已启用！重启目标应用生效" else "Hook已禁用"
                Toast.makeText(this, tip, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateButtonText() {
        val text = if (isHookEnabled) "当前：Hook已启用（点击禁用）" else "当前：Hook已禁用（点击启用）"
        binding.tvState.text = text
        binding.hookSwitch.isChecked = isHookEnabled
    }

    override fun initData() {
    }

    override fun addListener() {
    }
}