package com.zero.study.ui.fragment

import android.util.Log
import com.zero.base.fragment.BaseFragment
import com.zero.base.widget.CenterSeekBar
import com.zero.base.widget.CenterSeekBar.OnSeekBarChangeListener
import com.zero.study.databinding.FragmentWidgetBinding

class ThirdFragment : BaseFragment<FragmentWidgetBinding>(FragmentWidgetBinding::inflate) {
    companion object {
        fun newInstance() = ThirdFragment()
    }

    override fun initView() {
        binding.seekbar.setRange(-50, 50)
        binding.seekbar.setProgress(0)
        binding.seekbar.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: CenterSeekBar?, progress: Int) {
                Log.d("zzz", "onProgressChanged: $progress")
            }
        })
        binding.btn00.setOnClickListener {
            binding.seekbar.setRange(-50, 50)
            binding.seekbar.setProgress(30)
        }
        binding.btn01.setOnClickListener {
            binding.seekbar.setRange(0, 100)
            binding.seekbar.setProgress(80)
        }

        binding.btn02.setOnClickListener {
            binding.seekbar.setRange(-100, 100)
            binding.seekbar.setProgress(50)
        }
        binding.btn03.setOnClickListener {
            binding.seekbar.setRange(0, 50)
            binding.seekbar.setProgress(0)
        }
        binding.banner.initBannerImageView(listOf("https://i.mij.rip/2025/04/15/e590f7632b8263184d026a4c5922b5dc.png","https://i.miji.bid/2025/04/15/93a88977959de47c06569b8ebb677d95.png","https://i.mij.rip/2025/04/15/c881a01cd0cd0814f2df66d01f0839f7.png"))
    }

    override fun initData() {
    }

    override fun setListener() {
    }

}