package com.zero.health.ui.activity

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.view.LayoutInflater
import android.view.ViewGroup
import android.app.usage.UsageStatsManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.zero.base.activity.BaseActivity
import com.zero.base.ext.hasExactAlarmPermission
import com.zero.base.ext.toast
import com.zero.health.R
import com.zero.health.databinding.ActivityAlarmRemindBinding
import com.zero.health.databinding.ItemProcessBinding
import com.zero.health.helper.AlarmHelper
import com.zero.health.helper.ReminderManager

/**
 * @date:2025/8/7 21:19
 * @path:com.zero.health.ui.activity.AlarmRemindActivity
 */
class AlarmRemindActivity :
    BaseActivity<ActivityAlarmRemindBinding>(ActivityAlarmRemindBinding::inflate) {

    override fun initView() {

    }

    override fun initData() {
        if (!hasUsageStatsPermission()) {
            showUsageStatsPermissionDialog()
        }
        initProcessList()
    }

    // 检查是否有使用情况访问权限
    private fun hasUsageStatsPermission(): Boolean {
        val usageStatsManager = getSystemService(USAGE_STATS_SERVICE) as UsageStatsManager
        return try {
            usageStatsManager.queryUsageStats(
                UsageStatsManager.INTERVAL_DAILY,
                System.currentTimeMillis() - 1000 * 60, // 1分钟前
                System.currentTimeMillis()
            ).isNotEmpty()
        } catch (e: SecurityException) {
            false
        }
    }

    private fun showUsageStatsPermissionDialog() {
        androidx.appcompat.app.AlertDialog.Builder(this).setTitle("需要使用情况访问权限").setMessage(
            "为了显示其他应用的进程列表，需要您手动授予使用情况访问权限。请在接下来的设置页面中找到并启用本应用的权限。").setPositiveButton(
            "去设置") { _, _ ->
            val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
            startActivity(intent)
        }.setNegativeButton("取消", null).show()
    }

    override fun addListener() {
        binding.tvAddRemind.setOnClickListener {
            ReminderManager.scheduleReminders(this, 2)
        }
    }

    // 获取最近使用的应用包名列表及其使用时长（需要UsageStats权限）
    private fun getRecentAppsUsage(): List<Triple<String, String, Long>> {
        val usageStatsManager = getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val endTime = System.currentTimeMillis()
        val startTime = endTime - (24 * 60 * 60 * 1000) // 24小时内

        val usageStatsList = usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY,
            startTime,
            endTime
        )

        // 计算每个应用的总使用时长
        val appUsageMap = mutableMapOf<String, Long>()
        for (usageStats in usageStatsList) {
            val packageName = usageStats.packageName
            val totalUsageTime = usageStats.totalTimeInForeground

            appUsageMap[packageName] = (appUsageMap[packageName] ?: 0) + totalUsageTime
        }

        val packageManager = packageManager
        return appUsageMap.entries.map { entry ->
            val packageName = entry.key
            val usageTime = entry.value
            val appName = try {
                val appInfo = packageManager.getApplicationInfo(packageName, 0)
                packageManager.getApplicationLabel(appInfo).toString()
            } catch (e: PackageManager.NameNotFoundException) {
                packageName // 如果找不到应用名称，使用包名作为替代
            }
            Triple(packageName, appName, usageTime)
        }.toList()
    }

    // 判断是否为系统应用
    private fun isSystemApp(packageManager: PackageManager, packageName: String): Boolean {
        return try {
            val appInfo = packageManager.getApplicationInfo(packageName, 0)
            (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
        } catch (e: PackageManager.NameNotFoundException) {
            false // 如果找不到应用信息，默认为非系统应用
        }
    }

    // 获取非系统应用的包名列表及使用时长
    fun getNonSystemApps(): List<Triple<String, String, Long>> {
        val packageManager = packageManager
        val allApps = getRecentAppsUsage()
        return allApps.filter { (packageName, _, _) ->
            !isSystemApp(packageManager, packageName)
        }
    }

    // 创建适配器用于显示应用列表
    inner class ProcessAdapter(private val processList: List<Triple<String, String, Long>>) :
        RecyclerView.Adapter<ProcessAdapter.ProcessViewHolder>() {

        inner class ProcessViewHolder(val binding: ItemProcessBinding) :
            RecyclerView.ViewHolder(binding.root) {

            fun bind(packageName: String, appName: String, usageTime: Long) {
                binding.tvProcessName.text = "$appName ($packageName)"
                binding.tvPid.text = "使用时长: ${formatTime(usageTime)}"
                binding.tvUid.text = "" // 不再显示PID和UID，因为使用UsageStats无法直接获取
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProcessViewHolder {
            val binding = ItemProcessBinding.inflate(LayoutInflater.from(parent.context), parent,
                false)
            return ProcessViewHolder(binding)
        }

        override fun onBindViewHolder(holder: ProcessViewHolder, position: Int) {
            val (packageName, appName, usageTime) = processList[position]
            holder.bind(packageName, appName, usageTime)
        }

        override fun getItemCount(): Int = processList.size
    }

    // 格式化时间显示
    private fun formatTime(milliseconds: Long): String {
        val seconds = milliseconds / 1000
        val minutes = seconds / 60
        val hours = minutes / 60

        return if (hours > 0) {
            String.format("%02d:%02d:%02d", hours, minutes % 60, seconds % 60)
        } else {
            String.format("%02d:%02d", minutes % 60, seconds % 60)
        }
    }

    // 初始化应用列表显示
    private fun initProcessList() {
        val nonSystemApps = getNonSystemApps()
        val adapter = ProcessAdapter(nonSystemApps)
        binding.rvProcessList.layoutManager = LinearLayoutManager(this)
        binding.rvProcessList.adapter = adapter
    }
}