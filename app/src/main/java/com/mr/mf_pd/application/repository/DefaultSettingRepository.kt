package com.mr.mf_pd.application.repository

import com.google.gson.Gson
import com.mr.mf_pd.application.app.MRApplication
import com.mr.mf_pd.application.common.CheckType
import com.mr.mf_pd.application.common.ConstantStr
import com.mr.mf_pd.application.model.SettingBean
import com.mr.mf_pd.application.repository.impl.SettingRepository
import com.sito.tool.library.utils.SPHelper

class DefaultSettingRepository : SettingRepository {

    override fun toSaveSettingData(checkType: CheckType) {
        val settingBean = checkType.settingBean
        SPHelper.write(
            MRApplication.instance,
            ConstantStr.SETTING_DATA,
            settingBean.cacheKey,
            Gson().toJson(settingBean)
        )
    }

    override fun getSettingData(checkType: CheckType): SettingBean {
        val settingBean = checkType.settingBean
        val result = SPHelper.readString(
            MRApplication.instance,
            ConstantStr.SETTING_DATA,
            settingBean.cacheKey
        )
        val cacheSettingBean = Gson().fromJson(result, SettingBean::class.java)
        if (cacheSettingBean != null) {
            settingBean.xwTb = cacheSettingBean.xwTb
            settingBean.autoTb = cacheSettingBean.autoTb
            settingBean.lyXc = cacheSettingBean.lyXc
            settingBean.xwPy = cacheSettingBean.xwPy
            settingBean.ljTime = cacheSettingBean.ljTime
            settingBean.gdCd = cacheSettingBean.gdCd
            settingBean.maxValue = cacheSettingBean.maxValue
            settingBean.minValue = cacheSettingBean.minValue
            settingBean.nTbPl = cacheSettingBean.nTbPl
            settingBean.fzUnit = cacheSettingBean.fzUnit
            settingBean.outVoice = cacheSettingBean.outVoice
            settingBean.pdDown = cacheSettingBean.pdDown
            settingBean.pdUp = cacheSettingBean.pdUp
            settingBean.maxValue2 = cacheSettingBean.maxValue2
            settingBean.minValue2 = cacheSettingBean.minValue2
            settingBean.fdlUnit = cacheSettingBean.fdlUnit
            settingBean.jzRatio = cacheSettingBean.jzRatio
            settingBean.jzOutValue = cacheSettingBean.jzOutValue
        }
        return settingBean
    }

    override fun getLinkIP(): String? {
        return SPHelper.readString(
            MRApplication.instance,
            ConstantStr.USER_INFO,
            ConstantStr.APP_HOST,
            "192.168.3.199"
        )
    }

    override fun saveLinkIP(ip: String?) {
        SPHelper.write(
            MRApplication.instance, ConstantStr.USER_INFO,
            ConstantStr.APP_HOST, ip
        )
    }

    override fun getLinkPort(): String {
        return SPHelper.readInt(
            MRApplication.instance,
            ConstantStr.USER_INFO,
            ConstantStr.APP_PORT,
            8000
        ).toString()
    }

    override fun saveLinkPort(port: String?) {
        val portIntValue = port?.toIntOrNull()
        portIntValue?.let {
            SPHelper.write(
                MRApplication.instance, ConstantStr.USER_INFO,
                ConstantStr.APP_PORT, it
            )
        }
    }

}