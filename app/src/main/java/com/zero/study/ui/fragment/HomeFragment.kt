package com.zero.study.ui.fragment

import android.Manifest
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity.RESULT_OK
import androidx.browser.customtabs.CustomTabColorSchemeParams
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import androidx.core.net.toUri
import androidx.core.os.bundleOf
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.google.gson.Gson
import com.zero.base.activity.BaseActivity
import com.zero.base.bus.RxBus
import com.zero.base.ext.appFileManager
import com.zero.base.ext.fromJson
import com.zero.base.ext.log
import com.zero.base.ext.readJson
import com.zero.base.ext.startActivity
import com.zero.base.ext.toast
import com.zero.base.fragment.BaseFragment
import com.zero.base.fragment.LoadingDialog
import com.zero.base.util.ThreadPool
import com.zero.health.ui.activity.AlarmRemindActivity
import com.zero.health.ui.activity.HeartRateActivity
import com.zero.study.R
import com.zero.study.databinding.FragmentHomeBinding
import com.zero.study.event.MsgEvent
import com.zero.study.provider.HookSwitchProvider.Companion.PATH_SWITCH
import com.zero.study.ui.activity.AccessPerActivity
import com.zero.study.ui.activity.AnimationActivity
import com.zero.study.ui.activity.ContextProviderActivity
import com.zero.study.ui.activity.GuideActivity
import com.zero.study.ui.activity.InterstitialActivity
import com.zero.study.ui.activity.LanguageActivity
import com.zero.study.ui.activity.MainActivity
import com.zero.study.ui.activity.NotificationActivity
import com.zero.study.ui.activity.OkioActivity
import com.zero.study.ui.activity.PagingActivity
import com.zero.study.ui.activity.PermissionManagerActivity
import com.zero.study.ui.activity.RecyclerViewActivity
import com.zero.study.ui.activity.RoomActivity
import com.zero.study.ui.activity.SecondActivity
import com.zero.study.ui.activity.TakePhotoActivity
import com.zero.study.ui.dialog.BottomSheetDialog
import com.zero.study.ui.dialog.Dialog
import com.zero.study.ui.model.AskViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import kotlin.properties.Delegates

class HomeFragment : BaseFragment<FragmentHomeBinding>(FragmentHomeBinding::inflate) {
    private var listener: OnClickListener? = null

    private val viewModel: AskViewModel by lazy {
        ViewModelProvider(this)[AskViewModel::class.java]
    }
    private var size: Int by Delegates.observable(0) { _, oldValue, newValue ->
        Log.d("zzz", "${oldValue}->${newValue} ")
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnClickListener) {
            this.listener = context
        }
    }

    /**
     * 是否开启沉浸式
     */
    private var windowIsTranslucent: Boolean = false
    override fun initView() {
        binding.tagFlow.addTag(requireContext(), Gson().fromJson(requireActivity().readJson("tags.json"))) { position, _ ->
            when (position + 1) {
                1 -> {
                    size += 1
                    storageLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)

                }

                2 -> context?.startActivity<GuideActivity>()
                3 -> context?.startActivity<RecyclerViewActivity>()
                4 -> context?.startActivity<AnimationActivity>()
                5 -> activityResult.launch(Intent(requireContext(), SecondActivity::class.java))
                6 -> checkPermissionThenLauncher()
                7 -> {
                    val parentActivity = requireActivity() as BaseActivity<*>
                    if (!windowIsTranslucent) {
                        parentActivity.hideSystemBars(WindowInsetsCompat.Type.systemBars())
                        windowIsTranslucent = true
                    } else {
                        parentActivity.showSystemBars(WindowInsetsCompat.Type.systemBars())
                        windowIsTranslucent = false
                    }
                }

                8 -> PermissionManagerActivity.start(requireContext())
                9 -> {
                    val progressDialog = LoadingDialog()
                    lifecycleScope.launch {
                        progressDialog.show(childFragmentManager, "LoadingDialog")
                        delay(3000)
                        progressDialog.dismissAllowingStateLoss()
                    }
                }

                10 -> context?.startActivity<InterstitialActivity>()
                11 -> context?.startActivity<LanguageActivity>()
                12 -> context?.startActivity<AccessPerActivity>()
                13 -> {
                    Dialog.Builder().setTitle("Custom Title").setCancelText("Exit").setConfirmText("Confirm").setCancelOnTouchOutSide(false).setOnClickListener { input ->
                        Toast.makeText(requireContext(), input, Toast.LENGTH_SHORT).show()
                    }.build().show(childFragmentManager, "Dialog")
                }

                14 -> {
                    val builder = CustomTabsIntent.Builder()
                    val schemeBuilder = CustomTabColorSchemeParams.Builder()
                    val param = schemeBuilder.setToolbarColor(ContextCompat.getColor(requireContext(), R.color.appThemeColor)).build()
                    val customTabsIntent = builder.setDefaultColorSchemeParams(param).build()
                    customTabsIntent.launchUrl(requireContext(), Uri.parse("https:www.baidu.com"))
                }

                15 -> context?.startActivity<TakePhotoActivity>()
                16 -> {
                    viewModel.fetchAskPageList()
                    viewModel.apply {
                        viewModel.articlePageListLiveData.observe(this@HomeFragment) {
                            requireContext().toast(it?.datas.toString())
                        }
                    }
                }

                17 -> context?.startActivity<RoomActivity>()
                18 -> context?.startActivity<PagingActivity>()
                19 -> context?.startActivity<OkioActivity>()
                20 -> context?.appFileManager()
                21 -> {
                    parentFragmentManager.setFragmentResult("changeTheme", bundleOf())
                }

                22 -> context?.startActivity<NotificationActivity>()
                23 -> {
                    //setFragmentResult API
                    parentFragmentManager.setFragmentResult(MainActivity::class.java.simpleName, bundleOf("screenHeight" to resources.displayMetrics.heightPixels))
                }

                24 -> {
                    context?.startActivity<HeartRateActivity>()
                }

                25 -> {
                    "EventBus post".log("zzz")
                    isHookEnabled(requireContext())
                    RxBus.getInstance().post(MsgEvent("time:${System.currentTimeMillis()}"))
                    BottomSheetDialog.Builder().setTitle(getString(R.string.dialog_title)).setCancelText(getString(R.string.dialog_cancel)).setConfirmText(getString(R.string.dialog_confirm)).setOnClickListener {

                    }.build().show(childFragmentManager, "BottomSheetDialog")
                }

                26 -> context?.startActivity<AlarmRemindActivity>()
                27 -> context?.startActivity<ContextProviderActivity>()

                else -> ThreadPool.execute { Log.i("zzz", "ThreadName:" + Thread.currentThread().name) }
            }
        }
    }

    private fun isHookEnabled(context: Context): Boolean {
        return try {
            val appContext = context.applicationContext
            val authority = "${appContext.packageName}.HookSwitchProvider"
            val uri = "content://$authority/$PATH_SWITCH".toUri()
            val contentResolver: ContentResolver = context.contentResolver
            val cursor: Cursor? = contentResolver.query(uri, arrayOf("is_enabled"), null, null, null)
            var enabled = false
            cursor?.use {
                if (it.count > 0 && it.moveToFirst()) {
                    val columnIndex = it.getColumnIndex("is_enabled")
                    if (columnIndex != -1) {
                        val value = it.getInt(columnIndex)
                        enabled = value == 1
                        Log.d("zzz", "Hook enabled value from provider: $enabled")
                    }
                }
            }
            enabled

        } catch (e: Exception) {
            false
        }
    }

    private fun deleteFile() {
        val filePath = "/storage/emulated/0/Download/1710409305027.jpg"
        val file = File(filePath)

        if (file.exists()) {
            val deleted = file.delete()
            if (deleted) {
                println("文件已删除")
            } else {
                println("文件删除失败")
            }
        } else {
            println("文件不存在")
        }
    }

    override fun initData() {
    }

    override fun setListener() {
    }

    private val permission: Array<String> by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(Manifest.permission.READ_MEDIA_IMAGES, Manifest.permission.READ_MEDIA_VIDEO)
        } else {
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
    }
    private var activityResult: ActivityResultLauncher<Intent> = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
        if (result.resultCode == RESULT_OK) {
            val resultData = result.data!!.getStringExtra("data_return")
            Log.d("zzz", "resultData: $resultData")
        }
    }
    var requestLauncher: ActivityResultLauncher<String> = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted: Boolean ->
        if (!granted) {
            val result = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
            if (result == PermissionChecker.PERMISSION_DENIED) {
            }
        }
        Toast.makeText(requireContext(), "request permission result:" + (if (granted) "success" else "failed"), Toast.LENGTH_SHORT).show()
    }
    private var storageLauncher: ActivityResultLauncher<String> = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted: Boolean ->
        if (granted) {
            deleteFile()
        } else {
            requireContext().toast("Permission denied")
        }
    }

    private fun checkPermissionThenLauncher() {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.R) {
            permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
        } else {
            selectSinglePhotoLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }
    }

    private val selectSinglePhotoLauncher = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {

        }
    }
    private var permissionLauncher: ActivityResultLauncher<String> = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted: Boolean ->
        if (granted) {
            selectSinglePhotoLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        } else {
            requireContext().toast("Permission denied")
        }
    }

    interface OnClickListener {
        fun onClickListener()
    }
}