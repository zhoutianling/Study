package com.zero.health.ui.activity

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.zero.base.activity.BaseActivity
import com.zero.health.databinding.ActivityAlarmRemindBinding
import com.zero.health.databinding.ItemProcessBinding
import com.zero.health.service.MemoryMonitorOverlayService
import com.zero.health.state.UiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * @date:2025/8/7 21:19
 * @path:com.zero.health.ui.activity.AlarmRemindActivity
 */
class AlarmRemindActivity :
    BaseActivity<ActivityAlarmRemindBinding>(ActivityAlarmRemindBinding::inflate) {
    private lateinit var adapter: ProcessAdapter
    private val viewModel: AlarmRemindViewModel by lazy {
        ViewModelProvider(this)[AlarmRemindViewModel::class.java]
    }

    override fun initView() {
        adapter = ProcessAdapter()
        binding.recyclerView.adapter = adapter
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
    }

    override fun initData() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    when (state) {
                        UiState.Loading -> showLoading()

                        is UiState.Success -> {
                            hideLoading()
                            adapter.submitList(state.data)
                        }

                        is UiState.Failed -> {
                            hideLoading()
                        }

                        UiState.Idle -> Unit
                    }
                }
            }
        }

    }


    override fun addListener() {
        binding.tvStart.setOnClickListener {
            viewModel.load()
        }

        binding.tvKillAll.setOnClickListener {
            killAllProcesses()
        }

        binding.tvClear.setOnClickListener {
            forceMemoryReclaim()
        }
        
        binding.tvOverlay.setOnClickListener {
            toggleOverlayService()
        }
    }

    private fun killAllProcesses() {
        lifecycleScope.launch {
            val results = withContext(Dispatchers.IO) {
                viewModel.killAllTargetProcesses()
            }

            // 显示结果
            val successCount = results.count { it.value }
            val totalCount = results.size

            if (totalCount > 0) {
                val message = "已尝试杀死 $totalCount 个进程，并强制停止目标应用\n(结果可能因应用自启机制而有限)"
                // 可以使用 Toast 或其他方式显示结果
                android.widget.Toast.makeText(this@AlarmRemindActivity, message,
                    android.widget.Toast.LENGTH_LONG).show()
            } else {
                android.widget.Toast.makeText(this@AlarmRemindActivity, "没有可杀死的进程",
                    android.widget.Toast.LENGTH_SHORT).show()
            }

            // 刷新列表以显示最新状态
            viewModel.load()
        }
    }
    
    private fun forceMemoryReclaim() {
        lifecycleScope.launch {
            val success = withContext(Dispatchers.IO) {
                viewModel.forceMemoryReclaim()
            }
            
            if (success) {
                android.widget.Toast.makeText(this@AlarmRemindActivity, "已触发系统内存回收，请观察进程变化",
                    android.widget.Toast.LENGTH_SHORT).show()
                
                // 刷新列表以观察内存回收后的进程状态
                viewModel.load()
            } else {
                android.widget.Toast.makeText(this@AlarmRemindActivity, "内存回收执行失败，请检查ROOT权限",
                    android.widget.Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun toggleOverlayService() {
        if (Settings.canDrawOverlays(this)) {
            // 如果已有悬浮窗权限，启动服务
            val intent = Intent(this, MemoryMonitorOverlayService::class.java)
            if (isServiceRunning(MemoryMonitorOverlayService::class.java)) {
                // 如果服务已在运行，则停止它
                stopService(intent)
                android.widget.Toast.makeText(this, "已停止内存监控悬浮窗", android.widget.Toast.LENGTH_SHORT).show()
            } else {
                // 如果服务未运行，则启动它
                startService(intent)
                android.widget.Toast.makeText(this, "已启动内存监控悬浮窗", android.widget.Toast.LENGTH_SHORT).show()
            }
        } else {
            // 请求悬浮窗权限
            val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))
            startActivity(intent)
            android.widget.Toast.makeText(this, "请授予悬浮窗权限以使用内存监控功能", android.widget.Toast.LENGTH_LONG).show()
        }
    }
    
    private fun <T> isServiceRunning(serviceClass: Class<T>): Boolean {
        val manager = getSystemService(android.app.Service.ACTIVITY_SERVICE) as android.app.ActivityManager
        for (service in manager.getRunningServices(Int.MAX_VALUE)) {
            if (serviceClass.name == service.service.className) {
                return true
            }
        }
        return false
    }


    class ProcessAdapter : ListAdapter<ProcessUiModel, ProcessAdapter.VH>(Diff) {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            val binding = ItemProcessBinding.inflate(LayoutInflater.from(parent.context), parent,
                false)
            return VH(binding)
        }

        override fun onBindViewHolder(holder: VH, position: Int) {
            holder.bind(getItem(position))
        }

        class VH(private val binding: ItemProcessBinding) : RecyclerView.ViewHolder(binding.root) {

            fun bind(item: ProcessUiModel) {
                binding.tvPid.text = "PID ${item.pid}  包名：${item.packageName}"

                val status = if (item.isRunning) "RUNNING" else "ENDED"
                val alive = formatTime(item.aliveSeconds)

                binding.tvSubtitle.text = "存活：$alive   状态：$status"

                binding.tvRestartCount.text = "重启次数：${item.restartCount}"

                binding.root.alpha = if (item.isRunning) 1f else 0.5f
            }

            private fun formatTime(sec: Double): String {
                val totalSeconds = sec.toInt()
                val hours = totalSeconds / 3600
                val minutes = (totalSeconds % 3600) / 60
                val seconds = totalSeconds % 60

                return if (hours > 0) {
                    "%02d:%02d:%02d".format(hours, minutes, seconds)
                } else {
                    "%02d:%02d".format(minutes, seconds)
                }
            }
        }

        companion object {
            val Diff = object : DiffUtil.ItemCallback<ProcessUiModel>() {
                override fun areItemsTheSame(a: ProcessUiModel, b: ProcessUiModel) = a.pid == b.pid

                override fun areContentsTheSame(a: ProcessUiModel, b: ProcessUiModel) = a == b
            }
        }
    }

}