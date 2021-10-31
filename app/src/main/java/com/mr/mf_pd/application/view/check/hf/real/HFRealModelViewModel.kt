package com.mr.mf_pd.application.view.check.hf.real

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mr.mf_pd.application.model.UHFModelBean
import com.mr.mf_pd.application.repository.impl.DataRepository

class HFRealModelViewModel(val repository: DataRepository) : ViewModel() {

    var toastStr: MutableLiveData<String> = MutableLiveData()
    var location: MutableLiveData<String> = MutableLiveData(repository.getCheckFileDir()?.name)
    var num1Str: MutableLiveData<String> = MutableLiveData("-76")
    var num2Str: MutableLiveData<String> = MutableLiveData("-78")
    var num3Str: MutableLiveData<String> = MutableLiveData("-80")
    var uhfData: UHFModelBean? = null
    fun start() {
        uhfData = repository.getHufData()
        repository.hufDataListener()
    }


    fun addHUfData(callback: DataRepository.DataCallback) {
        repository.addHufData(callback)
    }

    fun getPhaseData(): ArrayList<HashMap<Int, Float>> {
        return repository.getPhaseData(1)
    }

    fun getCaChePhaseData(): ArrayList<HashMap<Int, Float>> {
        return repository.getCachePhaseData(1)
    }

    override fun onCleared() {
        super.onCleared()
        repository.removeHufDataListener()
    }

}