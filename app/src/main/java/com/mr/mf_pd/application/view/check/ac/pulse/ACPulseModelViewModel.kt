package com.mr.mf_pd.application.view.check.ac.pulse

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mr.mf_pd.application.common.CheckType
import com.mr.mf_pd.application.manager.socket.comand.CommandHelp
import com.mr.mf_pd.application.manager.socket.comand.CommandType
import com.mr.mf_pd.application.manager.socket.SocketManager
import com.mr.mf_pd.application.manager.socket.callback.BaseDataCallback
import com.mr.mf_pd.application.repository.callback.RealDataCallback
import com.mr.mf_pd.application.repository.impl.DataRepository
import com.mr.mf_pd.application.repository.impl.FilesRepository

class ACPulseModelViewModel(val dataRepository: DataRepository, val filesRepository: FilesRepository) : ViewModel() {

    var toastStr: MutableLiveData<String> = MutableLiveData()
    var location: MutableLiveData<String> = MutableLiveData("/榆林有色220kV")
    var timeStr: MutableLiveData<String> = MutableLiveData("12:09:23")
    var synchronizationModel: MutableLiveData<String> = MutableLiveData("内同步，50kHz-300kHz")
    var gainLevelStr: MutableLiveData<String> = MutableLiveData("20dB")
    var gainMinValue: MutableLiveData<Float?> = MutableLiveData()

    lateinit var checkType: CheckType

    lateinit var gainValues: MutableLiveData<ArrayList<Float>>
    var isFile: MutableLiveData<Boolean> = MutableLiveData(false)



    fun start() {
        this.gainValues = dataRepository.getGainValueList()
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

    fun openPulseData() {
        val cmd = CommandHelp.readPulseData(2)
        SocketManager.get().sendData(cmd, CommandType.SendPulse) { bytes ->
            if (cmd.contentEquals(bytes)) {
                Log.d("zhangan", "请求打开读取原始脉冲数据")
            }
        }
    }

    fun closePulseData() {
        val cmd = CommandHelp.stopReadPulseData(2)
        SocketManager.get().sendData(cmd, CommandType.SendPulse) { bytes ->
            if (cmd.contentEquals(bytes)) {
                Log.d("zhangan", "请求关闭读取原始脉冲数据")
            }
        }
    }

}