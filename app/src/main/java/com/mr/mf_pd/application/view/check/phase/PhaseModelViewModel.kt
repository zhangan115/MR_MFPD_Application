package com.mr.mf_pd.application.view.check.phase

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mr.mf_pd.application.common.CheckType
import com.mr.mf_pd.application.common.Constants
import com.mr.mf_pd.application.manager.socket.SocketManager
import com.mr.mf_pd.application.manager.socket.callback.BytesDataCallback
import com.mr.mf_pd.application.manager.socket.comand.CommandType
import com.mr.mf_pd.application.model.CheckParamsBean
import com.mr.mf_pd.application.repository.DefaultDataRepository
import com.mr.mf_pd.application.repository.callback.RealDataCallback
import com.mr.mf_pd.application.repository.impl.DataRepository
import com.mr.mf_pd.application.repository.impl.FilesRepository
import com.mr.mf_pd.application.utils.ByteUtil
import com.mr.mf_pd.application.view.callback.FlightDataCallback
import com.mr.mf_pd.application.view.callback.PrPsDataCallback
import com.mr.mf_pd.application.view.opengl.`object`.PrPsCubeList
import java.io.File
import java.text.DecimalFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.math.max
import kotlin.math.min

class PhaseModelViewModel(
    val dataRepository: DataRepository,
    val filesRepository: FilesRepository,
) : ViewModel() {

    lateinit var checkType: CheckType

    var gainValues: MutableLiveData<Vector<Float>> = MutableLiveData()
    var gainMinValue: MutableLiveData<Float?> = MutableLiveData()

    var toastStr: MutableLiveData<String> = MutableLiveData()
    var isSaveData: MutableLiveData<Boolean>? = null

    var isFile: MutableLiveData<Boolean> = MutableLiveData(false)

    var location: MutableLiveData<String> = MutableLiveData(filesRepository.getCurrentCheckName())

    fun start() {
        this.isSaveData = filesRepository.isSaveData()
        if (isFile.value!!) {
            this.checkType = filesRepository.getCheckType()
            filesRepository.addDataListener()
        } else {
            this.checkType = dataRepository.getCheckType()
            SocketManager.get().addCallBack(CommandType.ReadYcData, ycBytesDataCallback)
            SocketManager.get().addCallBack(CommandType.RealData, realBytesDataCallback)
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
            dealRealData(source)
            if (filesRepository.isSaveData()?.value == true) {
                filesRepository.toSaveRealData2File(source)
            }
        }
    }

    private var prpsCallback: PrPsDataCallback? = null

    fun setPrpsCallback(callback: PrPsDataCallback) {
        prpsCallback = callback
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

    fun getPhaseData(): ArrayList<HashMap<Int, Float?>> {
        if (isFile.value == true) {
            return filesRepository.getPhaseData(0)
        }
        return dataRepository.getPhaseData(0)
    }

    fun startSaveData() {
        filesRepository.startSaveData()
    }

    fun stopSaveData() {
        filesRepository.stopSaveData()
    }

    fun cleanCurrentData() {
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
        SocketManager.get().removeCallBack(CommandType.ReadYcData, ycBytesDataCallback)
        SocketManager.get().removeCallBack(CommandType.RealData, realBytesDataCallback)
    }

    var checkParamsBean: CheckParamsBean? = null
    var receiverCount = 0
    var mcCount = 0
    var maxValue: Float? = null
    var maxGainValue: Float? = null
    var minValue: Float? = null

    @Volatile
    var gainFloatList = Vector<Float>()
    var gainValue: MutableLiveData<Vector<Float>> = MutableLiveData(Vector())
    private var phaseData: ArrayList<java.util.HashMap<Int, Float?>> = ArrayList()

    private var flightCallback: FlightDataCallback? = null

    fun setFlightCallback(callback: FlightDataCallback) {
        flightCallback = callback
    }

    private fun dealRealData(source: ByteArray) {
        val bytes = ByteArray(source.size - 7)
        System.arraycopy(source, 5, bytes, 0, source.size - 7)

        val newValueList: java.util.ArrayList<Float?> = java.util.ArrayList()
        for (j in 0 until Constants.PRPS_COLUMN) {
            newValueList.add(null)
        }
        val newPointList = java.util.HashMap<Int, Float?>()
        for (i in 0 until (bytes.size / 6)) {
            val values = ByteArray(6)
            System.arraycopy(bytes, 6 * i, values, 0, 6)
            val row = values[0].toInt()//周期，暂不使用
            val column = values[1].toInt()
            val height = ByteArray(4)
            System.arraycopy(values, 2, height, 0, 4)
            val f = ByteUtil.getFloat(height)
            var value = f
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
            }
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
                newPointList[off] = value
            } else {
                Log.d("zhangan", "数据相位异常：$column")
            }
            mcCount++
        }
        phaseData.add(newPointList)
        if (receiverCount % 5 == 0) {
            if (maxGainValue != null) {
                gainFloatList.add(maxGainValue!!)
            }
            if (gainFloatList.size > checkType.settingBean.ljTime * 10) {
                gainFloatList.removeFirst()
            }
            gainValue.postValue(gainFloatList)
            maxGainValue = null
        }
        if (receiverCount == 50) { //一秒钟刷新一次数据
            if (maxValue != null) {
                val df1 = DecimalFormat("0.00")
                checkParamsBean?.fzAttr = "${df1.format(maxValue)}dBm"
            }
            checkParamsBean?.mcCountAttr = "${mcCount}个/秒"
            checkType.checkParams.postValue(checkParamsBean)
            receiverCount = 0
            mcCount = 0
            maxValue = null
        } else {
            ++receiverCount
        }
    }

}