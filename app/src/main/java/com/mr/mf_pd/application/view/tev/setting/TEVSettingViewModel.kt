package com.mr.mf_pd.application.view.tev.setting

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mr.mf_pd.application.repository.impl.DataRepository
import com.mr.mf_pd.application.repository.impl.SettingRepository

class TEVSettingViewModel(dataRepository: DataRepository, setting:SettingRepository) : ViewModel() {

    var toastStr: MutableLiveData<String> = MutableLiveData()
    //相位同步
    var phaseModelStr: MutableLiveData<String> = MutableLiveData()
    var phaseModelInt: MutableLiveData<Int> = MutableLiveData()
    //频带检测
    var bandDetectionStr: MutableLiveData<String> = MutableLiveData()
    var bandDetectionInt: MutableLiveData<Int> = MutableLiveData()
    //实时上传
    var isUploadReal: MutableLiveData<Boolean> = MutableLiveData(true)
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
    fun start(){}



}