package com.mr.mf_pd.application.view.fragment.real

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mr.mf_pd.application.common.CheckType
import com.mr.mf_pd.application.repository.impl.DataRepository

class RealModelViewModel(val repository: DataRepository) : ViewModel() {

    lateinit var checkType: CheckType
    lateinit var gainValues:MutableLiveData<List<Float>>
    var toastStr: MutableLiveData<String> = MutableLiveData()
    var location: MutableLiveData<String> = MutableLiveData(repository.getCheckFileDir()?.name)

    fun start() {
        this.checkType = repository.getCheckType()
        this.gainValues = repository.getGainValueList()
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