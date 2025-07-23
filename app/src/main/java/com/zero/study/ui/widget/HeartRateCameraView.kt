package com.zero.study.ui.widget

import android.content.Context
import android.hardware.Camera
import android.hardware.Camera.PreviewCallback
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.util.Log
import android.view.SurfaceHolder
import android.view.SurfaceView

class HeartRateCameraView : SurfaceView {
    var listener: OnHeartRateCameraListener? = null
    private var camera: Camera? = null
    private var isAttached: Boolean = false
    private var isResume = false
    private var surfaceWidth = 0
    private var surfaceHeight = 0

    private val surfaceCallback: SurfaceHolder.Callback = object : SurfaceHolder.Callback {
        override fun surfaceDestroyed(holder: SurfaceHolder) {
        }


        override fun surfaceCreated(holder: SurfaceHolder) {
            Log.e("zzz", "surfaceCreated $isResume,$isAttached,$camera")
            tryBind()
        }


        override fun surfaceChanged(holder: SurfaceHolder, i: Int, width: Int, height: Int) {
            Log.e("zzz", "surfaceChanged $isResume,$isAttached,$camera,$width,$height")
            surfaceWidth = width
            surfaceHeight = height

            if (camera == null) {
                return
            }
            tryBind()
            tryPreview()
        }
    }

    private val previewCallback = PreviewCallback { data, camera ->
        if (listener != null) {
            listener!!.onPreviewCamera(data, camera)
        }
    }


    interface OnHeartRateCameraListener {
        fun onError(str: String?)

        fun onOpenCamera()

        fun onStopCamera()

        fun onPreviewCamera(data: ByteArray?, camera: Camera?)
    }


    constructor(context: Context?) : super(context)

    constructor(context: Context?, attributeSet: AttributeSet?) : super(context, attributeSet)

    constructor(context: Context?, attributeSet: AttributeSet?, i: Int) : super(context, attributeSet, i)

    override fun getHandler(): Handler {
        return mainHandler
    }

    fun onResume() {
        Log.e("zzz", "HeartRateCamera onResume $isResume,$isAttached,$camera")
        if (!this.isResume) {
            this.isResume = true
            dealCamera()
        }
    }

    fun onPause() {
        Log.e("zzz", "HeartRateCamera onPause $isResume,$isAttached,$camera")
        if (this.isResume) {
            this.isResume = false
            dealCamera()
        }
    }

    override fun onAttachedToWindow() {
        Log.e("zzz", "onAttachedToWindow $isResume,$isAttached,$camera")
        super.onAttachedToWindow()
        if (!this.isAttached) {
            this.isAttached = true
            val surfaceHolder = holder
            if (surfaceHolder != null) {
                surfaceHolder.addCallback(this.surfaceCallback)
                surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS)
            }
            dealCamera()
        }
    }

    override fun onDetachedFromWindow() {
        Log.e("zzz", "onDetachedFromWindow $isResume,$isAttached,$camera")
        super.onDetachedFromWindow()
        if (this.isAttached) {
            this.isAttached = false
            val surfaceHolder = holder
            surfaceHolder?.removeCallback(this.surfaceCallback)
            dealCamera()
        }
    }

    private fun dealCamera() {
        if (!this.isAttached || !this.isResume) {
            stopCamera()
        } else {
            openCamera()
        }
    }


    private fun getSmallestPreviewSize(width: Int, height: Int, parameters: Camera.Parameters): Camera.Size? {
        var result: Camera.Size? = null
        for (size in parameters.supportedPreviewSizes) {
            if (size.width <= width && size.height <= height) {
                if (result == null) {
                    result = size
                } else {
                    val resultArea = result.width * result.height
                    val newArea = size.width * size.height

                    if (newArea < resultArea) result = size
                }
            }
        }

        return result
    }


    private fun openCamera() {
        Log.e("zzz", "openCamera --->")
        try {
            if (this.camera == null) {
                this.camera = Camera.open()
                if (camera == null && this.listener != null) {
                    listener!!.onError("Cannot start the camera.")
                }
                tryBind()
                tryPreview()
                //bpmHandler.startHandle();
                listener!!.onOpenCamera()
            }
        } catch (th: Throwable) {
            th.printStackTrace()
            listener!!.onError(th.message)
            camera = null
            isResume = false
        }
    }

    private fun stopCamera() {
        Log.e("zzz", "stopCamera --->")
        try {
            if (this.camera != null) {
                //bpmHandler.startHandle();
                if (listener != null) {
                    listener!!.onStopCamera()
                }
                if (this.camera != null) {
                    camera!!.setPreviewCallback(null)
                    camera!!.stopPreview()
                    Thread {
                        camera!!.release()
                        this.camera = null
                    }.start()
                }
            }
        } catch (th: Throwable) {
            th.printStackTrace()
        }
    }

    fun tryPreview() {
        if (surfaceWidth <= 0 || surfaceHeight <= 0) {
            return
        }
        val smallestPreviewSize: Camera.Size?
        val parameters = camera!!.parameters
        smallestPreviewSize = getSmallestPreviewSize(surfaceWidth, surfaceHeight, parameters)
        if (smallestPreviewSize != null) {
            parameters.setPreviewSize(smallestPreviewSize.width, smallestPreviewSize.height)
        }
        try {
            parameters.flashMode = Camera.Parameters.FLASH_MODE_TORCH
            camera!!.parameters = parameters
            camera!!.setDisplayOrientation(90)

            camera!!.startPreview()
            Log.e("zzz", "startPreview ----> $surfaceWidth,$surfaceHeight")
        } catch (th: Throwable) {
            th.printStackTrace()

            parameters.flashMode = Camera.Parameters.FLASH_MODE_OFF
            camera!!.parameters = parameters

            if (listener != null) {
                listener!!.onError(th.toString())
            }
        }
    }

    fun tryBind() {
        try {
            camera?.setPreviewDisplay(holder)
            camera?.setPreviewCallback(previewCallback)
        } catch (th: Throwable) {
            th.printStackTrace()
            listener?.onError(th.toString())
        }
    }

    companion object {
        private val mainHandler = Handler(Looper.getMainLooper())
    }
}
