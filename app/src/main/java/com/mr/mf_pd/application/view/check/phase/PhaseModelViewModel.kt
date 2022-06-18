package com.mr.mf_pd.application.view.check.phase

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mr.mf_pd.application.common.CheckType
import com.mr.mf_pd.application.common.Constants
import com.mr.mf_pd.application.manager.file.CheckFileReadManager
import com.mr.mf_pd.application.manager.socket.SocketManager
import com.mr.mf_pd.application.manager.socket.callback.BytesDataCallback
import com.mr.mf_pd.application.manager.socket.comand.CommandType
import com.mr.mf_pd.application.model.CheckParamsBean
import com.mr.mf_pd.application.model.SettingBean
import com.mr.mf_pd.application.opengl.`object`.FlightPoint2DList
import com.mr.mf_pd.application.opengl.`object`.PrPdPoint2DList
import com.mr.mf_pd.application.opengl.`object`.PrPsCubeList
import com.mr.mf_pd.application.opengl.`object`.PrpsPointList
import com.mr.mf_pd.application.repository.DefaultDataRepository
import com.mr.mf_pd.application.repository.impl.DataRepository
import com.mr.mf_pd.application.repository.impl.FilesRepository
import com.mr.mf_pd.application.utils.ByteUtil
import com.mr.mf_pd.application.utils.DateUtil
import com.mr.mf_pd.application.utils.RepeatActionUtils
import com.sito.tool.library.utils.ByteLibUtil
import io.reactivex.disposables.Disposable
import java.io.File
import java.text.DecimalFormat
import java.util.*
import java.util.concurrent.ArrayBlockingQueue
import kotlin.collections.HashMap
import kotlin.math.max
import kotlin.math.min

class PhaseModelViewModel(
    val dataRepository: DataRepository,
    val filesRepository: FilesRepository,
) : ViewModel() {

    lateinit var checkType: CheckType

    var dataMaps: HashMap<Int, HashMap<Float, Int>> = HashMap()

    var gainValues: MutableLiveData<Vector<Float>> = MutableLiveData()
    var gainMinValue: MutableLiveData<Float?> = MutableLiveData()//图表的最低值

    var toastStr: MutableLiveData<String> = MutableLiveData()

    var isSaveData: MutableLiveData<Boolean>? = null

    var isFile: MutableLiveData<Boolean> = MutableLiveData(false)

    var limitValueStr: MutableLiveData<String> = MutableLiveData()
    var location: MutableLiveData<String> = MutableLiveData(filesRepository.getCurrentCheckName())

    var timeStr: MutableLiveData<String> = MutableLiveData()
    var saveDataStartTime: Long = 0
    var mTimeDisposable: Disposable? = null


    fun start() {
        this.isSaveData = filesRepository.isSaveData()
        if (isFile.value!!) {
            this.checkType = filesRepository.getCheckType()
        } else {
            this.checkType = dataRepository.getCheckType()
        }
    }

    private val ycBytesDataCallback = object : BytesDataCallback {
        override fun onData(source: ByteArray) {
            if (filesRepository.isSaveData()?.value == true) {
                filesRepository.toSaveYCData2File(source)
            }
        }
    }

    val realBytesDataCallback = object : BytesDataCallback {
        override fun onData(source: ByteArray) {
            dealRealData(source)
            dataCallback?.invoke(dataMaps)
            if (filesRepository.isSaveData()?.value == true) {
                filesRepository.toSaveRealData2File(source)
            }
        }
    }

    fun getQueue(): ArrayBlockingQueue<ByteArray>? {
        return if (isFile.value!!) {
            CheckFileReadManager.get().realDataDeque
        } else {
            SocketManager.get().realDataDeque
        }
    }

    var dataCallback: ((data: HashMap<Int, HashMap<Float, Int>>) -> Unit)? = null

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
        this.gainValues.postValue(null)
        this.dataMaps = HashMap()
        this.gainFloatList.clear()
        this.dataCallback?.invoke(dataMaps)
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
            if (DefaultDataRepository.realDataMaxValue.value != null) {
                val maxValue = max(DefaultDataRepository.realDataMaxValue.value!!, setting.maxValue)
                if (maxValue != DefaultDataRepository.realDataMaxValue.value!!) {
                    DefaultDataRepository.realDataMaxValue.postValue(maxValue)
                }
            } else {
                DefaultDataRepository.realDataMaxValue.postValue(setting.maxValue)
            }
            if (DefaultDataRepository.realDataMinValue.value != null) {
                val minValue = min(DefaultDataRepository.realDataMinValue.value!!, setting.minValue)
                if (minValue != DefaultDataRepository.realDataMinValue.value!!) {
                    DefaultDataRepository.realDataMinValue.postValue(minValue)
                }
            } else {
                DefaultDataRepository.realDataMinValue.postValue(setting.minValue)
            }
            PrPdPoint2DList.maxValue = setting.maxValue.toFloat()
            PrPdPoint2DList.minValue = setting.minValue.toFloat()
        }
    }

    override fun onCleared() {
        super.onCleared()
        this.dataCallback = null
        mTimeDisposable?.dispose()
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
                if (dataMaps.containsKey(column)) {
                    val map = dataMaps[column]
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
                    dataMaps[column] = newMap
                }
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
        } else {
            ++receiverCount
            if (bytes.isEmpty()) {
                ++emptyCount
            } else {
                emptyCount = 0
                canUpdateFz = true
            }
        }
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
            val maxValue = max(PrPdPoint2DList.maxValue, f)
            val minValue = min(PrPdPoint2DList.minValue, f)
            PrPdPoint2DList.maxValue = maxValue
            PrPdPoint2DList.minValue = minValue
        }
        return value
    }

    fun onResume() {
        if (isFile.value!!) {
            CheckFileReadManager.get().addCallBack(CommandType.ReadYcData, ycBytesDataCallback)
            CheckFileReadManager.get().addCallBack(CommandType.RealData, realBytesDataCallback)
        } else {
            SocketManager.get().addCallBack(CommandType.ReadYcData, ycBytesDataCallback)
            SocketManager.get().addCallBack(CommandType.RealData, realBytesDataCallback)
        }
    }

    fun onPause() {
        if (isFile.value!!) {
            CheckFileReadManager.get().removeCallBack(CommandType.ReadYcData, ycBytesDataCallback)
            CheckFileReadManager.get().removeCallBack(CommandType.RealData, realBytesDataCallback)
        } else {
            SocketManager.get().removeCallBack(CommandType.ReadYcData, ycBytesDataCallback)
            SocketManager.get().removeCallBack(CommandType.RealData, realBytesDataCallback)
        }
    }
}