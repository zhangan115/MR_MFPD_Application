package com.mr.mf_pd.application.view.check.real

import android.util.Log
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
import com.mr.mf_pd.application.opengl.`object`.PrPsCubeList
import com.mr.mf_pd.application.opengl.`object`.PrpsPointList
import com.mr.mf_pd.application.repository.DefaultDataRepository
import com.mr.mf_pd.application.repository.impl.DataRepository
import com.mr.mf_pd.application.repository.impl.FilesRepository
import com.mr.mf_pd.application.utils.ByteUtil
import com.mr.mf_pd.application.view.callback.PrPsDataCallback
import java.io.File
import java.text.DecimalFormat
import java.util.*
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.collections.HashMap
import kotlin.math.max
import kotlin.math.min

class RealModelViewModel(
    val dataRepository: DataRepository,
    private val filesRepository: FilesRepository,
) : ViewModel() {

    lateinit var checkType: CheckType
    var gainValues: MutableLiveData<Vector<Float>> = MutableLiveData()
    var isSaveData: MutableLiveData<Boolean>? = null
    var isFile: MutableLiveData<Boolean> = MutableLiveData(false)
    var toastStr: MutableLiveData<String> = MutableLiveData()
    var location: MutableLiveData<String> = MutableLiveData(filesRepository.getCurrentCheckName())

    //图表数据
    var gainMinValue: MutableLiveData<Float?> = MutableLiveData()

    //点数据
    private var dataMaps: HashMap<Int, HashMap<Float, Int>> = HashMap()

    //圆柱数据
    var prPsDataCallback: PrPsDataCallback? = null
    var toUpdateGl: (() -> Unit)? = null

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
            if (filesRepository.isSaveData()?.value == true) {
                filesRepository.toSaveRealData2File(source)
            }
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

    private fun dealRealData(source: ByteArray) {
        if (source.isEmpty() || source.size < 7) return
        val bytes = ByteArray(source.size - 7)
        canUpdateFz = bytes.isNotEmpty()
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
            val f = ByteUtil.getFloat(height)
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
            val value = dealMaxAndMinValue(setting, f)
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
                newValueList[off] = value
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
            } else {
                Log.d("zhangan", "数据相位异常：$column")
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
        } else {
            ++receiverCount
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
        filesRepository.startSaveData()
    }

    fun stopSaveData() {
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
        this.dataMaps = HashMap()
        this.gainFloatList.clear()
        prPsDataCallback?.prpsDataChange(this.dataMaps, CopyOnWriteArrayList())
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
    }

    fun onResume() {
        if (isFile.value!!) {
            CheckFileReadManager.get().addCallBack(CommandType.ReadYcData, ycBytesDataCallback)
            CheckFileReadManager.get().addCallBack(CommandType.RealData, realBytesDataCallback)
        } else {
            SocketManager.get().addCallBack(CommandType.ReadYcData, ycBytesDataCallback)
//            SocketManager.get().addCallBack(CommandType.RealData, realBytesDataCallback)
        }
    }

    fun onPause() {
        if (isFile.value!!) {
            CheckFileReadManager.get().removeCallBack(CommandType.ReadYcData, ycBytesDataCallback)
            CheckFileReadManager.get().removeCallBack(CommandType.RealData, realBytesDataCallback)
        } else {
            SocketManager.get().removeCallBack(CommandType.ReadYcData, ycBytesDataCallback)
//            SocketManager.get().removeCallBack(CommandType.RealData, realBytesDataCallback)
        }

    }
}