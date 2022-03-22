package com.mr.mf_pd.application.view.check.flight

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mr.mf_pd.application.common.CheckType
import com.mr.mf_pd.application.manager.socket.SocketManager
import com.mr.mf_pd.application.manager.socket.callback.BaseDataCallback
import com.mr.mf_pd.application.repository.callback.RealDataCallback
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
    var gainMinValue: MutableLiveData<Float?> = MutableLiveData()
    lateinit var gainValues: MutableLiveData<Vector<Float>>
    lateinit var checkType: CheckType
    var isFile: MutableLiveData<Boolean> = MutableLiveData(false)

    var isSaveData: MutableLiveData<Boolean>? = null

    private val dataMaps: HashMap<Int, HashMap<Float, Int>> = HashMap()

    var receiverCount = 0

    fun start() {
        this.gainValues = dataRepository.getGainValueList()
        if (isFile.value!!) {
            this.checkType = filesRepository.getCheckType()
            filesRepository.addDataListener()
        } else {
            this.checkType = dataRepository.getCheckType()
            dataRepository.switchPassageway(checkType.passageway, checkType.commandType)
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

    private var flightCallback: FlightDataCallback? = null

    fun setFlightCallback(callback: FlightDataCallback) {
        flightCallback = callback
    }

    private var maxGainValue: Float? = null

    private val flightValueCallBack = object : BaseDataCallback {
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
        flightCallback = null
        dataRepository.removeRealDataListener()
    }
}