package com.zero.study.ui.activity

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.zero.study.BuildConfig
import com.zero.base.activity.BaseActivity
import com.zero.study.databinding.ActivityTakePhotoBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * @date:2024/9/19 18:17
 * @path:com.zero.study.ui.activity.TakePhoto
 */
class TakePhotoActivity : BaseActivity<ActivityTakePhotoBinding>(ActivityTakePhotoBinding::inflate) {
    private var imageUri: Uri? = null

    override fun initView() {
    }

    override fun initData() {
        val storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
        val timeStamp: String = SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault()).format(Date())
        imageUri = FileProvider.getUriForFile(this, "${BuildConfig.APPLICATION_ID}.fileProvider", File.createTempFile(timeStamp, ".jpg", storageDir))
    }

    override fun addListener() {
        binding.tvTakePhoto.setOnClickListener {
            cameraLauncher.launch(Manifest.permission.CAMERA)
        }
        binding.tvTakeAlbum.setOnClickListener {
            albumLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }
        binding.tvTakeVideo.setOnClickListener {
            val videoIntent = Intent(MediaStore.ACTION_VIDEO_CAPTURE)
            takeVideo.launch(videoIntent)
        }
    }

    private val cameraLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
        if (isGranted) {
            takePicture.launch(imageUri!!)
        }
    }
    private val albumLauncher = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            val cursor = contentResolver.query(uri, arrayOf(MediaStore.Images.Media.DATA, MediaStore.Images.Media.BUCKET_DISPLAY_NAME), null, null, null)
            cursor?.use {
                it.moveToFirst()
                val index = it.getColumnIndex(MediaStore.Images.ImageColumns.DATA)
                val realPath = it.getString(index)
                lifecycleScope.launch(Dispatchers.IO) {
                    Log.d("zzz", "realPath: $realPath")
                    withContext(Dispatchers.Main) {
                    Glide.with(this@TakePhotoActivity).load(realPath).into(binding.ivPhoto)
                    }
                }

            }
        }
    }
    private val takePicture = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            Toast.makeText(this, "success:${imageUri}", Toast.LENGTH_SHORT).show()
            Glide.with(this).load(imageUri).into(binding.ivPhoto)
        } else {
            Toast.makeText(this, "failed", Toast.LENGTH_SHORT).show()
        }
    }
    private val takeVideo = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val videoUri: Uri? = result.data?.data
            videoUri?.let {
                playVideo(it)
            }
        }
    }

    private fun playVideo(videoUri: Uri) {
        val videoIntent = Intent(Intent.ACTION_VIEW, videoUri)
        videoIntent.setDataAndType(videoUri, "video/*")
        startActivity(videoIntent)
    }

}