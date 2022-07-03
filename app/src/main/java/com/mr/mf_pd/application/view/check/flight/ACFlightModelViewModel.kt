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
import com.mr.mf_pd.application.utils.DateUtil
import com.mr.mf_pd.application.utils.RepeatActionUtils
import com.mr.mf_pd.application.view.callback.FlightDataCallback
import com.sito.tool.library.utils.ByteLibUtil
import io.reactivex.disposables.Disposable
import java.io.File
import java.util.*
import java.util.concurrent.ArrayBlockingQueue
import kotlin.collections.HashMap
import kotlin.math.max

class ACFlightModelViewModel(
    val dataRepository: DataRepository,
    val filesRepository: FilesRepository,
) : ViewModel() {

    var toastStr: MutableLiveData<String> = MutableLiveData()
    var fdStateStr: MutableLiveData<String> = MutableLiveData()
    var location: MutableLiveData<String> = MutableLiveData(filesRepository.getCurrentCheckName())
    var limitValueStr: MutableLiveData<String> = MutableLiveData()
    lateinit var checkType: CheckType
    var isFile: MutableLiveData<Boolean> = MutableLiveData(false)
    var gainValues: MutableLiveData<Vector<Float>> = MutableLiveData()
    var gainMinValue: MutableLiveData<Float?> = MutableLiveData()
    var isSaveData: MutableLiveData<Boolean>? = null

    private var dataMaps: HashMap<Int, HashMap<Float, Int>> = HashMap()

    var timeStr: MutableLiveData<String> = MutableLiveData()
    var saveDataStartTime: Long = 0
    var mTimeDisposable: Disposable? = null

    var receiverCount = 0

    fun start() {
        if (isFile.value!!) {
            this.checkType = filesRepository.getCheckType()
        } else {
            this.checkType = dataRepository.getCheckType()
        }
    }

    private var flightCallback: FlightDataCallback? = null

    fun setFlightCallback(callback: FlightDataCallback) {
        flightCallback = callback
    }

    private var maxGainValue: Float? = null

    val flightValueCallBack = object : BytesDataCallback {
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
                    val value = ByteLibUtil.byteArrayToFloat(height)
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

    fun setState(stateStr: String) {
        fdStateStr.value = stateStr
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
        saveDataStartTime = System.currentTimeMillis()
        filesRepository.startSaveData()
        mTimeDisposable?.dispose()
        mTimeDisposable = RepeatActionUtils.execute {
            val time = System.currentTimeMillis() - saveDataStartTime
            timeStr.postValue(DateUtil.timeFormat(time, "mm:ss"))
        }
    }

    fun stopSaveData() {
        mTimeDisposable?.dispose()
        filesRepository.stopSaveData()
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

    fun getQueue(): ArrayBlockingQueue<ByteArray>?{
        return if (isFile.value!!) {
            CheckFileReadManager.get().flightDeque
        } else {
            SocketManager.get().flightDeque
        }
    }

    fun onResume() {

    }

    fun onPause() {

    }

    override fun onCleared() {
        super.onCleared()
        flightCallback = null
    }
}