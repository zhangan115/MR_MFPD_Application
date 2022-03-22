package com.mr.mf_pd.application.repository

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.mr.mf_pd.application.common.CheckType
import com.mr.mf_pd.application.common.Constants
import com.mr.mf_pd.application.manager.socket.SocketManager
import com.mr.mf_pd.application.manager.socket.callback.BaseDataCallback
import com.mr.mf_pd.application.manager.socket.callback.ReadListener
import com.mr.mf_pd.application.manager.socket.callback.YcDataCallback
import com.mr.mf_pd.application.manager.socket.comand.CommandHelp
import com.mr.mf_pd.application.model.CheckParamsBean
import com.mr.mf_pd.application.repository.callback.DataCallback
import com.mr.mf_pd.application.repository.callback.RealDataCallback
import com.mr.mf_pd.application.repository.impl.DataRepository
import com.mr.mf_pd.application.utils.ByteUtil
import com.mr.mf_pd.application.view.opengl.`object`.PrPsCubeList
import io.reactivex.disposables.Disposable
import java.text.DecimalFormat
import java.util.*
import kotlin.math.max
import kotlin.math.min

class DefaultDataRepository : DataRepository {

    companion object {
        var realDataMaxValue: MutableLiveData<Int> = MutableLiveData()
        var realDataMinValue: MutableLiveData<Int> = MutableLiveData()
    }

    private lateinit var mCheckType: CheckType
    var checkParamsBean: CheckParamsBean? = null
    var receiverCount = 0
    var mcCount = 0
    var maxValue: Float? = null
    var maxGainValue: Float? = null
    var minValue: Float? = null
    @Volatile
    var gainFloatList = Vector<Float>()

    var gainValue: MutableLiveData<Vector<Float>> = MutableLiveData(Vector())

    private var phaseData: ArrayList<HashMap<Int, Float?>> = ArrayList()

    private var realPointData: ArrayList<HashMap<Int, Float?>> = ArrayList()

    var realData: ArrayList<PrPsCubeList> = ArrayList()

    private var realDataCallbacks: ArrayList<RealDataCallback> = ArrayList()

    private var ycDataCallbacks: ArrayList<BaseDataCallback> = ArrayList()

    override fun getPhaseData(chartType: Int): ArrayList<HashMap<Int, Float?>> {
        val list = ArrayList<HashMap<Int, Float?>>()
        if (chartType == 0) {
            if (phaseData.isNotEmpty()) {
                phaseData.removeFirstOrNull()?.let { list.add(it) }
            }
        } else if (chartType == 1) {
            if (realPointData.isNotEmpty()) {
                list.add(realPointData.removeFirst())
            }
        }
        return list
    }

    override fun addDataListener() {
        SocketManager.get().setReadListener(realDataListener)
        SocketManager.get().ycDataCallback = ycDataCallback
    }

   private val ycDataCallback = object :YcDataCallback {
        override fun onData(source: ByteArray) {
            ycDataCallbacks.forEach {
                it.onData(source)
            }
        }
    }

    override fun removeRealDataListener() {
        SocketManager.get().removeReadListener()
    }

    override fun addRealDataCallback(callback: RealDataCallback) {
        realDataCallbacks.add(callback)
    }

    override fun addYcDataCallback(callback: BaseDataCallback) {
        ycDataCallbacks.add(callback)
    }
    override fun removeYcDataCallback(callback: BaseDataCallback) {
        ycDataCallbacks.remove(callback)
    }

    override fun addHufData(callback: DataCallback) {
        var prPsCube: PrPsCubeList? = null
        if (realData.isNotEmpty()) {
            prPsCube = realData.lastOrNull()
        }
        var map: HashMap<Int, Float?>? = null
        if (phaseData.isNotEmpty()) {
            map = phaseData.lastOrNull()
        }
        if (prPsCube != null && map != null) {
            callback.addData(map, prPsCube)
            if (realData.isNotEmpty()) {
                realData.removeLast()
            }
        }
    }

    override fun cleanData() {
        receiverCount = 0
        mcCount = 0
        gainFloatList.clear()
        phaseData.clear()
        realData.clear()
    }

    override fun switchPassageway(passageway: Int,commandType: Int) {
        cleanData()
        SocketManager.get().sendData(CommandHelp.switchPassageway(passageway,commandType))
    }

    override fun closePassageway() {
        cleanData()
        SocketManager.get().sendData(CommandHelp.closePassageway())
    }

    override fun setCheckType(checkType: CheckType) {
        mCheckType = checkType
        checkParamsBean = mCheckType.checkParams.value
        realDataMaxValue.postValue(mCheckType.settingBean.maxValue)
        realDataMinValue.postValue(mCheckType.settingBean.minValue)
        checkType.checkParams.postValue(checkParamsBean)
    }

    override fun getCheckType(): CheckType {
        return mCheckType
    }

    override fun getGainValueList(): MutableLiveData<Vector<Float>> {
        return gainValue
    }

    override fun readYcValue(): Disposable {
        return SocketManager.get().sendData(CommandHelp.readYcValue(getCheckType().passageway))
    }

    override fun readRepeatData(): Disposable {
        return SocketManager.get().sendRepeatData(CommandHelp.readYcValue(getCheckType().passageway),1)
    }

    private val realDataListener = object : ReadListener {
        override fun onData(source: ByteArray) {
            realDataCallbacks.forEach {
                it.onRealDataChanged(source)
            }
            val bytes = ByteArray(source.size - 7)
            System.arraycopy(source, 5, bytes, 0, source.size - 7)

            val newValueList: ArrayList<Float?> = ArrayList()
            for (j in 0 until Constants.PRPS_COLUMN) {
                newValueList.add(null)
            }
            val newPointList = HashMap<Int, Float?>()
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
                val setting = getCheckType().settingBean
                //处理固定尺度
                if (setting.gdCd == 1) {
                    if (f > setting.maxValue) {
                        value = setting.maxValue.toFloat()
                    } else if (f < setting.minValue) {
                        value = setting.minValue.toFloat()
                    }
                } else {
                    if (realDataMaxValue.value != null) {
                        val maxValue = max(realDataMaxValue.value!!, f.toInt())
                        if (maxValue != realDataMaxValue.value!!) {
                            realDataMaxValue.postValue(maxValue)
                        }
                    } else {
                        realDataMaxValue.postValue(setting.maxValue)
                    }
                    if (realDataMinValue.value != null) {
                        val minValue = min(realDataMinValue.value!!, f.toInt())
                        if (minValue != realDataMinValue.value!!) {
                            realDataMinValue.postValue(minValue)
                        }
                    } else {
                        realDataMinValue.postValue(setting.minValue)
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
            realPointData.add(newPointList)
            if (realData.size == Constants.PRPS_ROW) {
                realData.removeFirst()
            }
            realData.add(PrPsCubeList(newValueList))
            if (receiverCount % 5 == 0) {
                if (maxGainValue != null) {
                    gainFloatList.add(maxGainValue!!)
                }
                if (gainFloatList.size > getCheckType().settingBean.ljTime * 10) {
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
                mCheckType.checkParams.postValue(checkParamsBean)
                receiverCount = 0
                mcCount = 0
                maxValue = null
            } else {
                ++receiverCount
            }
        }
    }
}