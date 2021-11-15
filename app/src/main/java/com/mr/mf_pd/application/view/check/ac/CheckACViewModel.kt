package com.mr.mf_pd.application.view.check.ac

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mr.mf_pd.application.common.CheckType
import com.mr.mf_pd.application.repository.impl.DataRepository
import com.mr.mf_pd.application.repository.impl.SettingRepository

class CheckACViewModel(val dataRepository: DataRepository,val settingRepository: SettingRepository) : ViewModel() {

    var currentIndex: MutableLiveData<Int> = MutableLiveData(0)

    var toastStr: MutableLiveData<String> = MutableLiveData()

    fun start(checkType: CheckType) {
        settingRepository.getSettingData(checkType)
        dataRepository.setCheckType(checkType)
        dataRepository.switchPassageway(2)
    }

}