package com.mr.mf_pd.application.view.fragment.phase

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mr.mf_pd.application.common.CheckType
import com.mr.mf_pd.application.manager.socket.callback.BaseDataCallback
import com.mr.mf_pd.application.repository.callback.RealDataCallback
import com.mr.mf_pd.application.repository.impl.DataRepository
import com.mr.mf_pd.application.repository.impl.FilesRepository
import java.io.File

class PhaseModelViewModel(
    val dataRepository: DataRepository,
    val filesRepository: FilesRepository,
) : ViewModel() {

    lateinit var checkType: CheckType
    lateinit var gainValues: MutableLiveData<ArrayList<Float>>
    var toastStr: MutableLiveData<String> = MutableLiveData()
    var isSaveData: MutableLiveData<Boolean>? = null

    var location: MutableLiveData<String> = MutableLiveData(filesRepository.getCurrentCheckName())

    fun start() {
        this.checkType = dataRepository.getCheckType()
        this.gainValues = dataRepository.getGainValueList()
        this.isSaveData = filesRepository.isSaveData()

        dataRepository.addDataListener()
        dataRepository.addRealDataCallback(object : RealDataCallback {
            override fun onRealDataChanged(source: ByteArray) {
                if (filesRepository.isSaveData()?.value == true) {
                    filesRepository.toSaveRealData2File(source)
                }
            }
        })
        dataRepository.addYcDataCallback(object :BaseDataCallback{
            override fun onData(source: ByteArray) {
                if (filesRepository.isSaveData()?.value == true) {
                    filesRepository.toSaveYCData2File(source)
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
        filesRepository.toCreateCheckFile(checkType)
    }

    fun getPhaseData(): ArrayList<HashMap<Int, Float>> {
        return dataRepository.getPhaseData(0)
    }

    fun startSaveData() {
        filesRepository.startSaveData()
    }

    fun stopSaveData() {
        filesRepository.stopSaveData()
    }

    fun cleanCurrentData() {
        dataRepository.cleanData()
        dataRepository.getGainValueList().postValue(ArrayList())
    }


    override fun onCleared() {
        super.onCleared()
        dataRepository.removeRealDataListener()
    }

}