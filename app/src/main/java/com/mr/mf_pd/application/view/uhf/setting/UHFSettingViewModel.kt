package com.mr.mf_pd.application.view.uhf.setting

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mr.mf_pd.application.repository.impl.DataRepository

class UHFSettingViewModel(dataRepository: DataRepository) : ViewModel() {

    var toastStr: MutableLiveData<String> = MutableLiveData()

    fun start(){}

}