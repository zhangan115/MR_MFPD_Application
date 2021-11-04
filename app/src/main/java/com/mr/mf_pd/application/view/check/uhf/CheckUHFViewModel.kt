package com.mr.mf_pd.application.view.check.uhf

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mr.mf_pd.application.common.CheckType
import com.mr.mf_pd.application.repository.impl.DataRepository

class CheckUHFViewModel(val dataRepository: DataRepository) : ViewModel() {

    var toastStr: MutableLiveData<String> = MutableLiveData()

    fun start(checkType: CheckType) {
        dataRepository.setCheckType(checkType)
        dataRepository.switchPassageway(0)
    }

}