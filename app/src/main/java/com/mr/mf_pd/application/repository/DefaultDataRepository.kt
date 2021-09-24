package com.mr.mf_pd.application.repository

import com.mr.mf_pd.application.common.Constants
import com.mr.mf_pd.application.manager.ReadListener
import com.mr.mf_pd.application.manager.SocketManager
import com.mr.mf_pd.application.model.UHFModelBean
import com.mr.mf_pd.application.repository.impl.DataRepository
import com.mr.mf_pd.application.utils.ByteUtil
import com.mr.mf_pd.application.view.opengl.`object`.PrPsCubeList
import com.mr.mf_pd.application.view.opengl.`object`.PrPsXZPoints

class DefaultDataRepository : DataRepository {

    var uhfModelBean: UHFModelBean? = null

    private var pointValueList: ArrayList<PrPsXZPoints> = ArrayList()
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


    override fun getHufData(): UHFModelBean? {
        return uhfModelBean;
    }

    override fun hufDataListener() {
        SocketManager.getInstance().addReadListener(hufListener)
    }

    override fun addHufData(callback: DataRepository.DataCallback) {
        var data: PrPsXZPoints? = null
        if (pointValueList.isNotEmpty()) {
            data = pointValueList.lastOrNull()
        }
        var prPsCube: PrPsCubeList? = null
        if (prPsCubeList.isNotEmpty()) {
            prPsCube = prPsCubeList.lastOrNull()
        }
        if (data != null) {
            callback.addData(data)
            pointValueList.removeLast()
        }
        if (prPsCube != null) {
            callback.addData(prPsCube)
            prPsCubeList.removeLast()
        }
    }

    override fun removeHufDataListener() {
        SocketManager.getInstance().removeReadListener(hufListener)
    }

    override fun toSaveData() {

    }

    private val hufListener = object : ReadListener(0) {
        override fun onRead(bytes: ByteArray?) {
            if (bytes != null) {
                val newValueList = PrPsCubeList.defaultValues.clone() as ArrayList<ArrayList<Float>>
                for (i in 0 until (bytes.size / 6)) {
                    val values = ByteArray(6)
                    System.arraycopy(bytes, 6 * i, values, 0, 6)
                    val position = values[0].toInt()
                    val index = values[1].toInt()
                    val height = ByteArray(4)
                    System.arraycopy(values, 2, height, 0, 4)
                    newValueList[position][index] = ByteUtil.getFloat(height)
                }
                for (i in 0..4) {
                    val floatArray = newValueList[i]
                    val prPsCube = PrPsCubeList(0, floatArray)
                    if (prPsCubeList.size == Constants.PRPS_ROW) {
                        prPsCubeList.removeFirst()
                    }
                    prPsCubeList.add(prPsCube)
                }
            }
        }
    }
}