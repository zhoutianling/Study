package com.zero.study.ui.activity

import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout.OnRefreshListener
import com.zero.base.util.PingUtils
import com.zero.base.activity.BaseActivity
import com.zero.study.databinding.ActivityRecyclerViewBinding
import com.zero.study.ui.adapter.LineAdapter
import com.zero.study.ui.model.AskViewModel
import com.zero.study.ui.model.HomeViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.URL

class RecyclerViewActivity : BaseActivity<ActivityRecyclerViewBinding>(ActivityRecyclerViewBinding::inflate), OnRefreshListener {
    private val mAdapter: LineAdapter by lazy {
        LineAdapter()
    }
    private val viewModel: AskViewModel by lazy {
        ViewModelProvider(this)[AskViewModel::class.java]
    }

    override fun initView() {
        binding.recyclerView.adapter = mAdapter

    }

    override fun initData() {
        binding.swipeLayout.isRefreshing = true
        viewModel.fetchAskPageList().apply {
            viewModel.articlePageListLiveData.observe(this@RecyclerViewActivity) {
                mAdapter.submitList(it?.datas)
                binding.swipeLayout.isRefreshing = false
                lifecycleScope.launch(Dispatchers.IO) {
                    it?.let {
                        it.datas.forEachIndexed { index, article ->
                            article.delay = PingUtils.pingDelay(URL(article.link).host)
                            withContext(Dispatchers.Main) {
                                mAdapter.notifyItemChanged(index, "ping")
                            }
                        }
                    }
                }
            }
        }

    }

    override fun addListener() {
        binding.swipeLayout.setOnRefreshListener(this)
    }


    override fun onRefresh() {
        initData()
    }


}