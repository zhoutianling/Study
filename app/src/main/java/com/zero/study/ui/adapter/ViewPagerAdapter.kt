package com.zero.study.ui.adapter

/**

 * @date:2024/5/24 19:17
 * @path:com.toolkit.photoeditor.ui.adapter.ViewPagerAdapter
 */

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter


class ViewPagerAdapter(fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {
    private val fragmentList = ArrayList<FragmentWrapper>()

    fun addFragment(fragment: FragmentWrapper) {
        fragmentList.add(fragment)
    }

    override fun getItemCount(): Int {
        return fragmentList.size
    }

    override fun createFragment(position: Int): Fragment {
        return fragmentList[position].fragment
    }

    fun getTitle(position: Int): String {
        return fragmentList[position].title
    }

    fun getIconRes(position: Int): Int {
        return fragmentList[position].iconRes
    }

    class FragmentWrapper(var title: String, var iconRes: Int, var fragment: Fragment)
}