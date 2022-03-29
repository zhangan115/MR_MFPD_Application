package com.mr.mf_pd.application.view.check.flight

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
import java.io.File
import java.util.*
import kotlin.collections.HashMap
import kotlin.math.max

class ACFlightModelViewModel(
    val dataRepository: DataRepository,
    val filesRepository: FilesRepository,
) : ViewModel() {

    var toastStr: MutableLiveData<String> = MutableLiveData()
    var location: MutableLiveData<String> = MutableLiveData(filesRepository.getCurrentCheckName())
    var timeStr: MutableLiveData<String> = MutableLiveData()
    var synchronizationModel: MutableLiveData<String> = MutableLiveData("内同步，50kHz-300kHz")
    var gainLevelStr: MutableLiveData<String> = MutableLiveData("20dB")
    lateinit var checkType: CheckType
    var isFile: MutableLiveData<Boolean> = MutableLiveData(false)
    var gainValues: MutableLiveData<Vector<Float>> = MutableLiveData()
    var gainMinValue: MutableLiveData<Float?> = MutableLiveData()
    var isSaveData: MutableLiveData<Boolean>? = null

    private val dataMaps: HashMap<Int, HashMap<Float, Int>> = HashMap()

    var receiverCount = 0

    fun start() {
        if (isFile.value!!) {
            this.checkType = filesRepository.getCheckType()
        } else {
            this.checkType = dataRepository.getCheckType()
            dataRepository.switchPassageway(checkType.passageway, checkType.commandType)
            SocketManager.get().addCallBack(CommandType.ReadYcData, ycBytesDataCallback)
            SocketManager.get().addCallBack(CommandType.RealData, realBytesDataCallback)
            SocketManager.get().addCallBack(CommandType.FlightValue, flightValueCallBack)
        }
    }

    private val ycBytesDataCallback = object : BytesDataCallback {
        override fun onData(source: ByteArray) {
            if (filesRepository.isSaveData()?.value == true) {
                filesRepository.toSaveYCData2File(source)
            }
        }
    }

    private val realBytesDataCallback = object : BytesDataCallback {
        override fun onData(source: ByteArray) {
            if (filesRepository.isSaveData()?.value == true) {
                filesRepository.toSaveRealData2File(source)
            }
        }

    }

    private var flightCallback: FlightDataCallback? = null

    fun setFlightCallback(callback: FlightDataCallback) {
        flightCallback = callback
    }

    private var maxGainValue: Float? = null

    private val flightValueCallBack = object : BytesDataCallback {
        override fun onData(source: ByteArray) {
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
            flightCallback?.flightData(dataMaps)
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
        receiverCount = 0
        val list = Vector<Float>()
        if (isFile.value == true) {
            this.gainValues.postValue(list)
        } else {
            this.gainValues.postValue(list)
        }
    }


    fun onResume() {
        if (isFile.value!!) {
            CheckFileReadManager.get().addCallBack(CommandType.ReadYcData, ycBytesDataCallback)
            CheckFileReadManager.get().addCallBack(CommandType.RealData, realBytesDataCallback)
            CheckFileReadManager.get().addCallBack(CommandType.FlightValue, flightValueCallBack)
        } else {
            SocketManager.get().addCallBack(CommandType.ReadYcData, ycBytesDataCallback)
            SocketManager.get().addCallBack(CommandType.RealData, realBytesDataCallback)
            SocketManager.get().addCallBack(CommandType.FlightValue, flightValueCallBack)
        }
    }

    fun onPause() {
        if (isFile.value!!) {
            CheckFileReadManager.get().removeCallBack(CommandType.ReadYcData, ycBytesDataCallback)
            CheckFileReadManager.get().removeCallBack(CommandType.RealData, realBytesDataCallback)
            CheckFileReadManager.get().removeCallBack(CommandType.FlightValue, flightValueCallBack)
        } else {
            SocketManager.get().removeCallBack(CommandType.ReadYcData, ycBytesDataCallback)
            SocketManager.get().removeCallBack(CommandType.RealData, realBytesDataCallback)
            SocketManager.get().removeCallBack(CommandType.FlightValue, flightValueCallBack)
        }
    }

    override fun onCleared() {
        super.onCleared()
        flightCallback = null
    }
}