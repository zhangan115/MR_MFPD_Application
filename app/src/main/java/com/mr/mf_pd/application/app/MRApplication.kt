package com.mr.mf_pd.application.app

import android.app.Activity
import android.app.Application
import android.os.Handler
import androidx.annotation.NonNull
import com.mr.mf_pd.application.BuildConfig
import com.mr.mf_pd.application.R
import com.mr.mf_pd.application.common.ConstantStr
import com.mr.mf_pd.application.repository.RepositoryService
import com.mr.mf_pd.application.repository.impl.DataRepository
import com.mr.mf_pd.application.repository.impl.FilesRepository
import com.mr.mf_pd.application.repository.impl.SettingRepository
import com.mr.mf_pd.application.repository.impl.UserRepository
import com.mr.mf_pd.application.utils.ZLog
import com.mr.mf_pd.application.view.splash.SplashActivity
import com.scwang.smartrefresh.layout.SmartRefreshLayout
import com.scwang.smartrefresh.layout.footer.ClassicsFooter
import com.scwang.smartrefresh.layout.header.ClassicsHeader
import com.sito.tool.library.utils.SPHelper
import com.tuzhenlei.crashhandler.CrashHandler
import com.umeng.commonsdk.UMConfigure
import java.io.File
import java.util.*
import kotlin.collections.ArrayList


class MRApplication : Application() {

    companion object {
        lateinit var instance: MRApplication
        const val TAG = "MRApplication"
        @JvmStatic
        fun appHost(): String {
            return SPHelper.readString(
                instance,
                ConstantStr.USER_INFO,
                ConstantStr.APP_HOST,
                "192.168.3.199"
            )
        }

        @JvmStatic
        fun port(): Int {
            return SPHelper.readInt(
                instance,
                ConstantStr.USER_INFO,
                ConstantStr.APP_PORT,
                8000
            )
        }

        private val sHandler = Handler()

        fun runUi(runnable: Runnable?) {
            if (runnable != null) {
                sHandler.post(runnable)
            }
        }
    }

    init {
        //设置全局的Header构建器
        SmartRefreshLayout.setDefaultRefreshHeaderCreator { context, layout ->
            layout.setPrimaryColorsId(
                R.color.colorWhite,
                R.color.colorAccent
            )//全局设置主题颜色
            ClassicsHeader(context)
            //.setTimeFormat(new DynamicTimeFormat("更新于 %s"));
            // 指定为经典Header，默认是 贝塞尔雷达Header
        }
        //设置全局的Footer构建器
        SmartRefreshLayout.setDefaultRefreshFooterCreator { context, layout ->
            //指定为经典Footer，默认是 BallPulseFooter
            ClassicsFooter(context).setDrawableSize(20f)
        }
    }

    val userRepository: UserRepository
        get() = RepositoryService.provideUserRepository()

    val dataRepository: DataRepository
        get() = RepositoryService.provideDataRepository()

    val settingRepository: SettingRepository
        get() = RepositoryService.provideSettingRepository()

    val filesRepository: FilesRepository
        get() = RepositoryService.provideFilesRepository()


    private lateinit var activityList: ArrayList<Activity>

    override fun onCreate() {
        super.onCreate()
        instance = this
        activityList = ArrayList()
        CrashHandler.getInstance().init(this, BuildConfig.DEBUG, false, 0,
            SplashActivity::class.java)
        UMConfigure.init(this, BuildConfig.UMENG_APP_KEY,BuildConfig.UMENG_CHANNEL,1, null)
    }

    /**
     * 打开一个页面
     *
     * @param activity activity
     */
    fun openActivity(@NonNull activity: Activity?) {
        activity?.let { activityList.add(0, it) } //最新activity的加入第一个位置
    }

    /**
     * 获取当前Activity（堆栈中最后一个压入的）
     */
    fun currentActivity(): Activity? {
        if (activityList.isEmpty()) {
            return null
        }
        return activityList[0]
    }

    /**
     * 关闭一个页面
     *
     * @param activity activity
     */
    fun closeActivity(@NonNull activity: Activity?) {
        activity?.let { activityList.remove(it) }
    }

    /**
     * 退出App
     */
    fun exitApp() {
        if (activityList.size > 0) {
            for (activity in activityList) {
                activity.finish()
            }
        }
    }

    fun imageCacheFile(): String {
        return externalCacheDir!!.absolutePath
    }

    fun fileCacheFile(): File? {
        var externalFileRootDir = getExternalFilesDir(null)
        do {
            externalFileRootDir = Objects.requireNonNull(externalFileRootDir)?.parentFile
        } while (Objects.requireNonNull(externalFileRootDir)?.absolutePath
                ?.contains("/Android")!!
        )
        val saveDir = Objects.requireNonNull(externalFileRootDir)?.absolutePath;
        val file = File(saveDir, ConstantStr.MR_FILE)
        ZLog.d(TAG, "cache file ="+file.absolutePath)
        if (!file.exists()) {
            if (file.mkdir()) {
                ZLog.d(TAG, "create file success")
            }
        }
        return file
    }

    /**
     * 保存上次保存检测文件的地址
     */
    fun saveCheckFileToSp(file: File) {
        SPHelper.write(this, ConstantStr.USER_INFO, ConstantStr.CHECK_FILE_DIR, file.absolutePath)
    }
}