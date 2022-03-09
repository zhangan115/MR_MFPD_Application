package com.mr.mf_pd.application.view.check.ac.flight

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mr.mf_pd.application.common.CheckType
import com.mr.mf_pd.application.manager.socket.callback.BaseDataCallback
import com.mr.mf_pd.application.repository.callback.RealDataCallback
import com.mr.mf_pd.application.repository.impl.DataRepository
import com.mr.mf_pd.application.repository.impl.FilesRepository

class ACFlightModelViewModel(
    val dataRepository: DataRepository,
    val filesRepository: FilesRepository,
) : ViewModel() {

    var toastStr: MutableLiveData<String> = MutableLiveData()
    var location: MutableLiveData<String> = MutableLiveData("/榆林有色220kV")
    var timeStr: MutableLiveData<String> = MutableLiveData("12:09:23")
    var synchronizationModel: MutableLiveData<String> = MutableLiveData("内同步，50kHz-300kHz")
    var gainLevelStr: MutableLiveData<String> = MutableLiveData("20dB")
    var gainMinValue: MutableLiveData<Float?> = MutableLiveData()
    lateinit var gainValues: MutableLiveData<ArrayList<Float>>
    lateinit var checkType: CheckType
    var isFile: MutableLiveData<Boolean> = MutableLiveData(false)

    fun start() {
        this.gainValues = dataRepository.getGainValueList()
        if (isFile.value!!) {
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

}