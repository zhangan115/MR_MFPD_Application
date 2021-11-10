package com.mr.mf_pd.application.repository.impl

import com.mr.mf_pd.application.common.CheckType
import com.mr.mf_pd.application.model.SettingBean

interface SettingRepository {

    fun toSaveSettingData(checkType: CheckType)

    fun getSettingData(checkType: CheckType): SettingBean

}