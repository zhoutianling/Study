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
        binding.tvAddRemind.setOnClickListener {
            viewModel.load()
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
                binding.tvPid.text = "PID ${item.pid}  ${item.packageName}"

                val status = if (item.isRunning) "RUNNING" else "ENDED"
                val alive = formatTime(item.aliveSeconds)

                binding.tvSubtitle.text = "存活：$alive   状态：$status"

                binding.tvRestartCount.text = "重启次数：${item.restartCount}"

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
            val Diff = object : DiffUtil.ItemCallback<ProcessUiModel>() {
                override fun areItemsTheSame(a: ProcessUiModel, b: ProcessUiModel) = a.pid == b.pid

                override fun areContentsTheSame(a: ProcessUiModel, b: ProcessUiModel) = a == b
            }
        }
    }

}