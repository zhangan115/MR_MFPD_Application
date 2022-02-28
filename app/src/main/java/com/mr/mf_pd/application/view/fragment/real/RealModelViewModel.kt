package com.mr.mf_pd.application.view.fragment.real

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mr.mf_pd.application.common.CheckType
import com.mr.mf_pd.application.repository.callback.RealDataCallback
import com.mr.mf_pd.application.repository.impl.DataRepository
import com.mr.mf_pd.application.repository.impl.FilesRepository
import java.io.File

class RealModelViewModel(val repository: DataRepository,private val filesRepository: FilesRepository) : ViewModel() {

    lateinit var checkType: CheckType
    lateinit var gainValues:MutableLiveData<ArrayList<Float>>
    var isSaveData: MutableLiveData<Boolean>? = null
    var toastStr: MutableLiveData<String> = MutableLiveData()
    var location: MutableLiveData<String> = MutableLiveData(filesRepository.getCurrentCheckName())
    var ycByteArray: ByteArray? = null

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

    fun addHUfData(callback: DataRepository.DataCallback) {
        repository.addHufData(callback)
    }

    fun getPhaseData(): ArrayList<HashMap<Int, Float>> {
        return repository.getPhaseData(1)
    }

    fun startSaveData(){
        filesRepository.startSaveData()
    }

    fun stopSaveData(){
        filesRepository.stopSaveData()
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


    fun cleanCurrentData(){
        repository.getGainValueList().postValue(ArrayList())
        repository.cleanData()
    }

    override fun onCleared() {
        super.onCleared()
        repository.removeRealDataListener()
    }
}