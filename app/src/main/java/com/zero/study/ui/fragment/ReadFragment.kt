package com.zero.study.ui.fragment

import android.util.Log
import android.widget.ScrollView
import androidx.lifecycle.lifecycleScope
import com.google.gson.Gson
import com.zero.base.ext.fromJson
import com.zero.base.ext.readJson
import com.zero.base.fragment.BaseFragment
import com.zero.base.util.CordTimer
import com.zero.base.util.FileUtils.readLines
import com.zero.base.util.FileUtils.readLines2
import com.zero.study.databinding.FragmentReadBinding
import kotlinx.coroutines.launch
import java.io.File

/**
 * @date:2024/9/29 11:20
 * @path:com.zero.study.ui.fragment.FileFragment
 */
class ReadFragment : BaseFragment<FragmentReadBinding>(FragmentReadBinding::inflate) {

    private val file by lazy {
        File(requireContext().filesDir, "喜羊羊与灰太狼第一季.txt")
    }
    private val cordTimer by lazy {
        CordTimer()
    }

    companion object {
        fun newInstance() = ReadFragment()
    }

    override fun initView() {
        binding.tagFlow.addTag(requireContext(), Gson().fromJson(requireActivity().readJson("readTags.json"))) { position: Int, _: String? ->
            when (position + 1) {
                1 -> {
                    lifecycleScope.launch {
                        cordTimer.startTimer()
                        val readLine = StringBuilder()
                        readLines(requireContext(), file.name) { content ->
                            readLine.append("$content\n")
                            binding.tvReadContent.text = readLine.toString()
                            binding.nestedScrollView.let {
                                it.post { it.fullScroll(ScrollView.FOCUS_DOWN) }
                            }
                        }
                        cordTimer.stopTimer()
                        Log.d("zzz", "Time:${cordTimer.getElapsedTimeInSeconds()}")
                    }
                }

                2 -> lifecycleScope.launch {
                    cordTimer.startTimer()
                    val readLine = StringBuilder()
                    Log.d("ZZZ", "initView:" + requireContext().applicationInfo.packageName)
                    Log.d("ZZZ", "initView:" + requireContext().applicationInfo.name)
                    Log.d("ZZZ", "initView:" + requireContext().applicationInfo.dataDir)

                    readLines2(requireContext(), file.name) { content ->
                        readLine.append("$content\n")
                        binding.tvReadContent.text = readLine.toString()
                        binding.nestedScrollView.let {
                            it.post { it.fullScroll(ScrollView.FOCUS_DOWN) }
                        }
                    }
                    cordTimer.stopTimer()
                    Log.d("zzz", "Time:${cordTimer.getElapsedTimeInSeconds()}")
                }
            }
        }
    }

    override fun initData() {
    }

    override fun setListener() {
    }
}