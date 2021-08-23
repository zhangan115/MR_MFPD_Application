package com.mr.mf_pd.application.app

import com.anson.support.base.BaseApplication
import com.mr.mf_pd.application.repository.RepositoryService
import com.mr.mf_pd.application.repository.impl.DataRepository
import com.mr.mf_pd.application.repository.impl.FilesRepository
import com.mr.mf_pd.application.repository.impl.SettingRepository
import com.mr.mf_pd.application.repository.impl.UserRepository

class MRApplication : BaseApplication() {

    companion object {
        lateinit var instance: MRApplication
    }

    val userRepository: UserRepository
        get() = RepositoryService.provideUserRepository()

    val dataRepository: DataRepository
        get() = RepositoryService.provideDataRepository()

    val settingRepository: SettingRepository
        get() = RepositoryService.provideSettingRepository()

    val filesRepository: FilesRepository
        get() = RepositoryService.provideFilesRepository()


    override fun onCreate() {
        super.onCreate()
        instance = this
    }
}