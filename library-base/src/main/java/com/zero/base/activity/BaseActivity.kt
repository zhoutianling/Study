package com.zero.base.activity

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.lifecycleScope
import androidx.viewbinding.ViewBinding
import com.zero.base.theme.AppTheme
import com.zero.base.util.StorageUtils
import com.zero.base.widget.Gloading
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**

 * @date:2024/5/24 18:35
 * @path:com.toolkit.base.ui.activity.AbstractActivity
 */
abstract class BaseActivity<VB : ViewBinding>(private val inflate: (LayoutInflater) -> VB) : AppCompatActivity() {
    lateinit var binding: VB

    private val loadingHolder: Gloading.Holder by lazy {
        Gloading.default?.wrap(this)?.withRetry { onLoadRetry() }
            ?: throw IllegalStateException("GLoading.default is null")
    }

    open fun onLoadRetry() {
    }

    override fun attachBaseContext(newBase: Context) {
        // 加载本地配置的主题
        val theme = getAppTheme()
        delegate.localNightMode = theme.mode
        return super.attachBaseContext(newBase)
    }

    private fun getAppTheme(): AppTheme {
        val name = StorageUtils.getString(THEME_KEY, AppTheme.AUTO.name)!!
        return AppTheme.valueOf(name)
    }

    fun setTheme(theme: AppTheme) {
        if (theme == AppTheme.AUTO) {
            // delete theme
            StorageUtils.remove(THEME_KEY)
            return
        }
        StorageUtils.putString(THEME_KEY, theme.name)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            //去掉导航栏半透明遮罩
            window.isNavigationBarContrastEnforced = false
        }
        binding = inflate(layoutInflater)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        hideSystemBars(WindowInsetsCompat.Type.systemBars())

        setContentView(binding.root)
        initData()
        initView()
        addListener()
    }

    fun showLoading() {
        if (isMainThread()) {
            loadingHolder.showLoading()
        } else {
            lifecycleScope.launch(Dispatchers.Main) {
                loadingHolder.showLoading()
            }
        }
    }

    fun hideLoading() {
        if (isMainThread()) {
            loadingHolder.showLoadSuccess()
        } else {
            lifecycleScope.launch(Dispatchers.Main) {
                loadingHolder.showLoadSuccess()
            }
        }
    }

    open fun isMainThread(): Boolean {
        return Thread.currentThread() == Looper.getMainLooper().thread
    }

    /**
     * 使用 WindowInsetsCompat.Type.statusBars() 仅隐藏状态栏。
     * 使用 WindowInsetsCompat.Type.navigationBars() 仅隐藏导航栏。
     * 使用 WindowInsetsCompat.Type.systemBars() 可隐藏这两个系统栏。
     *
     * 作者：iSuperRed
     * 链接：https://juejin.cn/post/7395866692772085800
     * 来源：稀土掘金
     * 著作权归作者所有。商业转载请联系作者获得授权，非商业转载请注明出处。
     */
    fun hideSystemBars(type: Int) {
        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        //控制状态栏内容（例如时间、电池图标、通知图标）的外观：
        windowInsetsController.isAppearanceLightStatusBars = false
        //控制导航栏内容（例如返回、主页、最近应用按钮）的外观：
        windowInsetsController.isAppearanceLightNavigationBars = false
        windowInsetsController.hide(type)
    }

    fun showSystemBars(type: Int) {
        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        windowInsetsController.show(type)
    }

    abstract fun initView()

    abstract fun initData()

    abstract fun addListener()

    companion object {
        const val THEME_KEY = "theme"
    }


}
