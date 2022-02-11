package com.mr.mf_pd.application.repository

import androidx.lifecycle.MutableLiveData
import com.mr.mf_pd.application.common.CheckType
import com.mr.mf_pd.application.common.Constants
import com.mr.mf_pd.application.manager.socket.CommandHelp
import com.mr.mf_pd.application.manager.socket.ReadListener
import com.mr.mf_pd.application.manager.socket.SocketManager
import com.mr.mf_pd.application.model.CheckParamsBean
import com.mr.mf_pd.application.repository.impl.DataRepository
import com.mr.mf_pd.application.utils.ByteUtil
import com.mr.mf_pd.application.view.opengl.`object`.PrPsCubeList
import org.greenrobot.eventbus.Logger
import java.io.File
import java.util.*
import java.util.logging.Level
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.math.max

class DefaultDataRepository : DataRepository {

    private var checkDir: File? = null
    private var tempDir: File? = null
    private lateinit var mCheckType: CheckType
    var checkParamsBean: CheckParamsBean? = null
    var receiverCount = 0
    var mcCount = 0
    var gainFloatList = ArrayList<Float>()
    var gainValue: MutableLiveData<List<Float>> = MutableLiveData(ArrayList())

    private var phaseData: ArrayList<HashMap<Int, Float>> = ArrayList()
    private var cachePhaseData: ArrayList<HashMap<Int, Float>> = ArrayList()

    private var realPointData: ArrayList<HashMap<Int, Float>> = ArrayList()
    private var realPointCachePhaseData: ArrayList<HashMap<Int, Float>> = ArrayList()

    var realData: ArrayList<PrPsCubeList> = ArrayList()

    override fun getPhaseData(chartType: Int): ArrayList<HashMap<Int, Float>> {
        val list = ArrayList<HashMap<Int, Float>>()
        if (chartType == 0) {
            if (phaseData.isNotEmpty()) {
                list.add(phaseData.removeAt(0))
            }
        } else if (chartType == 1) {
            if (realPointData.isNotEmpty()) {
                list.add(realPointData.removeAt(0))
            }
        }
        return list
    }

    override fun getCachePhaseData(chartType: Int): ArrayList<HashMap<Int, Float>> {
        val list = ArrayList<HashMap<Int, Float>>()
        if (chartType == 0) {
            list.addAll(cachePhaseData)
            cachePhaseData.clear()
        } else if (chartType == 1) {
            list.addAll(realPointCachePhaseData)
            realPointCachePhaseData.clear()
        }
        return list
    }


    private var callbacks: ArrayList<DataRepository.DataCallback> = ArrayList()


    init {
        for (i in 0..4) {
            val list = ArrayList<Float?>()
            for (j in 0 until Constants.PRPS_COLUMN) {
                list.add(null)
            }
            PrPsCubeList.defaultValues.add(list)
        }
    }

    override fun setCheckFileDir(dir: File) {
        this.checkDir = dir
    }

    override fun getCheckFileDir(): File? {
        return checkDir
    }

    override fun hufDataListener() {
        SocketManager.getInstance().setReadListener(hufListener)
    }

    override fun addHufData(callback: DataRepository.DataCallback) {
        callbacks.add(callback)
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

    override fun removeHufDataListener() {
        SocketManager.getInstance().removeReadListener()
    }

    override fun switchPassageway(passageway: Int) {
        val bytes = CommandHelp.switchPassageway(passageway)
        cleanData()
        SocketManager.getInstance().sendData(bytes) { newBytes ->
            if (Arrays.equals(newBytes, bytes)) {
                Logger.Default.get().log(Level.INFO, "通道${passageway}打开成功")
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

    var maxValue = 0f

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

    private val hufListener = object : ReadListener(0) {
        override fun onRead(source: ByteArray) {
            //  将之前的数据全部存储到缓冲中，下次获取数据直接展示，避免数据积累
            val bytes = ByteArray(source.size - 9)
            System.arraycopy(source, 5, bytes, 0, source.size - 9)
            if (phaseData.isNotEmpty()) {
                cachePhaseData.addAll(phaseData)
            }
            phaseData.clear()
            if (realPointData.isNotEmpty()) {
                realPointCachePhaseData.addAll(realPointData)
            }
            realPointData.clear()
            val newValueList = PrPsCubeList.defaultValues.clone() as ArrayList<ArrayList<Float>>
            val newPointList = ArrayList<HashMap<Int, Float>>()

            newPointList.add(HashMap())
            newPointList.add(HashMap())
            newPointList.add(HashMap())
            newPointList.add(HashMap())
            newPointList.add(HashMap())

            for (i in 0 until (bytes.size / 6)) {
                val values = ByteArray(6)
                System.arraycopy(bytes, 6 * i, values, 0, 6)
                val row = values[0].toInt()
                val column = values[1].toInt()
                val height = ByteArray(4)
                System.arraycopy(values, 2, height, 0, 4)
                val f = ByteUtil.getFloat(height)
                maxValue = max(f, maxValue)
                //根据偏移量修改
//                    var off = column - getCheckType().settingBean.xwPy
//                    if (off < 0) {
//                        off += 359
//                    }
                newValueList[row][column] = f
                newPointList[row][column] = f
                mcCount++
            }
            for (i in 0 until PrPsCubeList.defaultValues.size) {
                val floatArray = newValueList[i]
                val prPsCube = PrPsCubeList(floatArray)
                if (realData.size == Constants.PRPS_ROW) {
                    realData.removeFirst()
                }
                realData.add(prPsCube)
            }
            phaseData.addAll(newPointList)
            realPointData.addAll(newPointList)
            gainFloatList.add(maxValue)
            //根据图谱累计时间修改
            if (gainFloatList.size >= getCheckType().settingBean.ljTime * 10) {
                gainFloatList.removeFirst()
            }
            gainValue.postValue(gainFloatList)
            if (receiverCount == 10) {
                checkParamsBean?.fzAttr = "${maxValue}dBm"
                checkParamsBean?.mcCountAttr = "${mcCount}个/秒"
                mCheckType.checkParams.postValue(checkParamsBean)
                receiverCount = 0
                mcCount = 0
            } else {
                receiverCount++
            }
        }
    }
}