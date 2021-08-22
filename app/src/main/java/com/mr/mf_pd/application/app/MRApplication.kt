package com.mr.mf_pd.application.app

import com.anson.support.base.BaseApplication

class MRApplication : BaseApplication() {

    companion object {
        lateinit var instance: MRApplication
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }
}