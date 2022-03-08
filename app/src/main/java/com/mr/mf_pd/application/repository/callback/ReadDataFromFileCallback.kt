package com.mr.mf_pd.application.repository.callback

import com.mr.mf_pd.application.model.SettingBean

interface ReadDataFromFileCallback {

    fun onYcData(source:ByteArray?)

    fun onSettingData(settingBean: SettingBean)

    fun onRealData(source: ByteArray?)
}