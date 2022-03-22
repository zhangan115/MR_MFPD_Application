package com.mr.mf_pd.application.view.check.phase

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mr.mf_pd.application.common.CheckType
import com.mr.mf_pd.application.manager.socket.callback.BaseDataCallback
import com.mr.mf_pd.application.repository.callback.RealDataCallback
import com.mr.mf_pd.application.repository.impl.DataRepository
import com.mr.mf_pd.application.repository.impl.FilesRepository
import com.mr.mf_pd.application.view.callback.FlightDataCallback
import com.mr.mf_pd.application.view.callback.PrPsDataCallback
import java.io.File
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class PhaseModelViewModel(
    val dataRepository: DataRepository,
    val filesRepository: FilesRepository,
) : ViewModel() {

    lateinit var checkType: CheckType
    lateinit var gainValues: MutableLiveData<Vector<Float>>
    var gainMinValue: MutableLiveData<Float?> = MutableLiveData()

    var toastStr: MutableLiveData<String> = MutableLiveData()
    var isSaveData: MutableLiveData<Boolean>? = null

    var isFile: MutableLiveData<Boolean> = MutableLiveData(false)

    var location: MutableLiveData<String> = MutableLiveData(filesRepository.getCurrentCheckName())

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

    private var prpsCallback : PrPsDataCallback? = null

    fun setPrpsCallback(callback: PrPsDataCallback){
        prpsCallback = callback
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

    fun getPhaseData(): ArrayList<HashMap<Int, Float>> {
        if (isFile.value == true) {
            return filesRepository.getPhaseData(0)
        }
        return dataRepository.getPhaseData(0)
    }

    fun startSaveData() {
        filesRepository.startSaveData()
    }

    fun stopSaveData() {
        filesRepository.stopSaveData()
    }

    fun cleanCurrentData() {
        val list = Vector<Float>()
        if (isFile.value == true) {
            this.gainValues.postValue(list)
            filesRepository.cleanData()
            filesRepository.getGainValueList().postValue(list)
        } else {
            this.gainValues.postValue(list)
            dataRepository.cleanData()
            dataRepository.getGainValueList().postValue(list)
        }
    }

    override fun onCleared() {
        super.onCleared()
        dataRepository.removeRealDataListener()
    }
}