package com.mr.mf_pd.application.view.check.tev.setting

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mr.mf_pd.application.common.CheckType
import com.mr.mf_pd.application.common.Constants
import com.mr.mf_pd.application.repository.impl.SettingRepository

class TEVSettingViewModel(val setting: SettingRepository) : ViewModel() {

    var toastStr: MutableLiveData<String> = MutableLiveData()

    lateinit var checkType: CheckType

    //相位同步
    var phaseModelStr: MutableLiveData<String> = MutableLiveData()
    var phaseModelInt: MutableLiveData<Int> = MutableLiveData()

    //自动同步
    var isAutoSync: MutableLiveData<Boolean> = MutableLiveData(true)

    //噪音过滤
    var isNoiseFiltering: MutableLiveData<Boolean> = MutableLiveData(true)

    //固定尺度
    var isFixedScale: MutableLiveData<Boolean> = MutableLiveData(false)

    //内同步频率
    var internalSyncStr: MutableLiveData<String> = MutableLiveData("50.00")

    //相位偏移
    var phaseOffsetStr: MutableLiveData<String> = MutableLiveData("0°")

    //累计时间
    var totalTimeStr: MutableLiveData<String> = MutableLiveData("30")

    //最大幅值
    var maximumAmplitudeStr: MutableLiveData<String> = MutableLiveData("-30")

    //最小幅值
    var minimumAmplitudeStr: MutableLiveData<String> = MutableLiveData("-80")

    fun start(checkType: CheckType) {
        this.checkType = checkType
        val settingBean = checkType.settingBean
        phaseModelInt.postValue(settingBean.xwTb)
        phaseModelStr.postValue(Constants.PHASE_MODEL_LIST[settingBean.xwTb])
        isAutoSync.postValue(settingBean.autoTb == 1)
        isNoiseFiltering.postValue(settingBean.lyXc == 1)
        isFixedScale.postValue(settingBean.gdCd == 1)
        internalSyncStr.postValue(settingBean.nTbPl.toString())
        phaseOffsetStr.postValue(settingBean.xwPy.toString())
        totalTimeStr.postValue(settingBean.ljTime.toString())
        maximumAmplitudeStr.postValue(settingBean.maxValue.toString())
        minimumAmplitudeStr.postValue(settingBean.minValue.toString())
    }

    fun toSave(){
        val settingBean = checkType.settingBean
        settingBean.xwTb = phaseModelInt.value!!
        settingBean.autoTb = if (isAutoSync.value!!) 1 else 0
        settingBean.lyXc = if (isNoiseFiltering.value!!) 1 else 0
        settingBean.gdCd = if (isFixedScale.value!!) 1 else 0
        settingBean.nTbPl = internalSyncStr.value!!.toFloat()
        settingBean.xwPy = phaseOffsetStr.value!!.toInt()
        settingBean.ljTime = totalTimeStr.value!!.toInt()
        settingBean.maxValue = maximumAmplitudeStr.value!!.toInt()
        settingBean.minValue = minimumAmplitudeStr.value!!.toInt()
        setting.toSaveSettingData(checkType)
    }

}