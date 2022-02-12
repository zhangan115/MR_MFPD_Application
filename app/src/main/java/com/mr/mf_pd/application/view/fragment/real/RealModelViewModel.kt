package com.mr.mf_pd.application.view.fragment.real

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mr.mf_pd.application.common.CheckType
import com.mr.mf_pd.application.repository.impl.DataRepository
import com.mr.mf_pd.application.repository.impl.FilesRepository

class RealModelViewModel(val repository: DataRepository,private val filesRepository: FilesRepository) : ViewModel() {

    lateinit var checkType: CheckType
    lateinit var gainValues:MutableLiveData<List<Float>>
    var isSaveData: MutableLiveData<Boolean>? = null
    var toastStr: MutableLiveData<String> = MutableLiveData()
    var location: MutableLiveData<String> = MutableLiveData(repository.getCheckFileDir()?.name)

    fun start() {
        this.checkType = repository.getCheckType()
        this.gainValues = repository.getGainValueList()
        this.isSaveData = filesRepository.isSaveData()
        repository.realDataListener()
    }

    fun addHUfData(callback: DataRepository.DataCallback) {
        repository.addHufData(callback)
    }

    fun getPhaseData(): ArrayList<HashMap<Int, Float>> {
        return repository.getPhaseData(checkType.type)
    }

    fun startSaveData(){
        filesRepository.startSaveData()
    }

    fun stopSaveData(){
        filesRepository.stopSaveData()
    }

    fun cleanCurrentData(){
        repository.getGainValueList().postValue(ArrayList())
        repository.cleanData()
    }

    override fun onCleared() {
        super.onCleared()
        repository.removeRealDataListener()
    }
}