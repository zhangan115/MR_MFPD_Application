package com.mr.mf_pd.application.view.fragment.phase

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mr.mf_pd.application.common.CheckType
import com.mr.mf_pd.application.repository.callback.RealDataCallback
import com.mr.mf_pd.application.repository.impl.DataRepository
import com.mr.mf_pd.application.repository.impl.FilesRepository
import java.io.File

class PhaseModelViewModel(
    val repository: DataRepository,
    val filesRepository: FilesRepository,
) :
    ViewModel() {

    lateinit var checkType: CheckType
    lateinit var gainValues: MutableLiveData<List<Float>>
    var toastStr: MutableLiveData<String> = MutableLiveData()
    var isSaveData: MutableLiveData<Boolean>? = null
    var ycByteArray: ByteArray? = null

    var location: MutableLiveData<String> = MutableLiveData(filesRepository.getCurrentCheckName())

    fun start() {
        this.checkType = repository.getCheckType()
        this.gainValues = repository.getGainValueList()
        this.isSaveData = filesRepository.isSaveData()
        repository.realDataListener()
        repository.addRealDataCallback(object : RealDataCallback {
            override fun onRealDataChanged(source: ByteArray) {
                if (filesRepository.isSaveData()?.value == true) {
                    filesRepository.toSaveData2File(source)
                }
            }
        })
    }

    fun setCheckFile(filePath: String) {
        val file = File(filePath)
        filesRepository.setCurrentChickFile(file)
        location.postValue(filesRepository.getCurrentCheckName())
        createACheckFile()
    }

    fun createACheckFile(){
        filesRepository.toCreateCheckFile(checkType,ycByteArray)
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