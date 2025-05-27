package com.zero.study.ui.activity

import android.view.View
import androidx.fragment.app.Fragment
import com.zero.base.activity.BaseActivity
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

    private var mCurrentFragment: Fragment = mWriteFragment
    override fun initView() {
        supportFragmentManager.beginTransaction().replace(R.id.fl_container, mCurrentFragment).commit()
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
                    switchFragment(mWriteFragment)
                }

                R.id.menu_read -> {
                    switchFragment(mReadFragment)
                }
            }
            true
        }
    }

    /**
     * 切换Fragment
     *
     * @param fragment 要切换的Fragment
     */
    private fun switchFragment(fragment: Fragment) {
        if (fragment !== mCurrentFragment) {
            val fragmentTransaction = supportFragmentManager.beginTransaction()
            fragmentTransaction.hide(mCurrentFragment)
            if (!fragment.isAdded) {
                fragmentTransaction.add(R.id.fl_container, fragment, fragment.javaClass.simpleName).show(fragment)
            } else {
                fragmentTransaction.show(fragment)
            }
            fragmentTransaction.commitAllowingStateLoss()
            mCurrentFragment = fragment
        }
    }
}