package com.mr.mf_pd.application.repository

import android.util.Log
import com.mr.mf_pd.application.common.Constants
import com.mr.mf_pd.application.manager.ReadListener
import com.mr.mf_pd.application.manager.SocketManager
import com.mr.mf_pd.application.model.UHFModelBean
import com.mr.mf_pd.application.repository.impl.DataRepository
import com.mr.mf_pd.application.view.opengl.`object`.PrPsCubeList
import com.mr.mf_pd.application.view.opengl.`object`.PrPsXZPoints
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class DefaultDataRepository : DataRepository {

    var uhfModelBean: UHFModelBean? = null
    var isRequest = true

    private var pointValueList: ArrayList<PrPsXZPoints> = ArrayList()
    private var prPsCubeList: ArrayList<PrPsCubeList> = ArrayList()

    override fun getHufData(): UHFModelBean? {
        return uhfModelBean;
    }

    var hufListenerJob: Job? = null

    override fun hufDataListener() {
        SocketManager.getInstance().addReadListener(hufListener)
        if (hufListenerJob == null || hufListenerJob!!.isCancelled) {
            hufListenerJob = GlobalScope.launch {
                while (isRequest) {
                    Thread.sleep(20)
                    val startTime = System.currentTimeMillis()
                    val pointValue = FloatArray(Constants.PRPS_COLUMN)
                    val floatArray = FloatArray(Constants.PRPS_COLUMN)
                    for (j in 0 until Constants.PRPS_COLUMN) {
                        val value = Math.random().toFloat() * 2f - 1f
                        floatArray[j] = value
                        pointValue[j] = value
                    }
                    val prPsCube = PrPsCubeList(0, floatArray)
                    val data = PrPsXZPoints(pointValue)
                    if (pointValueList.size == Constants.PRPS_ROW) {
                        pointValueList.removeFirst()
                    }
                    if (prPsCubeList.size == Constants.PRPS_ROW) {
                        prPsCubeList.removeFirst()
                    }
                    pointValueList.add(data)
                    prPsCubeList.add(prPsCube)
                    val endTime = System.currentTimeMillis()
                }
            }
            hufListenerJob?.start()
        }
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
        if (data != null && prPsCube != null) {
            callback.addData(data, prPsCube)
            pointValueList.removeLast()
            prPsCubeList.removeLast()
        }
    }

    override fun removeHufDataListener() {
        SocketManager.getInstance().removeLinkStateListener { hufListener }
        hufListenerJob?.cancel()
    }

    override fun toSaveData() {

    }

    private val hufListener = object : ReadListener(1) {
        override fun onRead(bytes: ByteArray?) {

        }
    }
}