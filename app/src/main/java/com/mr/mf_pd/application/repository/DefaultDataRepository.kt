package com.mr.mf_pd.application.repository

import android.util.Log
import com.mr.mf_pd.application.app.MRApplication
import com.mr.mf_pd.application.common.Constants
import com.mr.mf_pd.application.manager.socket.CommandHelp
import com.mr.mf_pd.application.manager.socket.ReadListener
import com.mr.mf_pd.application.manager.socket.SocketManager
import com.mr.mf_pd.application.model.ACModelBean
import com.mr.mf_pd.application.model.UHFModelBean
import com.mr.mf_pd.application.repository.impl.DataRepository
import com.mr.mf_pd.application.utils.ByteUtil
import com.mr.mf_pd.application.utils.DataUtil
import com.mr.mf_pd.application.view.opengl.`object`.PrPsCubeList
import java.io.File
import java.io.FileOutputStream
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class DefaultDataRepository : DataRepository {

    var uhfModelBean: UHFModelBean? = null
    var acModelBean: ACModelBean? = null
    var isSaving = false

    private var checkDir: File? = null
    var saveDataFileThread: Thread? = null
    var tempDir: File? = null
    val mFos: FileOutputStream? = null

    private var phaseData: ArrayList<HashMap<Int, Float>> = ArrayList()
    private var cachePhaseData: ArrayList<HashMap<Int, Float>> = ArrayList()

    private var realPointData: ArrayList<HashMap<Int, Float>> = ArrayList()
    private var realPointCachePhaseData: ArrayList<HashMap<Int, Float>> = ArrayList()

    var realData: ArrayList<PrPsCubeList> = ArrayList()

    override fun getPhaseData(chartType:Int): ArrayList<HashMap<Int, Float>> {
        val list = ArrayList<HashMap<Int, Float>>()
        if (chartType == 0) {
            if (phaseData.isNotEmpty()) {
                list.add(phaseData.removeAt(0))
            }
        }else if (chartType == 1){
            if (realPointData.isNotEmpty()) {
                list.add(realPointData.removeAt(0))
            }
        }
        return list
    }

    override fun getCachePhaseData(chartType:Int): ArrayList<HashMap<Int, Float>> {
        val list = ArrayList<HashMap<Int, Float>>()
        if (chartType == 0) {
            list.addAll(cachePhaseData)
            cachePhaseData.clear()
        }else if (chartType == 1){
            list.addAll(realPointCachePhaseData)
            realPointCachePhaseData.clear()
        }
        return list
    }


    private var callbacks: ArrayList<DataRepository.DataCallback> = ArrayList()


    /**
     * 实时模式获取数据
     */
    private var realDataCallback: DataRepository.RealDataCallback? = null

    init {
        for (i in 0..4) {
            val list = ArrayList<Float?>()
            for (j in 0 until Constants.PRPS_COLUMN) {
                list.add(null)
            }
            PrPsCubeList.defaultValues.add(list)
        }
    }

    override fun startSaveData() {
        val dirName = DataUtil.timeFormat(System.currentTimeMillis(), "yyyy_mm_dd_hh_mm_ss")
        tempDir = File(MRApplication.instance.fileCacheFile(), dirName)
        isSaving = true
    }

    override fun stopSaveData() {
        isSaving = false
    }

    override fun setCheckFileDir(dir: File) {
        this.checkDir = dir
    }

    override fun getCheckFileDir(): File? {
        return checkDir
    }

    override fun getHufData(): UHFModelBean? {
        return uhfModelBean
    }

    override fun getAcData(): ACModelBean? {
        return acModelBean
    }

    override fun hufDataListener() {
        SocketManager.getInstance().addReadListener(hufListener)
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
        SocketManager.getInstance().removeReadListener(hufListener)
    }

    override fun switchPassageway(passageway: Int) {
        val bytes = CommandHelp.switchPassageway(passageway)
        cleanData()
        SocketManager.getInstance().sendData(bytes) { newBytes ->
            if (Arrays.equals(newBytes, bytes)) {
                Log.d("zhangan", "通道${passageway}打开成功")
            }
        }
    }

    override fun setRealDataCallback(callback: DataRepository.RealDataCallback) {
        this.realDataCallback = callback
    }

    override fun cleanData() {
        phaseData.clear()
        realData.clear()
    }

    private val hufListener = object : ReadListener(0) {
        override fun onRead(source: ByteArray, bytes: ByteArray?) {
            if (bytes != null) {
                // TODO: 10/31/21 将之前的数据全部存储到缓冲中，下次获取数据直接展示，避免数据积累
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
                    newValueList[row][column] = f
                    newPointList[row][column] = f
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
                if (isSaving) {

                }
            }
        }
    }
}