package com.mr.mf_pd.application.view.check.flight

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mr.mf_pd.application.common.CheckType
import com.mr.mf_pd.application.common.Constants
import com.mr.mf_pd.application.manager.socket.SocketManager
import com.mr.mf_pd.application.manager.socket.callback.BaseDataCallback
import com.mr.mf_pd.application.repository.DefaultDataRepository
import com.mr.mf_pd.application.repository.callback.RealDataCallback
import com.mr.mf_pd.application.repository.impl.DataRepository
import com.mr.mf_pd.application.repository.impl.FilesRepository
import com.mr.mf_pd.application.utils.ByteUtil
import com.sito.tool.library.utils.ByteLibUtil
import java.util.*
import kotlin.math.max
import kotlin.math.min

class ACFlightModelViewModel(
    val dataRepository: DataRepository,
    val filesRepository: FilesRepository,
) : ViewModel() {

    var toastStr: MutableLiveData<String> = MutableLiveData()
    var location: MutableLiveData<String> = MutableLiveData(filesRepository.getCurrentCheckName())
    var timeStr: MutableLiveData<String> = MutableLiveData("12:09:23")
    var synchronizationModel: MutableLiveData<String> = MutableLiveData("内同步，50kHz-300kHz")
    var gainLevelStr: MutableLiveData<String> = MutableLiveData("20dB")
    var gainMinValue: MutableLiveData<Float?> = MutableLiveData()
    lateinit var gainValues: MutableLiveData<Vector<Float>>
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
            dataRepository.switchPassageway(checkType.passageway, 2)
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
            SocketManager.get().flightValueCallback = flightValueCallBack
        }
    }

    private val flightValueCallBack = object : BaseDataCallback {
        override fun onData(source: ByteArray) {
            val bytes = ByteArray(source.size - 7)
            System.arraycopy(source, 5, bytes, 0, source.size - 7)
            val newValueList: ArrayList<Float?> = ArrayList()
            val xValueList: ArrayList<Int?> = ArrayList()
            if (bytes.isNotEmpty() && bytes.size % 6 == 0) {
                for (i in 0 until (bytes.size / 6)) {
                    val values = ByteArray(6)
                    System.arraycopy(bytes, 6 * i, values, 0, 6)
                    val lengthBytes = byteArrayOf(0x00, 0x00, values[0], values[1])
                    xValueList.add(ByteLibUtil.getInt(lengthBytes))
                    val height = ByteArray(4)
                    System.arraycopy(values, 2, height, 0, 4)
                    val f = ByteUtil.getFloat(height)
                    newValueList.add(f)
                }
            }
            Log.d("zhangan", xValueList.size.toString())
            Log.d("zhangan", newValueList.size.toString())
        }
    }


    override fun onCleared() {
        super.onCleared()

    }
}