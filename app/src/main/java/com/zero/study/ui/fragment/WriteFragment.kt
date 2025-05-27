package com.zero.study.ui.fragment

import android.util.Log
import androidx.lifecycle.lifecycleScope
import com.google.gson.Gson
import com.zero.base.ext.fromJson
import com.zero.base.ext.readJson
import com.zero.base.fragment.BaseFragment
import com.zero.base.util.CordTimer
import com.zero.base.util.FileUtils
import com.zero.base.util.FileUtils.createFolder
import com.zero.base.util.FileUtils.deleteFilesByFolder
import com.zero.base.util.FileUtils.listFilesByFolderName
import com.zero.base.util.FileUtils.writeContentToFile
import com.zero.base.util.ToastUtil
import com.zero.study.databinding.FragmentWriteBinding
import com.zero.study.ui.dialog.MiniDialogFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

/**
 * @date:2024/9/29 11:20
 * @path:com.zero.study.ui.fragment.FileFragment
 */
class WriteFragment : BaseFragment<FragmentWriteBinding>(FragmentWriteBinding::inflate) {

    private val file by lazy {
        File(requireContext().filesDir, "喜羊羊与灰太狼.txt")
    }
    private val cordTimer by lazy {
        CordTimer()
    }

    companion object {
        fun newInstance() = WriteFragment()
    }

    override fun initView() {
        binding.tagFlow.addTag(requireContext(), Gson().fromJson(requireActivity().readJson("writeTags.json"))) { position: Int, _: String? ->
            when (position + 1) {
                1 -> {
                    val totalSize = 1000
                    lifecycleScope.launch {
                        cordTimer.startTimer()
                        for (i in 1..totalSize) {
                            writeContentToFile(requireContext(), "少儿动画", "喜羊羊与灰太狼第一季.txt", "喜羊羊与灰太狼第一季-第" + i + "集")
                            writeContentToFile(requireContext(), "少儿动画", "喜羊羊与灰太狼第二季.txt", "喜羊羊与灰太狼第二季-第" + i + "集")
                            writeContentToFile(requireContext(), "少儿动画", "喜羊羊与灰太狼第三季.txt", "喜羊羊与灰太狼第三季-第" + i + "集")
                            writeContentToFile(requireContext(), "少儿动画", "喜羊羊与灰太狼第四季.txt", "喜羊羊与灰太狼第四季-第" + i + "集")
                            writeContentToFile(requireContext(), "少儿动画", "喜羊羊与灰太狼第五季.txt", "喜羊羊与灰太狼第五季-第" + i + "集")
                            withContext(Dispatchers.Main) { binding.progressBar1.setProgress(i * 100 / (totalSize)) }
                        }
                        cordTimer.stopTimer()
                        Log.d("zzz", "Time:${cordTimer.getElapsedTimeInSeconds()}")
                    }
                    //方式二：使用
                    lifecycleScope.launch(Dispatchers.IO) {
                        cordTimer.startTimer()
                        for (i in 1..totalSize) {
                            FileUtils.writeContentToFile2(requireContext(), "喜羊羊与灰太狼第二季.txt", "喜羊羊与灰太狼第二季-第" + i + "集")
                            withContext(Dispatchers.Main) { binding.progressBar2.setProgress(i * 100 / totalSize) }
                        }
                        cordTimer.stopTimer()
                        Log.d("zzz", "Time2:${cordTimer.getElapsedTimeInSeconds()}")
                    }
                    lifecycleScope.launch(Dispatchers.IO) {
                        cordTimer.startTimer()
                        for (i in 1..totalSize) {
                            FileUtils.writeContentToFile3(requireContext(), "喜羊羊与灰太狼第三季.txt", "喜羊羊与灰太狼第三季-第" + i + "集")
                            withContext(Dispatchers.Main) { binding.progressBar3.setProgress(i * 100 / totalSize) }
                        }
                        cordTimer.stopTimer()
                        Log.d("zzz", "Time3:${cordTimer.getElapsedTimeInSeconds()}")
                    }
                }

                2 -> {
                    lifecycleScope.launch {
                        val results = FileUtils.deleteFiles(requireContext(), "少儿动画", "喜羊羊与灰太狼第一季.txt", "喜羊羊与灰太狼第二季.txt", "喜羊羊与灰太狼第三季.txt")
                        results.forEach { result ->
                            result.fold(onSuccess = { msg ->
                                withContext(Dispatchers.Main) {
                                    ToastUtil.showShort(requireContext(), msg)
                                }
                            }, onFailure = { exception ->
                                withContext(Dispatchers.Main) {
                                    ToastUtil.showShort(requireContext(), exception.message!!)
                                }
                            })
                        }
                    }
                }

                3 -> {
                    lifecycleScope.launch {
                        val deleteCount = deleteFilesByFolder(requireContext(), "少儿动画")
                        ToastUtil.showShort(requireContext(), "$deleteCount files deleted")
                    }

                }

                5 -> {
                    val fileList = listFilesByFolderName(requireContext(), "少儿动画")
                    fileList.forEach {
                        Log.d("ZZZ", "initView: " + it.name)
                    }
                }

                6 -> {
                    MiniDialogFragment.Builder().setTitle("Create Folder").setCancelText("Cancel").setConfirmText("OK").setCancelOnTouchOutSide(false).setOnClickListener { input ->
                        lifecycleScope.launch {
                            val result: Result<String> = createFolder(requireContext(), input)
                            result.fold(
                                onSuccess = { msg ->
                                    withContext(Dispatchers.Main) {
                                        ToastUtil.showShort(requireContext(), msg)
                                    }
                                }, onFailure = { exception ->
                                    withContext(Dispatchers.Main) {
                                        ToastUtil.showShort(requireContext(), exception.message!!)
                                    }
                                }
                            )
                        }

                    }.build().show(childFragmentManager, "CreateFolderDialog")
                }
            }
        }
    }

    override fun initData() {
    }

    override fun setListener() {
    }
}