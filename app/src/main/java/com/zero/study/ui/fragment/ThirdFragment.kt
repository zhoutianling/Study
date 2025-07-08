package com.zero.study.ui.fragment

import android.util.Log
import androidx.lifecycle.lifecycleScope
import com.zero.base.ext.copyAssetsToFileDir
import com.zero.base.fragment.BaseFragment
import com.zero.base.widget.CenterSeekBar
import com.zero.base.widget.CenterSeekBar.OnSeekBarChangeListener
import com.zero.study.databinding.FragmentWidgetBinding
import com.zero.study.ui.adapter.BannerAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ThirdFragment : BaseFragment<FragmentWidgetBinding>(FragmentWidgetBinding::inflate) {
    companion object {
        fun newInstance() = ThirdFragment()
    }

    override fun initView() {
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            requireContext().copyAssetsToFileDir("banner", true)
        }
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
        val bannerList = mutableListOf<String>()
        repeat(3) { i ->
            val thumbnail = requireContext().filesDir.absolutePath + "/banner/" + "banner_${i + 1}.webp"
            bannerList.add(thumbnail)
        }
        binding.bannerNormal.initBannerImageView(bannerList)
        val bannerAdapter = BannerAdapter().apply { submitList(bannerList) }
        binding.banner3D.setAdapter(bannerAdapter)
    }

    override fun initData() {
    }

    override fun setListener() {
    }

}