package com.mr.mf_pd.application.app

import android.app.Activity
import android.app.Application
import androidx.annotation.NonNull
import com.mr.mf_pd.application.common.ConstantStr
import com.mr.mf_pd.application.repository.RepositoryService
import com.mr.mf_pd.application.repository.impl.DataRepository
import com.mr.mf_pd.application.repository.impl.FilesRepository
import com.mr.mf_pd.application.repository.impl.SettingRepository
import com.mr.mf_pd.application.repository.impl.UserRepository
import com.sito.tool.library.utils.SPHelper

class MRApplication : Application() {

    companion object {
        lateinit var instance: MRApplication

        fun appHost(): String {
            return SPHelper.readString(
                instance,
                ConstantStr.USER_INFO,
                ConstantStr.APP_HOST,
                ""
            )
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


    private var activityList: ArrayList<Activity>? = null

    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    /**
     * 打开一个页面
     *
     * @param activity activity
     */
    fun openActivity(@NonNull activity: Activity?) {
        activity?.let { activityList?.add(0, it) } //最新activity的加入第一个位置
    }

    /**
     * 获取当前Activity（堆栈中最后一个压入的）
     */
    fun currentActivity(): Activity? {
        return activityList?.get(0)
    }

    /**
     * 关闭一个页面
     *
     * @param activity activity
     */
    fun closeActivity(@NonNull activity: Activity?) {
        activity?.let { activityList?.remove(it) }
    }

    /**
     * 退出App
     */
    private fun exitApp() {
        if (activityList!!.size > 0) {
            for (activity in activityList!!) {
                activity.finish()
            }
        }
    }

    fun imageCacheFile(): String {
        return externalCacheDir!!.absolutePath
    }
}