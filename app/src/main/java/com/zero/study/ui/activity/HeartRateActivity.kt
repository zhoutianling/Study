package com.zero.study.ui.activity

import android.Manifest
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.ImageFormat
import android.graphics.Rect
import android.graphics.YuvImage
import android.hardware.Camera
import android.util.Log
import android.util.Size
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
import com.zero.base.ext.dp
import com.zero.study.databinding.ActivityHeartRateBinding
import com.zero.study.listener.BpmListener
import com.zero.study.model.HeartRateRecordEntity
import com.zero.study.ui.widget.BpmHandler
import com.zero.study.ui.widget.HeartRateCameraView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class HeartRateActivity : BaseActivity<ActivityHeartRateBinding>(ActivityHeartRateBinding::inflate) {

    private var heartRateCameraView: HeartRateCameraView? = null
    private var entity: HeartRateRecordEntity? = null

    private var bpmHandler: BpmHandler? = null
    private val cameraLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
        if (isGranted) {
//            measureHeartRate(true)
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
//                val bitmap = data?.let { yuv420ToBitmap(it, size.width, size.height) }
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
            // 处理字节数据
            val byteArray = yuv420888ToNv21(image)
//            val bitmap = nv21ToBitmap(byteArray,image.width,image.height)
            runOnUiThread {
                bpmHandler?.handleCamera(byteArray, image.width, image.height)
            }
            // 处理完成后必须关闭图像
            image.close()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

    /**
     * 将 YUV_420_888 的 ImageProxy 转为 NV21 格式的 ByteArray
     */
    private fun yuv420888ToNv21(image: ImageProxy): ByteArray {
        val yBuffer = image.planes[0].buffer
        val uBuffer = image.planes[1].buffer
        val vBuffer = image.planes[2].buffer

        val ySize = yBuffer.remaining()
        val uSize = uBuffer.remaining()
        val vSize = vBuffer.remaining()

        val nv21 = ByteArray(ySize + uSize + vSize)

        // Y
        yBuffer.get(nv21, 0, ySize)

        // NV21: Y + VU
        var pos = ySize
        val pixelStride = image.planes[1].pixelStride
        val rowStride = image.planes[1].rowStride
        val width = image.width
        val height = image.height

        val u = ByteArray(uSize)
        val v = ByteArray(vSize)
        uBuffer.get(u)
        vBuffer.get(v)

        for (row in 0 until height / 2) {
            for (col in 0 until width / 2) {
                val vuIndex = row * rowStride + col * pixelStride
                nv21[pos++] = v[vuIndex]
                nv21[pos++] = u[vuIndex]
            }
        }
        return nv21
    }

    fun nv21ToBitmap(nv21: ByteArray, width: Int, height: Int): Bitmap {
        val yuvImage = YuvImage(nv21, ImageFormat.NV21, width, height, null)
        val out = ByteArrayOutputStream()
        yuvImage.compressToJpeg(Rect(0, 0, width, height), 100, out)
        val jpegData = out.toByteArray()
        return BitmapFactory.decodeByteArray(jpegData, 0, jpegData.size)
    }

    fun yuv420ToBitmap(yuvData: ByteArray, width: Int, height: Int): Bitmap {
        // 创建临时 Bitmap
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

        // 将 YUV420 数据转换为 JPEG 中间格式
        val yuvImage = YuvImage(yuvData, ImageFormat.NV21, width, height, null)
        val outputStream = ByteArrayOutputStream()
        yuvImage.compressToJpeg(Rect(0, 0, width, height), 100, outputStream)
        val jpegData = outputStream.toByteArray()

        // 从 JPEG 数据创建 Bitmap
        val decodedBitmap = BitmapFactory.decodeByteArray(jpegData, 0, jpegData.size)
        decodedBitmap?.let {
            // 将解码后的 Bitmap 绘制到目标 Bitmap 上
            Canvas(bitmap).drawBitmap(it, 0f, 0f, null)
            it.recycle() // 释放临时 Bitmap
        }

        return bitmap
    }

    fun imageProxyToBitmap(image: ImageProxy): Bitmap? {
        // 获取图像平面
        val planes = image.planes

        // 检查图像格式
        if (image.format != ImageFormat.YUV_420_888) {
            return null
        }

        // 分配输出缓冲区
        val ySize = planes[0].buffer.remaining()
        val uSize = planes[1].buffer.remaining()
        val vSize = planes[2].buffer.remaining()
        val nv21 = ByteArray(ySize + uSize + vSize)

        // 复制 Y 平面数据
        planes[0].buffer.get(nv21, 0, ySize)

        // 复制 UV 平面数据（交错存储）
        var offset = ySize
        val vBuffer = planes[2].buffer
        val uBuffer = planes[1].buffer

        // 注意：UV 数据在不同设备上可能顺序不同，某些设备需要先复制 U 再复制 V
        vBuffer.rewind()
        uBuffer.rewind()
        vBuffer.get(nv21, offset, vSize)
        offset += vSize
        uBuffer.get(nv21, offset, uSize)

        // 将 NV21 格式转换为 Bitmap
        val yuvImage = YuvImage(nv21, ImageFormat.NV21, image.width, image.height, null)
        val out = ByteArrayOutputStream()
        yuvImage.compressToJpeg(Rect(0, 0, image.width, image.height), 100, out)
        val imageBytes = out.toByteArray()

        return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
    }
}
