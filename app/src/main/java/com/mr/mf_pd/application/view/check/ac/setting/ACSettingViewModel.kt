package com.mr.mf_pd.application.view.check.ac.setting

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mr.mf_pd.application.common.CheckType
import com.mr.mf_pd.application.common.Constants
import com.mr.mf_pd.application.repository.impl.SettingRepository

class ACSettingViewModel(settingRepository: SettingRepository) : ViewModel() {

    var toastStr: MutableLiveData<String> = MutableLiveData()
    lateinit var checkType: CheckType

    //相位同步
    var phaseModelStr: MutableLiveData<String> = MutableLiveData()
    var phaseModelInt: MutableLiveData<Int> = MutableLiveData()

    //实时上传
    var isUploadReal: MutableLiveData<Boolean> = MutableLiveData(true)

    //自动同步
    var isAutoSync: MutableLiveData<Boolean> = MutableLiveData(true)

    //噪音过滤
    var isNoiseFiltering: MutableLiveData<Boolean> = MutableLiveData(true)

    //内同步频率
    var internalSyncStr: MutableLiveData<String> = MutableLiveData("50.00")

    //相位偏移
    var phaseOffsetStr: MutableLiveData<String> = MutableLiveData("0°")

    //累计时间
    var totalTimeStr: MutableLiveData<String> = MutableLiveData("30")

    //频带下限
    var pdDownStr: MutableLiveData<String> = MutableLiveData("30")

    //频带上限
    var pdUpStr: MutableLiveData<String> = MutableLiveData("30")
    //幅值单位
    var fzUnitStr: MutableLiveData<String> = MutableLiveData("mV")

    //固定尺度
    var isFixedScale: MutableLiveData<Boolean> = MutableLiveData(false)

    //声音输出
    var isOutVoice: MutableLiveData<Boolean> = MutableLiveData(false)

    //自动增益
    var isAutoZy: MutableLiveData<Boolean> = MutableLiveData(false)

    //最大幅值
    var maximumAmplitudeStr1: MutableLiveData<String> = MutableLiveData("-30")

    //最小幅值
    var minimumAmplitudeStr1: MutableLiveData<String> = MutableLiveData("-80")

    //最大幅值2
    var maximumAmplitudeStr2: MutableLiveData<String> = MutableLiveData("-30")

    //最小幅值2
    var minimumAmplitudeStr2: MutableLiveData<String> = MutableLiveData("-80")

    fun start(checkType: CheckType) {
        this.checkType = checkType
        val settingBean = checkType.settingBean
        phaseModelInt.postValue(settingBean.xwTb)
        phaseModelStr.postValue(Constants.PHASE_MODEL_LIST[settingBean.xwTb])

        isAutoSync.postValue(settingBean.autoTb == 1)
        isNoiseFiltering.postValue(settingBean.lyXc == 1)
        isFixedScale.postValue(settingBean.gdCd == 1)
        isOutVoice.postValue(settingBean.outVoice == 1)
        isAutoZy.postValue(settingBean.autoZy == 1)
        internalSyncStr.postValue(settingBean.nTbPl.toString())
        phaseOffsetStr.postValue(settingBean.xwPy.toString())
        totalTimeStr.postValue(settingBean.ljTime.toString())

        maximumAmplitudeStr1.postValue(settingBean.maxValue.toString())
        minimumAmplitudeStr1.postValue(settingBean.minValue.toString())
        maximumAmplitudeStr2.postValue(settingBean.maxValue2.toString())
        minimumAmplitudeStr2.postValue(settingBean.minValue2.toString())
    }


}