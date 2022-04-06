package com.mr.mf_pd.application.view.check.pulse

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mr.mf_pd.application.common.CheckType
import com.mr.mf_pd.application.manager.file.CheckFileReadManager
import com.mr.mf_pd.application.manager.socket.SocketManager
import com.mr.mf_pd.application.manager.socket.callback.BytesDataCallback
import com.mr.mf_pd.application.manager.socket.comand.CommandType
import com.mr.mf_pd.application.repository.impl.DataRepository
import com.mr.mf_pd.application.repository.impl.FilesRepository
import com.mr.mf_pd.application.utils.ByteUtil
import com.mr.mf_pd.application.view.callback.FlightDataCallback
import com.sito.tool.library.utils.ByteLibUtil
import java.util.*
import java.util.concurrent.ArrayBlockingQueue
import kotlin.math.max

class ACPulseModelViewModel(val dataRepository: DataRepository, val filesRepository: FilesRepository) : ViewModel() {

    var toastStr: MutableLiveData<String> = MutableLiveData()
    var location: MutableLiveData<String> = MutableLiveData()
    var timeStr: MutableLiveData<String> = MutableLiveData()
    var synchronizationModel: MutableLiveData<String> = MutableLiveData()
    var gainLevelStr: MutableLiveData<String> = MutableLiveData()
    var gainMinValue: MutableLiveData<Float?> = MutableLiveData()

    lateinit var checkType: CheckType

    var gainValues: MutableLiveData<Vector<Float>> = MutableLiveData(Vector<Float>())
    var isFile: MutableLiveData<Boolean> = MutableLiveData(false)

    fun start() {
        if (isFile.value!!) {
            this.checkType = filesRepository.getCheckType()
        } else {
            this.checkType = dataRepository.getCheckType()
        }
    }
    var isSaveData: MutableLiveData<Boolean>? = null

    fun startSaveData() {
        filesRepository.startSaveData()
    }

    fun stopSaveData() {
        filesRepository.stopSaveData()
    }

    fun getQueue(): ArrayBlockingQueue<ByteArray>?{
        return if (isFile.value!!) {
            CheckFileReadManager.get().fdDataDeque
        } else {
            SocketManager.get().fdDataDeque
        }
    }

    fun onResume() {
        if (isFile.value!!) {
            CheckFileReadManager.get().addCallBack(CommandType.FdData, fdValueCallBack)
        } else {
            SocketManager.get().addCallBack(CommandType.FdData, fdValueCallBack)
        }
    }

    fun onPause() {
        if (isFile.value!!) {
            CheckFileReadManager.get().removeCallBack(CommandType.FdData, fdValueCallBack)
        } else {
            SocketManager.get().removeCallBack(CommandType.FdData, fdValueCallBack)
        }
    }

    private var maxGainValue: Float? = null
    private var dataMaps: HashMap<Int, HashMap<Float, Int>> = HashMap()
    var receiverCount = 0

    private var flightCallback: FlightDataCallback? = null

    fun setFlightCallback(callback: FlightDataCallback) {
        flightCallback = callback
    }

    fun cleanCurrentData() {
        receiverCount = 0
        val list = Vector<Float>()
        dataMaps = HashMap()
        if (isFile.value == true) {
            this.gainValues.postValue(list)
        } else {
            this.gainValues.postValue(list)
        }
    }


    val fdValueCallBack = object : BytesDataCallback {
        override fun onData(source: ByteArray) {
            if (source.isEmpty() || source.size < 7) return
            var maxXValue = -1
            val bytes = ByteArray(source.size - 7)
            System.arraycopy(source, 5, bytes, 0, source.size - 7)
            if (bytes.isNotEmpty() && bytes.size % 6 == 0) {
                for (i in 0 until (bytes.size / 6)) {
                    val values = ByteArray(6)
                    System.arraycopy(bytes, 6 * i, values, 0, 6)
                    val lengthBytes = byteArrayOf(0x00, 0x00, values[0], values[1])
                    val height = ByteArray(4)
                    System.arraycopy(values, 2, height, 0, 4)
                    val value = ByteUtil.getFloat(height)
                    val key = ByteLibUtil.getInt(lengthBytes)
                    maxXValue = max(key, maxXValue)
                    maxGainValue = if (maxGainValue == null) {
                        value
                    } else {
                        max(value, maxGainValue!!)
                    }
                    if (dataMaps.containsKey(key)) {
                        val map = dataMaps[key]
                        if (map != null && map.containsKey(value)) {
                            val value1 = map[value]
                            if (value1 != null) {
                                map[value] = value1 + 1
                            }
                        } else {
                            map?.set(value, 1)
                        }
                    } else {
                        val newMap: HashMap<Float, Int> = HashMap()
                        newMap[value] = 1
                        dataMaps[key] = newMap
                    }
                }
            }
            if (receiverCount % 5 == 0) {
                if (maxGainValue != null) {
                    gainValues.value?.add(maxGainValue!!)
                }
                if (gainValues.value != null) {
                    if (gainValues.value!!.size > checkType.settingBean.ljTime * 10) {
                        gainValues.value?.removeFirstOrNull()
                    }
                }
                gainValues.postValue(gainValues.value)
                maxGainValue = null
            }
            receiverCount++
            flightCallback?.flightData(dataMaps, maxXValue)
        }

    }

}