package com.mr.mf_pd.application.view.check.hf.setting

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mr.mf_pd.application.common.CheckType
import com.mr.mf_pd.application.common.Constants
import com.mr.mf_pd.application.repository.impl.DataRepository
import com.mr.mf_pd.application.repository.impl.SettingRepository

class HFSettingViewModel(setting: SettingRepository) : ViewModel() {

    lateinit var checkType: CheckType

    var toastStr: MutableLiveData<String> = MutableLiveData()

    //相位同步
    var phaseModelStr: MutableLiveData<String> = MutableLiveData()
    var phaseModelInt: MutableLiveData<Int> = MutableLiveData()

    //频带检测
    var bandDetectionStr: MutableLiveData<String> = MutableLiveData()
    var bandDetectionInt: MutableLiveData<Int> = MutableLiveData()

    //自动同步
    var isAutoSync: MutableLiveData<Boolean> = MutableLiveData(true)

    //噪音过滤
    var isNoiseFiltering: MutableLiveData<Boolean> = MutableLiveData(true)

    //以放电量为单位
    var isFdUnit: MutableLiveData<Boolean> = MutableLiveData(true)

    //固定尺度
    var isFixedScale: MutableLiveData<Boolean> = MutableLiveData(false)

    //内同步频率
    var internalSyncStr: MutableLiveData<String> = MutableLiveData("50.00")

    //相位偏移
    var phaseOffsetStr: MutableLiveData<String> = MutableLiveData("0°")

    //累计时间
    var totalTimeStr: MutableLiveData<String> = MutableLiveData("30")

    //校准系数，pc/mv
    var jzXsStr: MutableLiveData<String> = MutableLiveData("30")

    //校准器输出，pc
    var jzQShuChu: MutableLiveData<String> = MutableLiveData("30")

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

        isFdUnit.postValue(settingBean.fdlUnit == 1)
        jzXsStr.postValue(settingBean.jzRatio.toString())
        jzQShuChu.postValue(settingBean.jzOutValue.toString())
    }


}