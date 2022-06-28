package com.mr.mf_pd.application.view.check.real

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mr.mf_pd.application.common.CheckType
import com.mr.mf_pd.application.common.Constants
import com.mr.mf_pd.application.manager.file.CheckFileReadManager
import com.mr.mf_pd.application.manager.socket.SocketManager
import com.mr.mf_pd.application.manager.socket.callback.BytesDataCallback
import com.mr.mf_pd.application.model.CheckParamsBean
import com.mr.mf_pd.application.model.Event
import com.mr.mf_pd.application.model.SettingBean
import com.mr.mf_pd.application.opengl.`object`.FlightPoint2DList
import com.mr.mf_pd.application.opengl.`object`.PrPdPoint2DList
import com.mr.mf_pd.application.opengl.`object`.PrPsCubeList
import com.mr.mf_pd.application.opengl.`object`.PrpsPointList
import com.mr.mf_pd.application.repository.DefaultDataRepository
import com.mr.mf_pd.application.repository.impl.DataRepository
import com.mr.mf_pd.application.repository.impl.FilesRepository
import com.mr.mf_pd.application.utils.DateUtil
import com.mr.mf_pd.application.utils.RepeatActionUtils
import com.mr.mf_pd.application.view.callback.PrPsDataCallback
import com.sito.tool.library.utils.ByteLibUtil
import io.reactivex.disposables.Disposable
import java.io.File
import java.text.DecimalFormat
import java.util.*
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.math.max
import kotlin.math.min

class RealModelViewModel(
    val dataRepository: DataRepository,
    val filesRepository: FilesRepository,
) : ViewModel() {

    lateinit var checkType: CheckType
    var gainValues: MutableLiveData<Vector<Float>> = MutableLiveData()
    var isSaveData: MutableLiveData<Boolean>? = null
    var isFile: MutableLiveData<Boolean> = MutableLiveData(false)
    var toastStr: MutableLiveData<String> = MutableLiveData()
    var location: MutableLiveData<String> = MutableLiveData(filesRepository.getCurrentCheckName())
    var limitValueStr: MutableLiveData<String> = MutableLiveData()
    var showTimeView: MutableLiveData<Boolean> = MutableLiveData<Boolean>(false)
    //图表数据
    var gainMinValue: MutableLiveData<Float?> = MutableLiveData()

    //点数据
    private var dataMaps: ConcurrentHashMap<Int, ConcurrentHashMap<Float, Int>> =
        ConcurrentHashMap()

    //圆柱数据
    var prPsDataCallback: PrPsDataCallback? = null

    var timeStr: MutableLiveData<String> = MutableLiveData()
    var saveDataStartTime: Long = 0
    var mTimeDisposable: Disposable? = null

    private val _toResetEvent = MutableLiveData<Event<Unit>>()
    val toResetEvent: LiveData<Event<Unit>> = _toResetEvent

    fun start() {
        this.isSaveData = filesRepository.isSaveData()
        if (isFile.value!!) {
            this.checkType = filesRepository.getCheckType()
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
        }
    }

    private fun resetFileRead() {
        _toResetEvent.postValue(Event(Unit))
        CheckFileReadManager.get().startReadData()
    }

    val realBytesDataCallback = object : BytesDataCallback {
        override fun onData(source: ByteArray) {
            dealRealData(source)
        }
    }

    var receiverCount = 0
    var mcCount = 0
    var maxValue: Float? = null
    var maxGainValue: Float? = null
    var minValue: Float? = null

    var gainFloatList = Vector<Float>()

    @Volatile
    var canUpdateFz = true
    var emptyCount = 0

    private fun dealRealData(source: ByteArray) {
        if (source.isEmpty() || source.size < 7) return
        val bytes = ByteArray(source.size - 7)
        System.arraycopy(source, 5, bytes, 0, source.size - 7)
        val newValueList: CopyOnWriteArrayList<Float?> = CopyOnWriteArrayList()
        for (j in 0 until Constants.PRPS_COLUMN) {
            newValueList.add(null)
        }
        var startTime = System.currentTimeMillis()
        for (i in 0 until (bytes.size / 6)) {
            val values = ByteArray(6)
            System.arraycopy(bytes, 6 * i, values, 0, 6)
            val row = values[0].toInt()//周期，暂不使用
            val column = values[1].toInt()
            val height = ByteArray(4)
            System.arraycopy(values, 2, height, 0, 4)
            val f = ByteLibUtil.byteArrayToFloat(height)
            maxValue = if (maxValue == null) {
                f
            } else {
                max(f, maxValue!!)
            }
            maxGainValue = if (maxGainValue == null) {
                f
            } else {
                max(f, maxGainValue!!)
            }
            minValue = if (minValue == null) {
                f
            } else {
                min(f, minValue!!)
            }
            //根据设置处理数据
            val setting = checkType.settingBean
            //处理固定尺度
            var value = dealMaxAndMinValue(setting, f)
            //处理偏移量
            val py = setting.xwPy
            val off: Int = if (py in 1..359) {
                val pyValue = (py / 3.6f).toInt()
                if (column + pyValue > 99) {
                    column + pyValue - 100
                } else {
                    column + pyValue
                }
            } else {
                column
            }
            if (off < Constants.PRPS_COLUMN && off >= 0) {
                if ((checkType == CheckType.AA || checkType == CheckType.AE) && value < 0) {
                    value *= -1
                }
                newValueList[off] = value
                if (dataMaps.containsKey(off)) {
                    val map = dataMaps[off]
                    if (map != null && map.containsKey(value)) {
                        val value1 = map[value]
                        if (value1 != null) {
                            map[value] = value1 + 1
                        }
                    } else {
                        map?.set(value, 1)
                    }
                } else {
                    val newMap: ConcurrentHashMap<Float, Int> = ConcurrentHashMap()
                    newMap[value] = 1
                    dataMaps[off] = newMap
                }
            } else {
                Log.d("zhangan", "数据相位异常：$off")
            }
            mcCount++
        }
        if (receiverCount % 5 == 0) {
            if (maxGainValue != null) {
                gainFloatList.add(maxGainValue!!)
            }
            if (gainFloatList.size > checkType.settingBean.ljTime * 10) {
                gainFloatList.removeFirstOrNull()
            }
            gainValues.postValue(gainFloatList)
            maxGainValue = null
        }
        if (receiverCount == 50) { //一秒钟刷新一次数据
            canUpdateFz = emptyCount != 50
            val checkParamsBean: CheckParamsBean? = checkType.checkParams.value
            if (canUpdateFz && maxValue != null) {
                val df1 = DecimalFormat("0.00")
                checkParamsBean?.fzAttr = df1.format(maxValue) + checkType.settingBean.fzUnit
            }
            checkParamsBean?.mcCountAttr = "${mcCount}个/秒"
            updateFzValue(checkParamsBean)
            receiverCount = 0
            mcCount = 0
            maxValue = null
            emptyCount = 0
            gainMinValue.postValue(minValue)
        } else {
            ++receiverCount
            if (bytes.isEmpty()) {
                ++emptyCount
            } else {
                canUpdateFz = true
                emptyCount = 0
            }
        }
        prPsDataCallback?.prpsDataChange(dataMaps, newValueList)
    }

    @Synchronized
    fun updateFzValue(checkParamsBean: CheckParamsBean?) {
        checkType.checkParams.postValue(checkParamsBean)
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
            val maxValue = max(PrPsCubeList.maxValue, f)
            val minValue = min(PrPsCubeList.minValue, f)
            PrPsCubeList.maxValue = maxValue
            PrPsCubeList.minValue = minValue
            PrpsPointList.maxValue = maxValue
            PrpsPointList.minValue = minValue
        }
        return value
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

    fun setCheckFile(filePath: String) {
        val file = File(filePath)
        filesRepository.setCurrentClickFile(file)
        location.postValue(filesRepository.getCurrentCheckName())
        createACheckFile()
    }

    fun createACheckFile() {
        filesRepository.toCreateCheckFile(checkType)
    }

    fun cleanCurrentData() {
        this.gainValues.postValue(null)
        this.dataMaps = ConcurrentHashMap()
        this.gainFloatList.clear()
        prPsDataCallback?.prpsDataChange(this.dataMaps, CopyOnWriteArrayList())
        updateSettingValue()

    }

    private fun updateSettingValue() {
        checkType.settingBean.let {
            PrPdPoint2DList.maxValue = it.maxValue.toFloat()
            PrPdPoint2DList.minValue = it.minValue.toFloat()

            PrpsPointList.maxValue = it.maxValue.toFloat()
            PrpsPointList.minValue = it.minValue.toFloat()

            PrPsCubeList.maxValue = it.maxValue.toFloat()
            PrPsCubeList.minValue = it.minValue.toFloat()

            FlightPoint2DList.maxValue = it.maxValue.toFloat()
            FlightPoint2DList.minValue = it.minValue.toFloat()
        }
        val setting = checkType.settingBean
        if (setting.gdCd == 0) {
            DefaultDataRepository.realDataMaxValue.postValue(setting.maxValue)
            DefaultDataRepository.realDataMinValue.postValue(setting.minValue)
            PrPsCubeList.maxValue = setting.maxValue.toFloat()
            PrPsCubeList.minValue = setting.minValue.toFloat()
            PrpsPointList.maxValue = setting.maxValue.toFloat()
            PrpsPointList.minValue = setting.minValue.toFloat()
        }
    }

    fun getQueue(): ArrayBlockingQueue<ByteArray>? {
        return if (isFile.value!!) {
            CheckFileReadManager.get().realDataDeque
        } else {
            SocketManager.get().realDataDeque
        }
    }

    override fun onCleared() {
        super.onCleared()
        prPsDataCallback = null
        mTimeDisposable?.dispose()
    }
}