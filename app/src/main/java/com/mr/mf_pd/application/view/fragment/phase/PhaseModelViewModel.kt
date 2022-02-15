package com.mr.mf_pd.application.view.fragment.phase

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mr.mf_pd.application.common.CheckType
import com.mr.mf_pd.application.repository.callback.RealDataCallback
import com.mr.mf_pd.application.repository.impl.DataRepository
import com.mr.mf_pd.application.repository.impl.FilesRepository

class PhaseModelViewModel(
    val repository: DataRepository,
    private val filesRepository: FilesRepository,
) :
    ViewModel() {

    lateinit var checkType: CheckType
    lateinit var gainValues: MutableLiveData<List<Float>>
    var toastStr: MutableLiveData<String> = MutableLiveData()
    var isSaveData: MutableLiveData<Boolean>? = null

    var location: MutableLiveData<String> = MutableLiveData(repository.getCheckFileDir()?.name)

    fun start() {
        this.checkType = repository.getCheckType()
        this.gainValues = repository.getGainValueList()
        this.isSaveData = filesRepository.isSaveData()
        repository.realDataListener()
    }

    fun getPhaseData(): ArrayList<HashMap<Int, Float>> {
        return repository.getPhaseData(0)
    }

    fun startSaveData() {
        filesRepository.startSaveData()
    }

    fun stopSaveData() {
        filesRepository.stopSaveData()
    }

    fun cleanCurrentData() {
        repository.cleanData()
        repository.getGainValueList().postValue(ArrayList())
    }

    override fun onCleared() {
        super.onCleared()
        repository.removeRealDataListener()
    }

}