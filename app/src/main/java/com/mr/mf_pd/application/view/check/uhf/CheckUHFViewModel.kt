package com.mr.mf_pd.application.view.check.uhf

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mr.mf_pd.application.common.CheckType
import com.mr.mf_pd.application.repository.impl.DataRepository
import com.mr.mf_pd.application.repository.impl.SettingRepository

class CheckUHFViewModel(
    val dataRepository: DataRepository,
    private val settingRepository: SettingRepository
) : ViewModel() {

    var toastStr: MutableLiveData<String> = MutableLiveData()

    fun start(checkType: CheckType) {
        settingRepository.getSettingData(checkType)
        dataRepository.setCheckType(checkType)
        dataRepository.switchPassageway(0)
    }

    override fun onCleared() {
        super.onCleared()
        dataRepository.closePassageway()
    }
}