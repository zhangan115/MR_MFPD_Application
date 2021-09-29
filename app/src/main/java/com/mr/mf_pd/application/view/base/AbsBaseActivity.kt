package com.mr.mf_pd.application.view.base

import android.app.Activity
import android.content.Intent
import android.content.res.Resources
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.Nullable
import androidx.appcompat.widget.Toolbar
import androidx.core.content.FileProvider
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import com.leon.lfilepickerlibrary.utils.Constant
import com.mr.mf_pd.application.R
import com.mr.mf_pd.application.utils.PhotoCompressUtils
import com.mr.mf_pd.application.view.base.ext.ACTION_CHOOSE_FILE
import com.mr.mf_pd.application.view.base.ext.ACTION_TAKE_PHOTO
import kotlinx.android.synthetic.main.toolbar.*
import org.greenrobot.eventbus.EventBus
import java.io.File
import java.io.IOException


abstract class AbsBaseActivity<T : ViewDataBinding> : BaseActivity() {
    val TAG = this.javaClass.simpleName
    lateinit var dataBinding: T
    open val showToolbar = true
    open var showClose = false
    open var noDataLayout: View? = null
    open var netWorkErrorLayout: View? = null
    open var showDarkIcon = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.window.setBackgroundDrawable(findDrawable(R.drawable.bk_color))
        getSaveState(savedInstanceState)
        initThem()
        dataBinding = DataBindingUtil.setContentView(this, getContentView())
        dataBinding.lifecycleOwner = this
        initData(savedInstanceState)
        initToolBar()
        initView(savedInstanceState)
        netWorkErrorLayout = findViewById(R.id.reload_tv)
        netWorkErrorLayout?.setOnClickListener {
            requestData()
            return@setOnClickListener
        }
        requestData()
    }

    override fun onStart() {
        super.onStart()
        if (showClose && !EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)
        }
    }

    override fun onStop() {
        super.onStop()
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this)
        }
    }

    open fun requestData() {

    }

    /**
     * 初始化view
     */
    abstract fun initView(savedInstanceState: Bundle?)

    /**
     * 初始化数据
     */
    abstract fun initData(savedInstanceState: Bundle?)


    open fun initThem() {
        //设置主题
        setDarkStatusIcon(showDarkIcon)
    }

    open fun getSaveState(savedInstanceState: Bundle?) {

    }

    open fun getToolBar(): Toolbar? {
        return null
    }

    open fun getToolBarTitleView(): TextView? {
        return null
    }

    /**
     * 约定请求拍照的RequestCode小于1000
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode < ACTION_TAKE_PHOTO && resultCode == Activity.RESULT_OK) {
            if (data?.data == null && photo != null) {
                if (photo != null) {
                    val photoURI = FileProvider.getUriForFile(
                        this,
                        "com.isuo.inspection.application.fileprovider",
                        photo!!
                    )
                    try {
                        PhotoCompressUtils.getFile(this, photoURI, {
                            dealFile(requestCode, it)
                        }, {
                            Toast.makeText(this, "图片选择失败！", Toast.LENGTH_SHORT).show()
                        })
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
            } else {
                try {
                    PhotoCompressUtils.getFile(this, data!!.data!!, {
                        dealFile(requestCode, it)
                    }, {
                        Toast.makeText(this, "图片选择失败！", Toast.LENGTH_SHORT).show()
                    })
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        } else if (requestCode in (ACTION_TAKE_PHOTO + 1)..ACTION_CHOOSE_FILE && resultCode == Activity.RESULT_OK) {
            val list = data?.getStringArrayListExtra(Constant.RESULT_INFO)
            if (list?.isNotEmpty()!!) {
                val filePath = list[0]
                // 创建File
                val mFile = File(filePath)
                dealFile(requestCode, mFile)
            }
        }
    }

    open fun dealFile(requestCode: Int, file: File) {
        Log.d("Photo File", "requestCode:${requestCode} and file path${file.absolutePath}")
    }

    /**
     * 初始化toolbar
     */
    open fun initToolBar() {
        val toolBar = findViewById<Toolbar>(R.id.toolbar) ?: return
        if (!showToolbar) {
            return
        }
        titleId?.text = getToolBarTitle()
        setSupportActionBar(toolBar)
        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setDisplayShowTitleEnabled(false)
        }
        toolBar.setNavigationOnClickListener {
            onBackAction()
        }
    }

    /**
     * 初始化toolbar
     */
    open fun initToolBar(showClose: Boolean) {
        val toolBar = findViewById<Toolbar>(R.id.toolbar) ?: return
        if (!showToolbar) {
            return
        }
        titleId?.text = getToolBarTitle()
        setSupportActionBar(toolBar)
        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setDisplayShowTitleEnabled(false)
        }
        toolBar.setNavigationOnClickListener {
            onBackAction()
        }

    }

    /**
     * 获取界面布局
     */
    abstract fun getContentView(): Int

    /**
     * 获取toolbar的标题
     */
    @Nullable
    open fun getToolBarTitle(): String? {
        return null
    }

    override fun onBackPressed() {
        onBackAction()
    }

    /**
     * 按下返回键或者toolbar的返回键
     */
    open fun onBackAction() {
        super.onBackPressed()
    }

    /**
     * 状态栏完全透明
     */
    fun transparentStatusBar() {
        //兼容5.0及以上支持全透明
        this.window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        this.window.decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        this.window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        this.window.statusBarColor = Color.TRANSPARENT
    }

    /**
     * 修改状态栏icon 颜色
     *
     * @param bDark 是否将icon 颜色变为灰色
     */
    fun setDarkStatusIcon(bDark: Boolean) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val decorView = window.decorView
            var vis = decorView.systemUiVisibility
            vis = if (bDark) {
                vis or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            } else {
                vis and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
            }
            decorView.systemUiVisibility = vis
        }
    }

    /**
     * 是否展示close图标
     */
    fun setToolBarClose(showClose: Boolean) {
        this.showClose = showClose
    }

    /**
     * 查找颜色
     */
    fun findColor(color: Int): Int {
        return resources.getColor(color)
    }

    /**
     * 查找字符串
     */
    fun findString(str: Int): String {
        return resources.getString(str)
    }

    /**
     * 查找图片
     */
    fun findDrawable(drawable: Int): Drawable {
        return resources.getDrawable(drawable)
    }

    open fun getStatusHeight(): Int {
        var statusBarHeight = 0
        val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
        if (resourceId > 0) {
            statusBarHeight = resources.getDimensionPixelSize(resourceId)
        }
        return statusBarHeight
    }

}
