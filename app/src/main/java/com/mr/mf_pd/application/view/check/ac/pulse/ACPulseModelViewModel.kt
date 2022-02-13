package com.mr.mf_pd.application.view.check.ac.pulse

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mr.mf_pd.application.manager.socket.CommandHelp
import com.mr.mf_pd.application.manager.socket.CommandType
import com.mr.mf_pd.application.manager.socket.ReceiverCallback
import com.mr.mf_pd.application.manager.socket.SocketManager
import com.mr.mf_pd.application.repository.impl.DataRepository

class ACPulseModelViewModel(val repository: DataRepository) : ViewModel() {

    var toastStr: MutableLiveData<String> = MutableLiveData()
    var location: MutableLiveData<String> = MutableLiveData("/榆林有色220kV")
    var timeStr: MutableLiveData<String> = MutableLiveData("12:09:23")
    var synchronizationModel: MutableLiveData<String> = MutableLiveData("内同步，50kHz-300kHz")
    var gainLevelStr: MutableLiveData<String> = MutableLiveData("20dB")

    lateinit var gainValues: MutableLiveData<List<Float>>

    fun start() {
        this.gainValues = repository.getGainValueList()
    }

    fun openPulseData() {
        val cmd = CommandHelp.readPulseData(2)
        SocketManager.getInstance().sendData(cmd,CommandType.SendPulse) { bytes ->
            if (cmd.contentEquals(bytes)) {
                Log.d("zhangan", "请求打开读取原始脉冲数据")
            }
        }
    }

    fun closePulseData() {
        val cmd = CommandHelp.stopReadPulseData(2)
        SocketManager.getInstance().sendData(cmd,CommandType.SendPulse) { bytes ->
            if (cmd.contentEquals(bytes)) {
                Log.d("zhangan", "请求关闭读取原始脉冲数据")
            }
        }
    }

}