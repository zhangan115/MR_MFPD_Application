package com.mr.mf_pd.application.view.check.real

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mr.mf_pd.application.common.CheckType
import com.mr.mf_pd.application.manager.socket.callback.BaseDataCallback
import com.mr.mf_pd.application.repository.callback.DataCallback
import com.mr.mf_pd.application.repository.callback.RealDataCallback
import com.mr.mf_pd.application.repository.impl.DataRepository
import com.mr.mf_pd.application.repository.impl.FilesRepository
import java.io.File
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class RealModelViewModel(
    val dataRepository: DataRepository,
    private val filesRepository: FilesRepository,
) : ViewModel() {

    lateinit var checkType: CheckType
    lateinit var gainValues: MutableLiveData<Vector<Float>>
    var isSaveData: MutableLiveData<Boolean>? = null
    var isFile: MutableLiveData<Boolean> = MutableLiveData(false)
    var toastStr: MutableLiveData<String> = MutableLiveData()
    var location: MutableLiveData<String> = MutableLiveData(filesRepository.getCurrentCheckName())

    var gainMinValue: MutableLiveData<Float?> = MutableLiveData()

    fun start() {
        this.isSaveData = filesRepository.isSaveData()
        if (isFile.value!!) {
            this.gainValues = filesRepository.getGainValueList()
            this.checkType = filesRepository.getCheckType()
            filesRepository.addDataListener()
        } else {
            this.gainValues = dataRepository.getGainValueList()
            this.checkType = dataRepository.getCheckType()
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

    fun addHUfData(callback: DataCallback) {
        if (isFile.value == true) {
            filesRepository.addHufData(callback)
        } else {
            dataRepository.addHufData(callback)
        }
    }

    fun getPhaseData(): ArrayList<HashMap<Int, Float?>> {
        if (isFile.value == true) {
            return filesRepository.getPhaseData(1)
        }
        return dataRepository.getPhaseData(1)
    }

    fun startSaveData() {
        filesRepository.startSaveData()
    }

    fun stopSaveData() {
        filesRepository.stopSaveData()
    }

    fun setCheckFile(filePath: String) {
        val file = File(filePath)
        filesRepository.setCurrentClickFile(file)
        location.postValue(filesRepository.getCurrentCheckName())
        createACheckFile()
    }

    fun createACheckFile() {
        filesRepository.toCreateCheckFile(checkType)
    }


    fun cleanCurrentData() {
        dataRepository.getGainValueList().postValue(Vector())
        dataRepository.cleanData()
    }

    override fun onCleared() {
        super.onCleared()
        dataRepository.removeRealDataListener()
    }
}