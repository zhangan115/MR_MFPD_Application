package com.mr.mf_pd.application.repository

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.mr.mf_pd.application.common.CheckType
import com.mr.mf_pd.application.common.Constants
import com.mr.mf_pd.application.manager.socket.CommandHelp
import com.mr.mf_pd.application.manager.socket.CommandType
import com.mr.mf_pd.application.manager.socket.ReadListener
import com.mr.mf_pd.application.manager.socket.SocketManager
import com.mr.mf_pd.application.model.CheckParamsBean
import com.mr.mf_pd.application.repository.callback.RealDataCallback
import com.mr.mf_pd.application.repository.impl.DataRepository
import com.mr.mf_pd.application.utils.ByteUtil
import com.mr.mf_pd.application.view.opengl.`object`.PrPsCubeList
import com.mr.mf_pd.application.view.opengl.`object`.PrpsPoint2DList
import org.greenrobot.eventbus.Logger
import java.io.File
import java.math.BigDecimal
import java.text.DecimalFormat
import java.util.*
import java.util.logging.Level
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.math.max
import kotlin.math.min

class DefaultDataRepository : DataRepository {

    private var checkDir: File? = null
    private var tempDir: File? = null
    private lateinit var mCheckType: CheckType
    var checkParamsBean: CheckParamsBean? = null
    var receiverCount = 0
    var mcCount = 0
    var maxValue: Float? = null
    var minValue: Float? = null
    var gainFloatList = ArrayList<Float>()

    var gainValue: MutableLiveData<List<Float>> = MutableLiveData(ArrayList())

    private var phaseData: ArrayList<HashMap<Int, Float>> = ArrayList()

    private var realPointData: ArrayList<HashMap<Int, Float>> = ArrayList()

    var realData: ArrayList<PrPsCubeList> = ArrayList()

    private var realDataCallback: RealDataCallback? = null

    override fun getPhaseData(chartType: Int): ArrayList<HashMap<Int, Float>> {
        val list = ArrayList<HashMap<Int, Float>>()
        if (chartType == 0) {
            if (phaseData.isNotEmpty()) {
                list.add(phaseData.removeFirst())
            }
        } else if (chartType == 1) {
            if (realPointData.isNotEmpty()) {
                list.add(realPointData.removeFirst())
            }
        }
        return list
    }


    override fun setCheckFileDir(dir: File) {
        this.checkDir = dir
    }

    override fun getCheckFileDir(): File? {
        return checkDir
    }

    override fun realDataListener() {
        SocketManager.getInstance().setReadListener(realDataListener)
    }

    override fun removeRealDataListener() {
        SocketManager.getInstance().removeReadListener()
    }

    override fun setRealDataCallback(callback: RealDataCallback) {
        realDataCallback = callback
    }

    override fun addHufData(callback: DataRepository.DataCallback) {
        var prPsCube: PrPsCubeList? = null
        if (realData.isNotEmpty()) {
            prPsCube = realData.lastOrNull()
        }
        var map: HashMap<Int, Float>? = null
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

    override fun switchPassageway(passageway: Int) {
        val bytes = CommandHelp.switchPassageway(passageway)
        cleanData()
        SocketManager.getInstance().sendData(bytes, CommandType.SwitchPassageway) { newBytes ->
            if (Arrays.equals(newBytes, bytes)) {
                Logger.Default.get().log(Level.INFO, "通道${passageway}打开成功")
            }
        }
    }

    override fun closePassageway() {
        val bytes = CommandHelp.closePassageway()
        cleanData()
        SocketManager.getInstance().sendData(bytes, CommandType.SwitchPassageway, null)
    }

    override fun setCheckType(checkType: CheckType) {
        mCheckType = checkType
        checkParamsBean = mCheckType.checkParams.value
        checkType.checkParams.value
    }

    override fun getCheckType(): CheckType {
        return mCheckType
    }

    override fun getGainValueList(): MutableLiveData<List<Float>> {
        return gainValue
    }

    private val realDataListener = object : ReadListener(0) {
        override fun onRead(source: ByteArray) {
            val bytes = ByteArray(source.size - 7)
            System.arraycopy(source, 5, bytes, 0, source.size - 7)

            val newValueList: ArrayList<Float?> = ArrayList()
            for (j in 0 until Constants.PRPS_COLUMN) {
                newValueList.add(null)
            }
            val newPointList = HashMap<Int, Float>()
            for (i in 0 until (bytes.size / 6)) {
                val values = ByteArray(6)
                System.arraycopy(bytes, 6 * i, values, 0, 6)
                val row = values[0].toInt()//暂不使用
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
                }else{

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
                if (maxValue != null) {
                    gainFloatList.add(maxValue!!)
                }
                if (gainFloatList.size >= getCheckType().settingBean.ljTime * 10) {
                    gainFloatList.removeFirst()
                }
                gainValue.postValue(gainFloatList)
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
            } else {
                ++receiverCount
            }
            realDataCallback?.onRealDataChanged()
        }
    }
}