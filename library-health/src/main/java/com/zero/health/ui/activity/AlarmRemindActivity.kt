package com.zero.health.ui.activity

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.appcompat.app.AlertDialog
import com.zero.base.activity.BaseActivity
import com.zero.base.ext.hasExactAlarmPermission
import com.zero.base.ext.toast
import com.zero.health.R
import com.zero.health.databinding.ActivityAlarmRemindBinding
import com.zero.health.helper.AlarmHelper
import java.util.Calendar

/**
 * @date:2025/8/7 21:19
 * @path:com.zero.health.ui.activity.AlarmRemindActivity
 */
class AlarmRemindActivity : BaseActivity<ActivityAlarmRemindBinding>(ActivityAlarmRemindBinding::inflate) {
    private val alarmHelper by lazy {
        AlarmHelper(this)
    }

    override fun initView() {
        binding.timePicker.setIs24HourView(true)
        binding.tvAddRemind.setOnClickListener {
            val calendar = Calendar.getInstance().apply {
                add(Calendar.SECOND, 10)
            }
            alarmHelper.setUpAlarm(101, "test", calendar)
        }
    }

    override fun initData() {
        if (this.hasExactAlarmPermission() != true) {
            AlertDialog.Builder(this).setTitle(getString(R.string.set_exact_alarm_permission_title)).setMessage(getString(R.string.set_exact_alarm_permission_text)).setPositiveButton(getString(R.string.health_dialog_confirm)) { _, _ ->
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.fromParts("package", packageName, null)
                }
                startActivity(intent)
            }.show()
        } else {
            this.toast("hasExactAlarmPermission")
        }
    }

    override fun addListener() {
    }
}