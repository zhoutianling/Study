package com.zero.study.ui.activity

import android.Manifest
import android.hardware.Camera
import android.util.Log
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import com.zero.base.activity.BaseActivity
import com.zero.study.databinding.ActivityHeartRateBinding
import com.zero.study.listener.BpmListener
import com.zero.study.model.HeartRateRecordEntity
import com.zero.study.ui.widget.BpmHandler
import com.zero.study.ui.widget.HeartRateCameraView
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class HeartRateActivity : BaseActivity<ActivityHeartRateBinding>(ActivityHeartRateBinding::inflate) {

    private var heartRateCameraView: HeartRateCameraView? = null
    private var entity: HeartRateRecordEntity? = null

    private var bpmHandler: BpmHandler? = null
    private val cameraLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
        if (isGranted) {
            measureHeartRate(true)
            startCamera()
        }
    }

    override fun initView() {

        heartRateCameraView = HeartRateCameraView(this@HeartRateActivity)
        binding.cvCamera.addView(heartRateCameraView, ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))

        bpmHandler = BpmHandler(object : BpmListener {
            override fun onProgress(progress: Int) {
                if (isFinishing) {
                    return
                }
                Log.e("zzz", "onProgress ---> $progress")
            }

            override fun onBpm(bpm: Int, stress: Int) {
//                Log.e("zzz", "onBpm ---> $bpm")
                if (isFinishing) {
                    return
                }
            }


            override fun onFingerOut(scene: Int) {
                Log.e("zzz", "onFingerOut ---> $scene")
            }

            override fun onFinish(intervals: ArrayList<Long>) {
                if (isFinishing) {
                    return
                }

                runOnUiThread {
                    val values = FloatArray(intervals.size)
                    for (i in intervals.indices) {
                        values[i] = intervals[i].toFloat()
                    }
                    entity = bpmHandler?.getMeasureResult(values)
                    Log.e("zzz", "onFinish ---> $entity")
                }
            }

            override fun onStop() {
                Log.e("zzz", "onStop ---> ")
            }

        })
        heartRateCameraView?.listener = object : HeartRateCameraView.OnHeartRateCameraListener {
            override fun onError(str: String?) {
            }

            override fun onOpenCamera() {
                bpmHandler?.startHandle()
            }

            override fun onStopCamera() {
                bpmHandler?.stopHandle()
            }


            override fun onPreviewCamera(data: ByteArray?, camera: Camera?) {
                val size = camera!!.parameters.previewSize
                bpmHandler?.handleCamera(data, size.width, size.height)
            }
        }
        cameraLauncher.launch(Manifest.permission.CAMERA)
    }

    private fun measureHeartRate(enabled: Boolean) {
        if (enabled) {
            heartRateCameraView?.onResume()
        } else {
            heartRateCameraView?.onPause()
        }
    }


    override fun initData() {
        // 初始化数据
        cameraExecutor = Executors.newSingleThreadExecutor()
    }

    override fun addListener() {

    }

    private lateinit var cameraExecutor: ExecutorService
    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build().also {
                it.surfaceProvider = binding.previewView.surfaceProvider
            }
            val imageAnalysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST) // 只处理最新帧
                .build()
                .also {
                    it.setAnalyzer(cameraExecutor, MyImageAnalyzer())
                }
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
            try {
                cameraProvider.unbindAll()
                val camera = cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalysis)
                if (camera.cameraInfo.hasFlashUnit()) {
                    camera.cameraControl.enableTorch(true)
                } else {
                    Toast.makeText(this, "设备不支持闪光灯", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this, "相机启动失败: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private inner class MyImageAnalyzer : ImageAnalysis.Analyzer {
        override fun analyze(image: ImageProxy) {
            // 将 ImageProxy 转换为 byte[]
            val buffer = image.planes[0].buffer
            val data = ByteArray(buffer.remaining())
            buffer.get(data)
            // 处理字节数据
            runOnUiThread {
                bpmHandler?.handleCamera(data, image.width, image.height)
            }
            // 处理完成后必须关闭图像
            image.close()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }
}
