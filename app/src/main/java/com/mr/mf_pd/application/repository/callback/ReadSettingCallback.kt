package com.mr.mf_pd.application.repository.callback

import com.mr.mf_pd.application.model.SettingBean

interface ReadSettingCallback {

    fun onSettingBean(settingBean: SettingBean)

}