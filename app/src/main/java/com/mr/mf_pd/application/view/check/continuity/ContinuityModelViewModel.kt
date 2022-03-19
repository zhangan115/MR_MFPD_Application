package com.mr.mf_pd.application.view.check.continuity

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

    var isFile: MutableLiveData<Boolean> = MutableLiveData(false)

    var fzValueList: ArrayList<Float> = ArrayList()
    var yxValueList: ArrayList<Float> = ArrayList()
    var f1ValueList: ArrayList<Float> = ArrayList()
    var f2ValueList: ArrayList<Float> = ArrayList()

    var fzMinValue: MutableLiveData<String> = MutableLiveData()
    var fzValue: MutableLiveData<String> = MutableLiveData()
    var yxMinValue: MutableLiveData<String> = MutableLiveData()
    var yxValue: MutableLiveData<String> = MutableLiveData()
    var f1MinValue: MutableLiveData<String> = MutableLiveData()
    var f1Value: MutableLiveData<String> = MutableLiveData()
    var f2MinValue: MutableLiveData<String> = MutableLiveData()
    var f2Value: MutableLiveData<String> = MutableLiveData()

    lateinit var checkType: CheckType

    fun start() {
        if (isFile.value!!) {
            this.checkType = filesRepository.getCheckType()
            filesRepository.addDataListener()
        }else{
            this.isSaveData = filesRepository.isSaveData()
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

    fun setCheckFile(filePath: String) {
        val file = File(filePath)
        filesRepository.setCurrentClickFile(file)
        location.postValue(filesRepository.getCurrentCheckName())
        createACheckFile()
    }

    fun createACheckFile() {
        filesRepository.toCreateCheckFile(checkType)
    }

    fun startSaveData() {
        filesRepository.startSaveData()
    }

    fun stopSaveData() {
        filesRepository.stopSaveData()
    }

    fun cleanCurrentData() {
        dataRepository.cleanData()
        fzValueList.clear()
        yxValueList.clear()
        f1ValueList.clear()
        f2ValueList.clear()
    }

}