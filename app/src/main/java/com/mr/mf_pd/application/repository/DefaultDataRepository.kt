package com.mr.mf_pd.application.repository

import android.util.Log
import com.mr.mf_pd.application.common.Constants
import com.mr.mf_pd.application.manager.socket.CommandHelp
import com.mr.mf_pd.application.manager.socket.ReadListener
import com.mr.mf_pd.application.manager.socket.ReceiverCallback
import com.mr.mf_pd.application.manager.socket.SocketManager
import com.mr.mf_pd.application.model.ACModelBean
import com.mr.mf_pd.application.model.UHFModelBean
import com.mr.mf_pd.application.repository.impl.DataRepository
import com.mr.mf_pd.application.utils.ByteUtil
import com.mr.mf_pd.application.view.opengl.`object`.PrPsCubeList
import java.io.File
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class DefaultDataRepository : DataRepository {

    var uhfModelBean: UHFModelBean? = null
    private var checkDir: File? = null
    var acModelBean: ACModelBean? = null
    var pointList: ArrayList<HashMap<Int, Float>> = ArrayList()
    private var prPsCubeList: ArrayList<PrPsCubeList> = ArrayList()

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
        var prPsCube: PrPsCubeList? = null
        if (prPsCubeList.isNotEmpty()) {
            prPsCube = prPsCubeList.lastOrNull()
        }
        var map: HashMap<Int, Float>? = null
        if (pointList.isNotEmpty()) {
            map = pointList.lastOrNull()
        }
        if (prPsCube != null && map != null) {
            callback.addData(map, prPsCube)
            prPsCubeList.removeLast()
            pointList.removeLast()
        }
    }

    override fun removeHufDataListener() {
        SocketManager.getInstance().removeReadListener(hufListener)
    }

    override fun toSaveData() {

    }

    override fun switchPassageway(passageway: Int) {
        val bytes = CommandHelp.switchPassageway(passageway)
        SocketManager.getInstance().sendData(bytes) { newBytes ->
            if (Arrays.equals(newBytes, bytes)) {
                Log.d("zhangan", "通道打开成功")
            }
        }
    }

    private val hufListener = object : ReadListener(0) {
        override fun onRead(bytes: ByteArray?) {
            if (bytes != null) {
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
                    val f = ByteUtil.getFloat(height) * -1
                    newValueList[row][column] = f / 4
                    newPointList[row][column] = f / 4
                }
                for (i in 0 until PrPsCubeList.defaultValues.size) {
                    val floatArray = newValueList[i]
                    val prPsCube = PrPsCubeList(floatArray)
                    if (prPsCubeList.size == Constants.PRPS_ROW) {
                        prPsCubeList.removeFirst()
                    }
                    prPsCubeList.add(prPsCube)
                }
                pointList.addAll(newPointList)
            }
        }
    }
}