package com.zero.study.ui.fragment

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import com.google.gson.Gson
import com.zero.base.ext.checkPermission
import com.zero.base.ext.fromJson
import com.zero.base.ext.readJson
import com.zero.base.ext.requestPermission
import com.zero.base.ext.saveImageToGallery
import com.zero.base.ext.saveImageToPrivateDir
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
                            writeContentToFile(requireContext().filesDir, "少儿动画", "喜羊羊与灰太狼第一季.txt", "喜羊羊与灰太狼第一季-第" + i + "集")
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

                    }.build().show(childFragmentManager, "CreateFolderDialog")
                }

                7 -> {
                    //外部私有存储
                    val bitmap = Bitmap.createBitmap(500, 500, Bitmap.Config.ARGB_8888)
                    val canvas = Canvas(bitmap)
                    canvas.drawColor(Color.GREEN)
                    requireContext().saveImageToPrivateDir(bitmap)
                }

                8 -> {
                    // 外部公共存储
                    if (checkPermission(requireContext())) {
                        val bitmap = Bitmap.createBitmap(400, 400, Bitmap.Config.ARGB_8888).apply {
                            val canvas = Canvas(this)
                            canvas.drawColor(Color.RED)
                        }
                        requireContext().saveImageToGallery(bitmap)
                    } else {
                        requestPermission(requireActivity())
                    }

                }

                9 -> {
                }
            }
        }
    }

    override fun initData() {
    }

    override fun setListener() {
    }
}