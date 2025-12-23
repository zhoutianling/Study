package com.zero.health.ui.activity

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.zero.base.activity.BaseActivity
import com.zero.health.databinding.ActivityAlarmRemindBinding
import com.zero.health.databinding.ItemProcessBinding
import com.zero.health.helper.ReminderManager
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
    override fun initView() {
        adapter = ProcessAdapter()
        binding.recyclerView.adapter = adapter
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
    }

    override fun initData() {
        val monitor = ProcessMonitor(2000)
        monitor.start()

        lifecycleScope.launch {
            while (isActive) {
                val list = withContext(Dispatchers.IO) {
                    monitor.buildUiList()
                }
                adapter.submitList(list)
                delay(2000)
            }
        }
    }


    override fun addListener() {
        binding.tvAddRemind.setOnClickListener {
            ReminderManager.scheduleReminders(this, 2)
        }
    }

    class ProcessAdapter : ListAdapter<ProcessMonitor.ProcessUiModel, ProcessAdapter.VH>(Diff) {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            val binding = ItemProcessBinding.inflate(LayoutInflater.from(parent.context), parent,
                false)
            return VH(binding)
        }

        override fun onBindViewHolder(holder: VH, position: Int) {
            holder.bind(getItem(position))
        }

        class VH(private val binding: ItemProcessBinding) : RecyclerView.ViewHolder(binding.root) {

            fun bind(item: ProcessMonitor.ProcessUiModel) {
                binding.title.text = "PID ${item.pid}  ${item.packageName}"

                val status = if (item.isRunning) "RUNNING" else "ENDED"
                val alive = formatTime(item.aliveSeconds)

                binding.subtitle.text = "存活：$alive   状态：$status"

                binding.root.alpha = if (item.isRunning) 1f else 0.5f
            }

            private fun formatTime(sec: Double): String {
                val s = sec.toInt()
                val m = s / 60
                val r = s % 60
                return "%02d:%02d".format(m, r)
            }
        }

        companion object {
            val Diff = object : DiffUtil.ItemCallback<ProcessMonitor.ProcessUiModel>() {
                override fun areItemsTheSame(a: ProcessMonitor.ProcessUiModel,
                                             b: ProcessMonitor.ProcessUiModel) = a.pid == b.pid

                override fun areContentsTheSame(a: ProcessMonitor.ProcessUiModel,
                                                b: ProcessMonitor.ProcessUiModel) = a == b
            }
        }
    }

}