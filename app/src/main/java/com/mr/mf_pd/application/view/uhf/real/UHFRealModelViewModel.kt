package com.mr.mf_pd.application.view.uhf.real

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mr.mf_pd.application.model.UHFModelBean
import com.mr.mf_pd.application.repository.impl.DataRepository

class UHFRealModelViewModel(val repository: DataRepository) : ViewModel() {
    var toastStr: MutableLiveData<String> = MutableLiveData()
    var location: MutableLiveData<String> = MutableLiveData("/榆林有色220kV")
    var num1Str: MutableLiveData<String> = MutableLiveData("-76")
    var num2Str: MutableLiveData<String> = MutableLiveData("-78")
    var num3Str: MutableLiveData<String> = MutableLiveData("-80")
    var uhfData: UHFModelBean? = null

    fun start() {
        uhfData = repository.getHufData()
    }
}