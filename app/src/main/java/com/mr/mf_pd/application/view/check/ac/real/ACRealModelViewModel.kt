package com.mr.mf_pd.application.view.check.ac.real

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mr.mf_pd.application.model.ACModelBean
import com.mr.mf_pd.application.model.UHFModelBean
import com.mr.mf_pd.application.repository.impl.DataRepository

class ACRealModelViewModel(val repository: DataRepository) : ViewModel() {

    var toastStr: MutableLiveData<String> = MutableLiveData()
    var location: MutableLiveData<String> = MutableLiveData("/榆林有色220kV")

    var num1Str: MutableLiveData<String> = MutableLiveData("-76")
    var num2Str: MutableLiveData<String> = MutableLiveData("-78")
    var num3Str: MutableLiveData<String> = MutableLiveData("-80")

    var acData: ACModelBean? = null

    fun start() {
        acData = repository.getAcData()
        repository.hufDataListener()
    }

    fun addACData( callback:DataRepository.DataCallback) {
        repository.addHufData(callback)
    }

    override fun onCleared() {
        super.onCleared()
        repository.removeHufDataListener()
    }
}