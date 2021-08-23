package com.mr.mf_pd.application.repository

import com.mr.mf_pd.application.repository.impl.DataRepository
import com.mr.mf_pd.application.repository.impl.FilesRepository
import com.mr.mf_pd.application.repository.impl.SettingRepository
import com.mr.mf_pd.application.repository.impl.UserRepository

object RepositoryService {

    private var userRepository: UserRepository? = null

    fun provideUserRepository(): UserRepository {
        synchronized(this) {
            if (userRepository == null) {
                userRepository = DefaultUserRepository()
            }
            return userRepository!!
        }
    }

    private var dataRepository: DataRepository? = null

    fun provideDataRepository(): DataRepository {
        synchronized(this) {
            if (dataRepository == null) {
                dataRepository = DefaultDataRepository()
            }
            return dataRepository!!
        }
    }

    private var settingRepository: SettingRepository? = null

    fun provideSettingRepository(): SettingRepository {
        synchronized(this) {
            if (settingRepository == null) {
                settingRepository = DefaultSettingRepository()
            }
            return settingRepository!!
        }
    }

    private var filesRepository: FilesRepository? = null

    fun provideFilesRepository(): FilesRepository {
        synchronized(this) {
            if (filesRepository == null) {
                filesRepository = DefaultFilesRepository()
            }
            return filesRepository!!
        }
    }

}