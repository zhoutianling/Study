package com.zero.study.ui.activity

import android.Manifest
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.ImageFormat
import android.graphics.Outline
import android.graphics.Rect
import android.graphics.YuvImage
import android.hardware.Camera
import android.util.Log
import android.util.Size
import android.view.View
import android.view.ViewGroup
import android.view.ViewOutlineProvider
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.core.view.doOnLayout
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
    private var previewView: PreviewView? = null
    private var bpmHandler: BpmHandler? = null
    private val cameraLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
        if (isGranted) {
//            measureHeartRate(true)
            startCamera()
        }
    }

    override fun initView() {
        previewView = binding.previewView
        previewView?.doOnLayout {
            previewView?.clipToOutline = true
            previewView?.outlineProvider = object : ViewOutlineProvider() {
                override fun getOutline(view: View, outline: Outline) {
                    val radius = 30f.dp // 圆角半径
                    outline.setRoundRect(0, 0, view.width, view.height, radius)
                }
            }
        }
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

    private var cameraProvider: ProcessCameraProvider? = null
    private lateinit var cameraExecutor: ExecutorService
    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            cameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build().also {
                it.surfaceProvider = binding.previewView.surfaceProvider
            }
            val imageAnalysis = ImageAnalysis.Builder().setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST).build().also {
                it.setAnalyzer(cameraExecutor, MyImageAnalyzer())
            }
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
            try {
                cameraProvider?.unbindAll()
                val camera = cameraProvider?.bindToLifecycle(this, cameraSelector, preview, imageAnalysis)
                camera?.let {
                    if (it.cameraInfo.hasFlashUnit()) {
                        it.cameraControl.enableTorch(true)
                    } else {
                        Toast.makeText(this, "设备不支持闪光灯", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(this, "相机启动失败: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private inner class MyImageAnalyzer : ImageAnalysis.Analyzer {
        override fun analyze(image: ImageProxy) {
            // 处理字节数据
            val targetWidth = 120
            val targetHeight = 60
            val cropLeft = (image.width - targetWidth) / 2
            val cropTop = (image.height - targetHeight) / 2
            val nv21ByteArray = yuv420888ToNv21(image)
            val originBitmap = nv21ToBitmap(nv21ByteArray, image.width, image.height)
            val originRotatedBitmap = rotateBitmap(originBitmap, image.imageInfo.rotationDegrees.toFloat())

            val nv21ByteArrayCrop = yuv420888ToNv21Crop(image, cropLeft, cropTop, targetWidth, targetHeight)
            val scaledBitmap = nv21ToBitmap(nv21ByteArrayCrop, targetWidth, targetHeight)
            val scaledRotatedBitmap = rotateBitmap(scaledBitmap, image.imageInfo.rotationDegrees.toFloat())

            bpmHandler?.handleCamera(nv21ByteArrayCrop, targetWidth, targetHeight)
            // 处理完成后必须关闭图像
            image.close()
        }
    }

    fun rotateBitmap(bitmap: Bitmap, degrees: Float): Bitmap {
        val matrix = android.graphics.Matrix()
        matrix.postRotate(degrees)
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraProvider?.unbindAll()
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

    fun yuv420888ToNv21Crop(image: ImageProxy, cropLeft: Int, cropTop: Int, cropWidth: Int, cropHeight: Int): ByteArray {
        val yPlane = image.planes[0]
        val uPlane = image.planes[1]
        val vPlane = image.planes[2]

        val yRowStride = yPlane.rowStride
        val uRowStride = uPlane.rowStride
        val vRowStride = vPlane.rowStride
        val uPixelStride = uPlane.pixelStride
        val vPixelStride = vPlane.pixelStride

        val nv21 = ByteArray(cropWidth * cropHeight * 3 / 2)
        val yBuffer = yPlane.buffer
        val uBuffer = uPlane.buffer
        val vBuffer = vPlane.buffer

        // 1. 裁剪Y分量
        var yPos = 0
        for (row in 0 until cropHeight) {
            val yRowStart = (row + cropTop) * yRowStride + cropLeft
            yBuffer.position(yRowStart)
            yBuffer.get(nv21, yPos, cropWidth)
            yPos += cropWidth
        }

        // 2. 裁剪UV分量（每2行采样一次）
        var uvPos = cropWidth * cropHeight
        for (row in 0 until cropHeight / 2) {
            val uRowStart = ((row + cropTop / 2) * uRowStride) + (cropLeft / 2) * uPixelStride
            val vRowStart = ((row + cropTop / 2) * vRowStride) + (cropLeft / 2) * vPixelStride
            for (col in 0 until cropWidth / 2) {
                // NV21: VU顺序
                vBuffer.position(vRowStart + col * vPixelStride)
                nv21[uvPos++] = vBuffer.get()
                uBuffer.position(uRowStart + col * uPixelStride)
                nv21[uvPos++] = uBuffer.get()
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

    fun bitmapToNv21(bitmap: Bitmap): ByteArray {
        val width = bitmap.width
        val height = bitmap.height
        val argb = IntArray(width * height)
        bitmap.getPixels(argb, 0, width, 0, 0, width, height)
        val yuv = ByteArray(width * height * 3 / 2)
        var yIndex = 0
        var uvIndex = width * height

        for (j in 0 until height) {
            for (i in 0 until width) {
                val argbPixel = argb[j * width + i]

                val r = (argbPixel shr 16) and 0xff
                val g = (argbPixel shr 8) and 0xff
                val b = argbPixel and 0xff

                // Y分量
                val y = ((66 * r + 129 * g + 25 * b + 128) shr 8) + 16
                yuv[yIndex++] = y.coerceIn(0, 255).toByte()

                // UV分量（偶数行偶数列采样）
                if (j % 2 == 0 && i % 2 == 0) {
                    val u = ((-38 * r - 74 * g + 112 * b + 128) shr 8) + 128
                    val v = ((112 * r - 94 * g - 18 * b + 128) shr 8) + 128
                    yuv[uvIndex++] = v.coerceIn(0, 255).toByte() // NV21: VU顺序
                    yuv[uvIndex++] = u.coerceIn(0, 255).toByte()
                }
            }
        }
        return yuv
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
