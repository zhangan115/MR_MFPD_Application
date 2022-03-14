package com.mr.mf_pd.application.view.fragment.real

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mr.mf_pd.application.common.CheckType
import com.mr.mf_pd.application.manager.socket.callback.BaseDataCallback
import com.mr.mf_pd.application.repository.callback.RealDataCallback
import com.mr.mf_pd.application.repository.impl.DataRepository
import com.mr.mf_pd.application.repository.impl.FilesRepository
import java.io.File
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class RealModelViewModel(val dataRepository: DataRepository, private val filesRepository: FilesRepository) : ViewModel() {

    lateinit var checkType: CheckType
    lateinit var gainValues:MutableLiveData<Vector<Float>>
    var isSaveData: MutableLiveData<Boolean>? = null
    var isFile: MutableLiveData<Boolean> = MutableLiveData(false)
    var toastStr: MutableLiveData<String> = MutableLiveData()
    var location: MutableLiveData<String> = MutableLiveData(filesRepository.getCurrentCheckName())

    var gainMinValue: MutableLiveData<Float?> = MutableLiveData()

    fun start() {
        this.checkType = dataRepository.getCheckType()
        this.gainValues = dataRepository.getGainValueList()
        this.isSaveData = filesRepository.isSaveData()
        if (isFile.value!!) {
            filesRepository.addDataListener()
        }else{
            dataRepository.addDataListener()
            dataRepository.addRealDataCallback(object : RealDataCallback {
                override fun onRealDataChanged(source: ByteArray) {
                    if (filesRepository.isSaveData()?.value == true) {
                        filesRepository.toSaveRealData2File(source)
                    }
                }
            })
            dataRepository.addYcDataCallback(object : BaseDataCallback {
                override fun onData(source: ByteArray) {
                    if (filesRepository.isSaveData()?.value == true) {
                        filesRepository.toSaveYCData2File(source)
                    }
                }
            })
        }
    }

    fun addHUfData(callback: DataRepository.DataCallback) {
        dataRepository.addHufData(callback)
    }

    fun getPhaseData(): ArrayList<HashMap<Int, Float>> {
        return dataRepository.getPhaseData(1)
    }

    fun startSaveData(){
        filesRepository.startSaveData()
    }

    fun stopSaveData(){
        filesRepository.stopSaveData()
    }

    fun setCheckFile(filePath: String) {
        val file = File(filePath)
        filesRepository.setCurrentClickFile(file)
        location.postValue(filesRepository.getCurrentCheckName())
        createACheckFile()
    }

    fun createACheckFile(){
        filesRepository.toCreateCheckFile(checkType)
    }


    fun cleanCurrentData(){
        dataRepository.getGainValueList().postValue(Vector())
        dataRepository.cleanData()
    }

    override fun onCleared() {
        super.onCleared()
        dataRepository.removeRealDataListener()
    }
}