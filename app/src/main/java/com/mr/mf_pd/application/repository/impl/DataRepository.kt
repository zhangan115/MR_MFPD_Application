package com.mr.mf_pd.application.repository.impl

import androidx.lifecycle.MutableLiveData
import com.mr.mf_pd.application.common.CheckType
import com.mr.mf_pd.application.view.opengl.`object`.PrPsCubeList
import java.io.File

interface DataRepository {

    /**
     * 设置当前检测任务的文件夹
     * @param dir 文件夹
     */
    fun setCheckFileDir(dir: File)

    /**
     * 获取当前检测任务的文件夹
     * @return 文件夹
     */
    fun getCheckFileDir(): File?

    /**
     * 获取图表数据
     */
    fun getPhaseData(chartType: Int): ArrayList<HashMap<Int, Float>>

    /**
     * 获取缓冲的图表数据
     */
    fun getCachePhaseData(chartType: Int): ArrayList<HashMap<Int, Float>>


    fun hufDataListener()

    fun addHufData(callback: DataCallback)

    fun removeHufDataListener()

    fun switchPassageway(passageway: Int)

    interface DataCallback {

        fun addData(map: HashMap<Int, Float>, prPsCube: PrPsCubeList)
    }

    fun cleanData()

    fun setCheckType(checkType: CheckType)

    fun getCheckType(): CheckType

    fun getGainValueList(): MutableLiveData<List<Float>>
}