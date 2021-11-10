package com.mr.mf_pd.application.view.check.tev

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mr.mf_pd.application.common.CheckType
import com.mr.mf_pd.application.repository.impl.DataRepository
import com.mr.mf_pd.application.repository.impl.SettingRepository

class CheckTEVViewModel(val dataRepository: DataRepository,val settingRepository: SettingRepository) : ViewModel() {

    var toastStr: MutableLiveData<String> = MutableLiveData()

    fun start(checkType: CheckType) {
        settingRepository.getSettingData(checkType)
        dataRepository.switchPassageway(1)
    }

}