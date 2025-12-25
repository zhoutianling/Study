package com.zero.health.ui.activity

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
                val message = "成功杀死 $successCount / $totalCount 个进程"
                // 可以使用 Toast 或其他方式显示结果
                android.widget.Toast.makeText(this@AlarmRemindActivity, message,
                    android.widget.Toast.LENGTH_SHORT).show()
            } else {
                android.widget.Toast.makeText(this@AlarmRemindActivity, "没有可杀死的进程",
                    android.widget.Toast.LENGTH_SHORT).show()
            }
        }
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