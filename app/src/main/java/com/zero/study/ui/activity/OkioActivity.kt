package com.zero.study.ui.activity

import androidx.fragment.app.Fragment
import com.zero.base.activity.BaseActivity
import com.zero.base.ext.switchFragment
import com.zero.study.R
import com.zero.study.databinding.ActivityNetworkBinding
import com.zero.study.ui.fragment.ReadFragment
import com.zero.study.ui.fragment.WriteFragment

/**
 * @date:2024/9/23 19:22
 * @path:com.zero.study.ui.activity.NetWorkActivity
 */
class OkioActivity : BaseActivity<ActivityNetworkBinding>(ActivityNetworkBinding::inflate) {

    private val mReadFragment by lazy { ReadFragment.newInstance() }
    private val mWriteFragment by lazy { WriteFragment.newInstance() }

    override fun initView() {
        binding.bottomNavigationView.selectedItemId = R.id.menu_write
        switchFragment(R.id.fl_container, mWriteFragment)
    }

    override fun initData() {
    }

    override fun addListener() {
        binding.bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.menu_back -> {
                    this@OkioActivity.finish()
                }

                R.id.menu_write -> {
                    switchFragment(R.id.fl_container, mWriteFragment)
                }

                R.id.menu_read -> {
                    switchFragment(R.id.fl_container, mReadFragment)
                }
            }
            true
        }
    }

}