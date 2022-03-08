package com.mr.mf_pd.application.view.fragment.continuity

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mr.mf_pd.application.common.CheckType
import com.mr.mf_pd.application.manager.socket.callback.BaseDataCallback
import com.mr.mf_pd.application.repository.callback.RealDataCallback
import com.mr.mf_pd.application.repository.impl.DataRepository
import com.mr.mf_pd.application.repository.impl.FilesRepository
import java.io.File

class ContinuityModelViewModel(
    val dataRepository: DataRepository,
    val filesRepository: FilesRepository,
) : ViewModel() {

    var toastStr: MutableLiveData<String> = MutableLiveData()
    var location: MutableLiveData<String> = MutableLiveData(filesRepository.getCurrentCheckName())
    var isSaveData: MutableLiveData<Boolean>? = null

    lateinit var checkType: CheckType

    fun start() {
        this.checkType = dataRepository.getCheckType()
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
                Log.d("zhangan",source.toString())
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

    fun createACheckFile() {

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

}