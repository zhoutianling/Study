package com.zero.base.ext

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment

/**
 * 多个Fragment的切换
 */
fun AppCompatActivity.switchFragment(containerId: Int, fragmentToShow: Fragment, tag: String) {
    supportFragmentManager.beginTransaction().apply {
        // 隐藏当前显示的Fragment
        supportFragmentManager.fragments.forEach {
            if (it.isVisible) {
                hide(it)
            }
        }

        // 显示目标Fragment，如果不存在则添加
        if (fragmentToShow.isAdded) {
            show(fragmentToShow)
        } else {
            add(containerId, fragmentToShow, tag)
        }
        // 提交事务
        commit()
    }
}

/**
 * 单个Fragment的显示和隐藏
 */
fun AppCompatActivity.toggleFragment(containerId: Int, fragment: Fragment, tag: String, show: Boolean) {
    supportFragmentManager.beginTransaction().apply {
        val existingFragment = supportFragmentManager.findFragmentByTag(tag)
        if (show) {
            if (existingFragment == null) {
                add(containerId, fragment, tag)
            } else {
                show(existingFragment)
            }
        } else {
            existingFragment?.let { hide(it) }
        }
        commit()
    }
}