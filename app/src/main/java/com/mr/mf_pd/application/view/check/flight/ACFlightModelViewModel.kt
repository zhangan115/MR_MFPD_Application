package com.mr.mf_pd.application.view.check.flight

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mr.mf_pd.application.common.CheckType
import com.mr.mf_pd.application.manager.file.CheckFileReadManager
import com.mr.mf_pd.application.manager.socket.SocketManager
import com.mr.mf_pd.application.manager.socket.callback.BytesDataCallback
import com.mr.mf_pd.application.model.Event
import com.mr.mf_pd.application.model.SettingBean
import com.mr.mf_pd.application.opengl.`object`.PrPdPoint2DList
import com.mr.mf_pd.application.repository.DefaultDataRepository
import com.mr.mf_pd.application.repository.impl.DataRepository
import com.mr.mf_pd.application.repository.impl.FilesRepository
import com.mr.mf_pd.application.utils.DateUtil
import com.mr.mf_pd.application.utils.RepeatActionUtils
import com.mr.mf_pd.application.view.callback.FlightDataCallback
import com.sito.tool.library.utils.ByteLibUtil
import io.reactivex.disposables.Disposable
import java.io.File
import java.text.DecimalFormat
import java.util.*
import java.util.concurrent.ArrayBlockingQueue
import kotlin.collections.HashMap
import kotlin.math.max
import kotlin.math.min

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
    var showTimeView: MutableLiveData<Boolean> = MutableLiveData<Boolean>(false)

    private var dataMaps: HashMap<Int, HashMap<Float, Int>> = HashMap()

    var timeStr: MutableLiveData<String> = MutableLiveData()
    var saveDataStartTime: Long = 0
    var mTimeDisposable: Disposable? = null
    val df1 = DecimalFormat("0.00")
    var receiverCount = 0

    private val _toResetEvent = MutableLiveData<Event<Unit>>()
    val toResetEvent: LiveData<Event<Unit>> = _toResetEvent

    fun start() {
        this.isSaveData = filesRepository.isSaveData()
        if (isFile.value!!) {
            this.checkType = filesRepository.getCheckType()
            showTimeView.postValue(true)
            val checkDataFileModel = filesRepository.getCheckFileModel()
            this.showTimeView.postValue(true)
            var startTime = 0L
            checkDataFileModel?.let {
                mTimeDisposable = RepeatActionUtils.execute {
                    it.dataTime?.let {
                        timeStr.postValue(DateUtil.timeFormat((it - startTime), "mm:ss"))
                        startTime += 1000L
                        if (startTime > it) {
                            resetFileRead()
                            startTime = 0
                        }
                    }
                }
            }
        } else {
            this.checkType = dataRepository.getCheckType()
            showTimeView.postValue(false)
        }
    }

    /**
     * 重新从文件中读取
     */
    private fun resetFileRead() {
        _toResetEvent.postValue(Event(Unit))
        CheckFileReadManager.get().startReadData()
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
            val count  =  ByteLibUtil.mergeByte2Int(source[3],source[4])
            System.arraycopy(source, 5, bytes, 0, source.size - 7)
            if (bytes.isNotEmpty() && bytes.size % 6 == 0) {
                for (i in 0 until (bytes.size / 6)) {
                    val values = ByteArray(6)
                    System.arraycopy(bytes, 6 * i, values, 0, 6)
                    val height = ByteArray(4)
                    System.arraycopy(values, 2, height, 0, 4)
                    val key = ByteLibUtil.mergeByte2Int(values[0],values[1])
                    val f = df1.format(ByteLibUtil.byteArrayToFloat(height)).toFloat()
                    //根据设置处理数据
                    val setting = checkType.settingBean
                    //处理固定尺度
                    var value = dealMaxAndMinValue(setting, f)
                    maxXValue = max(key, maxXValue)
                    maxGainValue = if (maxGainValue == null) {
                        value
                    } else {
                        max(value, maxGainValue!!)
                    }
                    if ((checkType == CheckType.AA || checkType == CheckType.AE) && value < 0) {
                        value *= -1
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


    private fun dealMaxAndMinValue(
        setting: SettingBean,
        f: Float,
    ): Float {
        var value = f
        if (setting.gdCd == 1) {
            if (f > setting.maxValue) {
                value = setting.maxValue.toFloat()
            } else if (f < setting.minValue) {
                value = setting.minValue.toFloat()
            }
        } else {
            if (DefaultDataRepository.realDataMaxValue.value != null) {
                val maxValue = max(DefaultDataRepository.realDataMaxValue.value!!, f.toInt())
                if (maxValue != DefaultDataRepository.realDataMaxValue.value!!) {
                    DefaultDataRepository.realDataMaxValue.postValue(maxValue)
                }
            } else {
                DefaultDataRepository.realDataMaxValue.postValue(setting.maxValue)
            }
            if (DefaultDataRepository.realDataMinValue.value != null) {
                val minValue = min(DefaultDataRepository.realDataMinValue.value!!, f.toInt())
                if (minValue != DefaultDataRepository.realDataMinValue.value!!) {
                    DefaultDataRepository.realDataMinValue.postValue(minValue)
                }
            } else {
                DefaultDataRepository.realDataMinValue.postValue(setting.minValue)
            }
            val maxValue = max(PrPdPoint2DList.maxValue, f)
            val minValue = min(PrPdPoint2DList.minValue, f)
            PrPdPoint2DList.maxValue = maxValue
            PrPdPoint2DList.minValue = minValue
        }
        return value
    }


    fun setState(stateStr: String?) {
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

    override fun onCleared() {
        super.onCleared()
        flightCallback = null
    }
}